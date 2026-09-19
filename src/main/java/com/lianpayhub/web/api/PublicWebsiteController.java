package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.website.*;
import com.lianpayhub.service.website.WebsiteContentService;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/site/v1")
public class PublicWebsiteController {
 private final WebsiteContentService service; public PublicWebsiteController(WebsiteContentService service){this.service=service;}
 @GetMapping("/site") public ApiResponse<WebsiteSiteSettings> site(){return ApiResponse.ok(service.siteSettings());}
 @GetMapping("/products") public ApiResponse<List<WebsiteProduct>> products(){return ApiResponse.ok(service.publicProducts());}
 @GetMapping("/products/{slug}") public ApiResponse<WebsiteProduct> product(@PathVariable String slug){return ApiResponse.ok(service.publicProduct(slug));}
 @GetMapping("/articles") public ApiResponse<List<WebsiteArticle>> articles(@RequestParam(required=false) String category){return ApiResponse.ok(service.publicArticles(category));}
 @GetMapping("/articles/{slug}") public ApiResponse<WebsiteArticle> article(@PathVariable String slug){return ApiResponse.ok(service.publicArticle(slug));}
 @PostMapping("/leads") public ApiResponse<WebsiteLead> lead(@Valid @RequestBody LeadRequest r){return ApiResponse.ok(service.createLead(r.name,r.contact,r.subject,r.message));}
 public static class LeadRequest {@NotBlank @Size(max=80) public String name; @NotBlank @Size(max=128) public String contact; @Size(max=160) public String subject; @NotBlank @Size(max=2000) public String message;}
}
