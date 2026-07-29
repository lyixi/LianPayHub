package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.search.UnifiedSearchResult;
import com.lianpayhub.service.search.UnifiedSearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/platform-search")
public class AdminPlatformSearchController {

    private final UnifiedSearchService searchService;

    public AdminPlatformSearchController(UnifiedSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<List<UnifiedSearchResult>> search(@RequestParam String appId,
                                                         @RequestParam String keyword,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(searchService.search(appId, keyword, limit));
    }
}
