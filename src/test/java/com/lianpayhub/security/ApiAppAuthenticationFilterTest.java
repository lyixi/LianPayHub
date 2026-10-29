package com.lianpayhub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.service.security.ApiSignatureService;
import com.lianpayhub.service.security.AppSecretService;
import com.lianpayhub.service.security.NonceCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ApiAppAuthenticationFilterTest {

    @Test
    void allowsOnlyPublicWebsiteRequestsWithoutAppAuthentication() {
        SecurityProperties properties = new SecurityProperties();
        properties.setApiAuthEnabled(true);
        ApiAppAuthenticationFilter filter = new ApiAppAuthenticationFilter(
                properties,
                mock(AppInfoRepository.class),
                mock(AppSecretService.class),
                mock(ApiSignatureService.class),
                mock(NonceCacheService.class),
                new ObjectMapper());

        assertPublic(filter, "GET", "/api/site/v1/site");
        assertPublic(filter, "GET", "/api/site/v1/products");
        assertPublic(filter, "GET", "/api/site/v1/products/writing-world");
        assertPublic(filter, "GET", "/api/site/v1/articles");
        assertPublic(filter, "GET", "/api/site/v1/articles/news-item");
        assertPublic(filter, "GET", "/api/site/v1/pages/about");
        assertPublic(filter, "POST", "/api/site/v1/leads");

        assertProtected(filter, "POST", "/api/site/v1/products");
        assertProtected(filter, "GET", "/api/payment/orders");
        assertProtected(filter, "GET", "/api/site/v1/leads");
    }

    private void assertPublic(ApiAppAuthenticationFilter filter, String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        assertTrue(filter.shouldNotFilter(request), method + " " + uri);
    }

    private void assertProtected(ApiAppAuthenticationFilter filter, String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        assertFalse(filter.shouldNotFilter(request), method + " " + uri);
    }
}
