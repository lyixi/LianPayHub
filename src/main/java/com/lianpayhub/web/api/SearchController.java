package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.search.UnifiedSearchResult;
import com.lianpayhub.service.search.UnifiedSearchService;
import com.lianpayhub.service.security.AppUserAccessService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final UnifiedSearchService searchService;
    private final AppUserAccessService appUserAccessService;

    public SearchController(UnifiedSearchService searchService, AppUserAccessService appUserAccessService) {
        this.searchService = searchService;
        this.appUserAccessService = appUserAccessService;
    }

    @GetMapping
    public ApiResponse<List<UnifiedSearchResult>> search(@RequestParam String appId,
                                                         @RequestParam String keyword,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(defaultValue = "20") int limit,
                                                         @AuthenticationPrincipal AppUserPrincipal principal) {
        appUserAccessService.requireUserAccessWhenNeeded(appId, userId, principal);
        return ApiResponse.ok(searchService.search(appId, keyword, limit));
    }
}
