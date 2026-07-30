package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.repository.AppLoginLogRepository;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.user.UserProfileResult;
import com.lianpayhub.service.user.UserProfileService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AppLoginLogRepository appLoginLogRepository;

    public UserProfileController(UserProfileService userProfileService,
                                 AppLoginLogRepository appLoginLogRepository) {
        this.userProfileService = userProfileService;
        this.appLoginLogRepository = appLoginLogRepository;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResult> profile(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.ok(userProfileService.profile(principal.getUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResult> updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                                                        @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.ok(userProfileService.updateProfile(
                principal.getUserId(),
                request.username(),
                request.nickname()
        ));
    }

    @PostMapping("/password/set")
    public ApiResponse<Void> setPassword(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @Valid @RequestBody SetPasswordRequest request) {
        userProfileService.setPassword(principal.getUserId(), request.password());
        return ApiResponse.ok();
    }

    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal AppUserPrincipal principal,
                                            @Valid @RequestBody ChangeUserPasswordRequest request) {
        userProfileService.changePassword(principal.getUserId(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok();
    }

    @PostMapping("/avatar")
    public ApiResponse<UserProfileResult> uploadAvatar(@AuthenticationPrincipal AppUserPrincipal principal,
                                                       @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(userProfileService.uploadAvatar(principal.getUserId(), principal.getAppId(), file));
    }

    @PostMapping("/mobile/change")
    public ApiResponse<UserProfileResult> changeMobile(@AuthenticationPrincipal AppUserPrincipal principal,
                                                       @Valid @RequestBody ChangeMobileRequest request) {
        return ApiResponse.ok(userProfileService.changeMobile(
                principal.getUserId(),
                principal.getAppId(),
                principal.getMobile(),
                request.newMobile(),
                request.oldCode(),
                request.newCode()
        ));
    }

    @GetMapping("/login-logs")
    public ApiResponse<Page<AppLoginLog>> loginLogs(@AuthenticationPrincipal AppUserPrincipal principal,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(appLoginLogRepository.findByAppIdAndUserId(
                principal.getAppId(),
                principal.getUserId(),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))
        ));
    }
}
