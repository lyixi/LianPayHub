package com.lianpayhub.service.storage;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.StorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@Component
public class FileValidator {

    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final int MAGIC_READ_BYTES = 16;

    private final StorageProperties props;

    public FileValidator(StorageProperties props) {
        this.props = props;
    }

    /**
     * 校验上传文件，返回匹配的文件类型。
     * 抛出 BusinessException 表示校验不通过。
     */
    public AllowedFileType validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }

        String ext = extractExtension(file.getOriginalFilename());
        AllowedFileType type = findByExtension(ext);

        byte[] header = readHeader(file);

        if (type.magicBytes != null) {
            checkMagicBytes(header, type);
        }

        long maxBytes = type.category == FileCategory.IMAGE
                ? props.getMaxImageFileBytes()
                : props.getMaxConfigFileBytes();
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "文件超过大小限制：" + (maxBytes / 1024) + " KB");
        }

        if (type.category == FileCategory.IMAGE) {
            validateImageDimensions(file);
        }

        return type;
    }

    /**
     * 校验虚拟路径合法性（防路径穿越、层级过深）。
     */
    public String validateVirtualPath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        // 拒绝空字节
        if (path.contains("\0")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "路径包含非法字符");
        }
        // 标准化路径，拒绝跳出根目录
        String normalized;
        try {
            normalized = Paths.get("/", path).normalize().toString().replace("\\", "/");
        } catch (InvalidPathException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "路径格式不正确");
        }
        if (!normalized.startsWith("/")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "路径不合法");
        }
        // 检查目录层级深度
        long depth = normalized.chars().filter(c -> c == '/').count();
        if (depth > props.getMaxPathDepth()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "目录层级超过上限：" + props.getMaxPathDepth());
        }
        return normalized;
    }

    private AllowedFileType findByExtension(String ext) {
        for (AllowedFileType type : AllowedFileType.values()) {
            for (String allowed : type.extensions) {
                if (allowed.equals(ext)) {
                    return type;
                }
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的文件类型：" + ext);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件名缺少扩展名");
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private byte[] readHeader(MultipartFile file) {
        try {
            byte[] buf = new byte[MAGIC_READ_BYTES];
            int read = file.getInputStream().read(buf);
            return Arrays.copyOf(buf, read);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "文件读取失败");
        }
    }

    private void checkMagicBytes(byte[] header, AllowedFileType type) {
        byte[] magic = type.magicBytes;
        if (header.length < magic.length) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件内容与扩展名不匹配");
        }
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "文件内容与扩展名不匹配");
            }
        }
    }

    private void validateImageDimensions(MultipartFile file) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (img == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "图片无法解析，文件可能已损坏");
            }
            if (img.getWidth() > MAX_IMAGE_DIMENSION || img.getHeight() > MAX_IMAGE_DIMENSION) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "图片尺寸超过上限：" + MAX_IMAGE_DIMENSION + "px");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片解析失败");
        }
    }
}
