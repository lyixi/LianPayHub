package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.website.*;
import com.lianpayhub.service.website.WebsiteContentService;
import com.lianpayhub.service.website.WebsiteMediaService;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@org.springframework.transaction.annotation.Transactional
@RestController @RequestMapping("/admin/website")
public class AdminWebsiteController {
 private final WebsiteContentService service; private final WebsiteMediaService media;
 public AdminWebsiteController(WebsiteContentService service,WebsiteMediaService media){this.service=service;this.media=media;}
 @GetMapping("/products") public ApiResponse<List<WebsiteProduct>> products(){return ApiResponse.ok(service.adminProducts());}
 @PostMapping("/products") public ApiResponse<WebsiteProduct> createProduct(@Valid @RequestBody ProductRequest r){return ApiResponse.ok(publish(service.createProduct(r.slug,r.appId,r.name,r.tagline,r.summary,r.accentColor,r.coverUrl,r.downloadUrl,r.downloadJson,r.purchaseUrl,r.content,r.mediaJson,r.sortOrder,r.previewProject,r.previewVolume,r.previewChapter,r.previewExcerpt,r.previewInsight,r.previewChapterTwo,r.previewExcerptTwo,r.previewChapterThree,r.previewExcerptThree,r.previewBody,r.previewDraft),r.publish));}
 @PutMapping("/products/{id}") public ApiResponse<WebsiteProduct> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r){return ApiResponse.ok(publish(service.updateProduct(id,r.appId,r.name,r.tagline,r.summary,r.accentColor,r.coverUrl,r.downloadUrl,r.downloadJson,r.purchaseUrl,r.content,r.mediaJson,r.sortOrder,r.previewProject,r.previewVolume,r.previewChapter,r.previewExcerpt,r.previewInsight,r.previewChapterTwo,r.previewExcerptTwo,r.previewChapterThree,r.previewExcerptThree,r.previewBody,r.previewDraft),r.publish));}
 @PatchMapping("/products/{id}/status") public ApiResponse<WebsiteProduct> productStatus(@PathVariable Long id,@RequestBody StatusRequest r){return ApiResponse.ok(service.productStatus(id,r.status));}
 @DeleteMapping("/products/{id}") public ApiResponse<Void> deleteProduct(@PathVariable Long id){service.deleteProduct(id);return ApiResponse.ok(null);}
 @GetMapping("/articles") public ApiResponse<List<WebsiteArticle>> articles(){return ApiResponse.ok(service.adminArticles());}
 @PostMapping("/articles") public ApiResponse<WebsiteArticle> createArticle(@Valid @RequestBody ArticleRequest r){return ApiResponse.ok(publish(service.createArticle(r.slug,r.category,r.title,r.excerpt,r.coverUrl,r.content,r.seoTitle,r.seoDescription),r.publish));}
 @PutMapping("/articles/{id}") public ApiResponse<WebsiteArticle> updateArticle(@PathVariable Long id,@Valid @RequestBody ArticleRequest r){return ApiResponse.ok(publish(service.updateArticle(id,r.category,r.title,r.excerpt,r.coverUrl,r.content,r.seoTitle,r.seoDescription),r.publish));}
 @PatchMapping("/articles/{id}/status") public ApiResponse<WebsiteArticle> articleStatus(@PathVariable Long id,@RequestBody StatusRequest r){return ApiResponse.ok(service.articleStatus(id,r.status));}
 @DeleteMapping("/articles/{id}") public ApiResponse<Void> deleteArticle(@PathVariable Long id){service.deleteArticle(id);return ApiResponse.ok(null);}
 @GetMapping("/pages") public ApiResponse<List<WebsitePage>> pages(){return ApiResponse.ok(service.adminPages());}
 @PostMapping("/pages") public ApiResponse<WebsitePage> createPage(@Valid @RequestBody PageRequest r){return ApiResponse.ok(publish(service.createPage(r.slug,r.title,r.excerpt,r.coverUrl,r.content,r.seoTitle,r.seoDescription,r.sortOrder),r.publish));}
 @PutMapping("/pages/{id}") public ApiResponse<WebsitePage> updatePage(@PathVariable Long id,@Valid @RequestBody PageRequest r){return ApiResponse.ok(publish(service.updatePage(id,r.title,r.excerpt,r.coverUrl,r.content,r.seoTitle,r.seoDescription,r.sortOrder),r.publish));}
 @PatchMapping("/pages/{id}/status") public ApiResponse<WebsitePage> pageStatus(@PathVariable Long id,@RequestBody StatusRequest r){return ApiResponse.ok(service.pageStatus(id,r.status));}
 @DeleteMapping("/pages/{id}") public ApiResponse<Void> deletePage(@PathVariable Long id){service.deletePage(id);return ApiResponse.ok(null);}
 @GetMapping("/media") public ApiResponse<List<WebsiteMedia>> media(){return ApiResponse.ok(media.list());}
 @PostMapping(value="/media",consumes="multipart/form-data") public ApiResponse<WebsiteMedia> uploadMedia(@RequestPart("file") MultipartFile file,@RequestParam(required=false) String title,@RequestParam(required=false) String altText,@RequestParam(required=false) String description){return ApiResponse.ok(media.upload(file,title,altText,description));}
 @PutMapping("/media/{id}") public ApiResponse<WebsiteMedia> updateMedia(@PathVariable Long id,@RequestBody MediaRequest r){return ApiResponse.ok(media.update(id,r.title,r.altText,r.description));}
 @DeleteMapping("/media/{id}") public ApiResponse<Void> deleteMedia(@PathVariable Long id){media.delete(id);return ApiResponse.ok(null);}
 @GetMapping("/leads") public ApiResponse<List<WebsiteLead>> leads(){return ApiResponse.ok(service.adminLeads());}
 @PatchMapping("/leads/{id}/status") public ApiResponse<WebsiteLead> leadStatus(@PathVariable Long id,@RequestBody LeadStatusRequest r){return ApiResponse.ok(service.leadStatus(id,r.status));}
 @GetMapping("/site") public ApiResponse<WebsiteSiteSettings> site(){return ApiResponse.ok(service.siteSettings());}
 @PutMapping("/site") public ApiResponse<WebsiteSiteSettings> updateSite(@Valid @RequestBody WebsiteSiteSettings r){return ApiResponse.ok(service.updateSiteSettings(r));}
 private WebsiteProduct publish(WebsiteProduct p,boolean publish){return publish?service.productStatus(p.getId(),ContentStatus.PUBLISHED):p;}
 private WebsiteArticle publish(WebsiteArticle p,boolean publish){return publish?service.articleStatus(p.getId(),ContentStatus.PUBLISHED):p;}
 private WebsitePage publish(WebsitePage p,boolean publish){return publish?service.pageStatus(p.getId(),ContentStatus.PUBLISHED):p;}
 public static class ProductRequest {@NotBlank @Size(max=80) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*",message="页面地址只能包含小写字母、数字和连字符") public String slug; public boolean publish; public String appId; @NotBlank public String name; @NotBlank public String tagline; public String summary,accentColor,coverUrl,downloadUrl,downloadJson,purchaseUrl,content,mediaJson,previewProject,previewVolume,previewChapter,previewExcerpt,previewInsight,previewChapterTwo,previewExcerptTwo,previewChapterThree,previewExcerptThree,previewBody,previewDraft; public Integer sortOrder;}
 public static class ArticleRequest {@NotBlank @Size(max=80) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*",message="页面地址只能包含小写字母、数字和连字符") public String slug; public boolean publish; @NotBlank public String category; @NotBlank public String title; public String excerpt,coverUrl,content,seoTitle,seoDescription;}
 public static class PageRequest {@NotBlank @Size(max=80) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*",message="页面地址只能包含小写字母、数字和连字符") public String slug; public boolean publish; @NotBlank public String title; public String excerpt,coverUrl,content,seoTitle,seoDescription; public Integer sortOrder;}
 public static class MediaRequest {public String title,altText,description;}
 public static class StatusRequest {@NotNull public ContentStatus status;} public static class LeadStatusRequest {@NotBlank public String status;}
}
