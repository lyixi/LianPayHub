package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.website.*;
import com.lianpayhub.service.website.WebsiteContentService;
import com.lianpayhub.web.api.dto.PublicWebsiteProduct;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/site/v1")
public class PublicWebsiteController {
 private final WebsiteContentService service; public PublicWebsiteController(WebsiteContentService service){this.service=service;}
 @GetMapping("/site") public ApiResponse<WebsiteSiteSettings> site(){return ApiResponse.ok(service.siteSettings());}
 @GetMapping("/products") public ApiResponse<List<PublicWebsiteProduct>> products(){return ApiResponse.ok(service.publicProducts().stream().map(PublicWebsiteProduct::from).collect(Collectors.toList()));}
 @GetMapping("/products/{slug}") public ApiResponse<PublicWebsiteProduct> product(@PathVariable String slug){return ApiResponse.ok(PublicWebsiteProduct.from(service.publicProduct(slug)));}
 @GetMapping("/articles") public ApiResponse<List<WebsiteArticle>> articles(@RequestParam(required=false) String category){return ApiResponse.ok(service.publicArticles(category));}
 @GetMapping("/articles/{slug}") public ApiResponse<WebsiteArticle> article(@PathVariable String slug){return ApiResponse.ok(service.publicArticle(slug));}
 @GetMapping("/pages") public ApiResponse<List<WebsitePage>> pages(){return ApiResponse.ok(service.publicPages());}
 @GetMapping("/pages/{slug}") public ApiResponse<WebsitePage> page(@PathVariable String slug){return ApiResponse.ok(service.publicPage(slug));}
 @PostMapping("/leads") public ApiResponse<WebsiteLead> lead(@Valid @RequestBody LeadRequest r){return ApiResponse.ok(service.createLead(r.name,r.contact,r.subject,r.message));}
 public static class LeadRequest {@NotBlank @Size(max=80) public String name; @NotBlank @Size(max=128) public String contact; @Size(max=160) public String subject; @NotBlank @Size(max=2000) public String message;}
}
