package com.lianpayhub.service.user;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.auth.SmsCodeService;
import com.lianpayhub.service.storage.AllowedFileType;
import com.lianpayhub.service.storage.FileCategory;
import com.lianpayhub.service.storage.FileValidator;
import com.lianpayhub.service.storage.StoredFile;
import com.lianpayhub.service.storage.StorageService;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileService {

    private static final int AVATAR_SIZE = 256;

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final AppService appService;
    private final SmsCodeService smsCodeService;

    public UserProfileService(UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder,
                              StorageService storageService, FileValidator fileValidator,
                              AppService appService, SmsCodeService smsCodeService) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
        this.appService = appService;
        this.smsCodeService = smsCodeService;
    }

    @Transactional(readOnly = true)
    public UserProfileResult profile(Long userId) {
        return new UserProfileResult(requireUser(userId));
    }

    @Transactional
    public UserProfileResult updateProfile(Long userId, String username, String nickname) {
        UserInfo userInfo = requireUser(userId);
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername != null) {
            userInfoRepository.findByUsername(normalizedUsername)
                    .filter(other -> !other.getId().equals(userId))
                    .ifPresent(other -> {
                        throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
                    });
        }
        userInfo.updateProfile(normalizedUsername, trimToNull(nickname));
        return new UserProfileResult(userInfo);
    }

    @Transactional
    public void setPassword(Long userId, String password) {
        UserInfo userInfo = requireUser(userId);
        if (StringUtils.hasText(userInfo.getPasswordHash()) && !userInfo.isMustChangePassword()) {
            throw new BusinessException(ErrorCode.CONFLICT, "密码已设置，请使用修改密码接口");
        }
        userInfo.setPasswordHash(passwordEncoder.encode(validatePassword(password)));
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserInfo userInfo = requireUser(userId);
        if (!StringUtils.hasText(userInfo.getPasswordHash())
                || !passwordEncoder.matches(oldPassword, userInfo.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码错误");
        }
        userInfo.setPasswordHash(passwordEncoder.encode(validatePassword(newPassword)));
    }

    @Transactional
    public UserProfileResult uploadAvatar(Long userId, String appId, MultipartFile file) {
        AppInfo appInfo = appService.requireEnabledApp(appId);
        if (!appInfo.isAllowAvatarUpload()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 未开启头像上传");
        }

        AllowedFileType type = fileValidator.validate(file);
        if (type.getCategory() != FileCategory.IMAGE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "头像必须是图片文件");
        }

        UserInfo userInfo = requireUser(userId);
        byte[] jpegBytes = compressAvatar(file);
        String oldKey = userInfo.getAvatarStorageKey();
        String key = "avatars/" + appId + "/" + userId + "/" + UUID.randomUUID() + ".jpg";
        StoredFile stored = storageService.store(key, new ByteArrayInputStream(jpegBytes), jpegBytes.length, "image/jpeg");
        String url = storageService.getDownloadUrl(stored.getKey(), Duration.ofDays(365));
        userInfo.updateAvatar(stored.getKey(), url, stored.getContentType(), stored.getSize());
        if (StringUtils.hasText(oldKey)) {
            storageService.delete(oldKey);
        }
        return new UserProfileResult(userInfo);
    }

    @Transactional
    public UserProfileResult changeMobile(Long userId, String appId, String oldMobile, String newMobile,
                                          String oldCode, String newCode) {
        UserInfo userInfo = requireUser(userId);
        AppInfo appInfo = appService.requireEnabledApp(appId);
        if (appInfo.getAppType() == com.lianpayhub.domain.app.AppType.DEVICE_ONLY || !appInfo.isNeedMobileLogin()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 不支持手机号登录");
        }
        String normalizedNewMobile = trimToNull(newMobile);
        if (normalizedNewMobile == null || !normalizedNewMobile.matches("^1\\d{10}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        if (normalizedNewMobile.equals(userInfo.getMobile())) {
            throw new BusinessException(ErrorCode.CONFLICT, "新手机号不能与旧手机号相同");
        }
        if (userInfoRepository.findByMobile(normalizedNewMobile).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
        }
        smsCodeService.verifyAndConsume(appId, trimToNull(oldMobile) == null ? userInfo.getMobile() : trimToNull(oldMobile), oldCode);
        smsCodeService.verifyAndConsume(appId, normalizedNewMobile, newCode);
        userInfo.updateProfile(normalizedNewMobile, userInfo.getUsername(), userInfo.getNickname());
        userInfo.bumpTokenVersion();
        return new UserProfileResult(userInfo);
    }

    private UserInfo requireUser(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    }

    private String normalizeUsername(String username) {
        String value = trimToNull(username);
        if (value == null) {
            return null;
        }
        value = value.toLowerCase(Locale.ROOT);
        if (!value.matches("^[a-z0-9_][a-z0-9_.-]{2,31}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名需为 3-32 位字母、数字、点、横线或下划线");
        }
        return value;
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度需为 8-64 位");
        }
        return password;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private byte[] compressAvatar(MultipartFile file) {
        try {
            BufferedImage source = ImageIO.read(file.getInputStream());
            if (source == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "图片无法解析");
            }
            int minSide = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - minSide) / 2;
            int y = (source.getHeight() - minSide) / 2;
            BufferedImage target = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = target.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, AVATAR_SIZE, AVATAR_SIZE, x, y, x + minSide, y + minSide, null);
            graphics.dispose();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(target, "jpg", output);
            return output.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "头像处理失败");
        }
    }
}
