package com.lianpayhub.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.domain.website.WebsiteArticle;
import com.lianpayhub.domain.website.WebsitePage;
import com.lianpayhub.domain.website.WebsiteProduct;
import com.lianpayhub.domain.website.WebsiteSiteSettings;
import com.lianpayhub.service.website.WebsiteContentService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OfficialWebsitePageController {
    private static final DateTimeFormatter SITEMAP_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final MediaType HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);
    private final WebsiteContentService content;
    private final ObjectMapper objectMapper;
    private final String publicBaseUrl;

    public OfficialWebsitePageController(WebsiteContentService content, ObjectMapper objectMapper,
                                         @Value("${lianpayhub.website.public-base-url:https://lyixi.com}") String publicBaseUrl) {
        this.content = content;
        this.objectMapper = objectMapper;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home() {
        WebsiteSiteSettings site = content.siteSettings();
        String name = text(site.getName(), "懒一夕智能科技");
        String description = text(site.getHeroDescription(), "写作天下、神奇弹幕，以及更多为创作者和高效工作者打造的软件。");
        Map<String, Object> organization = jsonLd("Organization");
        organization.put("name", name); organization.put("url", publicBaseUrl); organization.put("email", site.getEmail());
        return seoPage(name + "｜为创作者打造的工具", description, "/", null, "website", organization);
    }

    @GetMapping(value = "/product/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> product(@PathVariable String slug) {
        WebsiteProduct product = content.publicProduct(slug);
        String description = text(product.getSummary(), product.getTagline());
        Map<String, Object> software = jsonLd("SoftwareApplication");
        software.put("name", product.getName()); software.put("description", description);
        software.put("applicationCategory", "ProductivityApplication"); software.put("operatingSystem", "Windows, macOS, Android");
        software.put("url", absoluteUrl("/product/" + product.getSlug()));
        if (hasText(product.getCoverUrl())) software.put("image", absoluteUrl(product.getCoverUrl()));
        return seoPage(product.getName() + "｜" + product.getTagline(), description, "/product/" + product.getSlug(), product.getCoverUrl(), "website", software,
                productFallback(product, description));
    }

    @GetMapping(value = "/news/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> article(@PathVariable String slug) {
        WebsiteArticle article = content.publicArticle(slug);
        String title = text(article.getSeoTitle(), article.getTitle());
        String description = text(article.getSeoDescription(), article.getExcerpt());
        Map<String, Object> articleData = jsonLd("Article");
        articleData.put("headline", article.getTitle()); articleData.put("description", description);
        articleData.put("datePublished", article.getPublishedAt()); articleData.put("dateModified", article.getUpdatedAt());
        articleData.put("mainEntityOfPage", absoluteUrl("/news/" + article.getSlug()));
        articleData.put("author", organizationReference()); articleData.put("publisher", organizationReference());
        if (hasText(article.getCoverUrl())) articleData.put("image", absoluteUrl(article.getCoverUrl()));
        return seoPage(title, description, "/news/" + article.getSlug(), article.getCoverUrl(), "article", articleData,
                articleFallback(article, description));
    }

    @GetMapping(value = {"/about", "/culture", "/page/{slug}"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page(@PathVariable(required = false) String slug,
                                       javax.servlet.http.HttpServletRequest request) {
        String pageSlug = hasText(slug) ? slug : request.getRequestURI().substring(1);
        WebsitePage page = content.publicPage(pageSlug);
        String title = text(page.getSeoTitle(), page.getTitle() + "｜懒一夕智能科技");
        String description = text(page.getSeoDescription(), page.getExcerpt());
        String publicPath = "about".equals(pageSlug) || "culture".equals(pageSlug) ? "/" + pageSlug : "/page/" + pageSlug;
        Map<String, Object> data = jsonLd("AboutPage");
        data.put("name", page.getTitle()); data.put("description", description);
        data.put("url", absoluteUrl(publicPath)); data.put("isPartOf", organizationReference());
        if (hasText(page.getCoverUrl())) data.put("image", absoluteUrl(page.getCoverUrl()));
        return seoPage(title, description, publicPath, page.getCoverUrl(), "website", data,
                pageFallback(page, description));
    }

    @GetMapping(value = {"/product", "/news"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> directory(javax.servlet.http.HttpServletRequest request) {
        boolean news = request.getRequestURI().startsWith("/news");
        return seoPage(news ? "产品动态｜懒一夕智能科技" : "产品｜懒一夕智能科技",
                news ? "了解懒一夕的产品更新、设计思考与团队动态。" : "探索懒一夕面向创作者、直播运营与高效工作者打造的软件产品。",
                news ? "/news" : "/product", null, "website", null);
    }

    @GetMapping("/contact") public String contact() { return "redirect:/#contact"; }
    @GetMapping({"/admin", "/admin/"}) public String websiteAdmin() { return "forward:/website-admin/index.html"; }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String body = "User-agent: *\nAllow: /\nDisallow: /admin/\nDisallow: /console/\nDisallow: /api/\nSitemap: " + publicBaseUrl + "/sitemap.xml\n";
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        appendUrl(xml, "/", null, "1.0"); appendUrl(xml, "/product", null, "0.8"); appendUrl(xml, "/news", null, "0.8");
        for (WebsiteProduct product : content.publicProducts()) appendUrl(xml, "/product/" + product.getSlug(), product.getUpdatedAt(), "0.9");
        for (WebsiteArticle article : content.publicArticles(null)) appendUrl(xml, "/news/" + article.getSlug(), article.getUpdatedAt(), "0.7");
        for (WebsitePage page : content.publicPages()) appendUrl(xml, "about".equals(page.getSlug()) || "culture".equals(page.getSlug()) ? "/" + page.getSlug() : "/page/" + page.getSlug(), page.getUpdatedAt(), "0.7");
        xml.append("</urlset>");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml.toString());
    }

    @GetMapping(value = "/site.webmanifest", produces = "application/manifest+json")
    public ResponseEntity<String> manifest() {
        return ResponseEntity.ok().contentType(MediaType.valueOf("application/manifest+json")).body(
                "{\"name\":\"懒一夕智能科技\",\"short_name\":\"懒一夕\",\"lang\":\"zh-CN\",\"start_url\":\"/\",\"display\":\"standalone\",\"background_color\":\"#eef4ff\",\"theme_color\":\"#6246d9\",\"icons\":[{\"src\":\"/favicon.svg\",\"sizes\":\"any\",\"type\":\"image/svg+xml\"}]}"
        );
    }

    private ResponseEntity<String> seoPage(String title, String description, String path, String image, String type, Map<String, Object> structuredData) {
        return seoPage(title, description, path, image, type, structuredData, "");
    }

    /**
     * The interactive website is a client application, while this semantic noscript layer gives
     * search crawlers and visitors without JavaScript the actual page subject instead of an empty app shell.
     * It is deliberately plain text: rich text from the CMS is never injected as executable HTML.
     */
    private ResponseEntity<String> seoPage(String title, String description, String path, String image, String type,
                                           Map<String, Object> structuredData, String fallback) {
        String page = websiteShell()
                .replaceAll("(?s)<title>.*?</title>", "")
                .replaceAll("<meta name=\"description\"[^>]*>", "")
                .replaceAll("<link rel=\"canonical\"[^>]*>", "").replaceAll("<meta property=\"og:[^>]*>", "").replaceAll("<meta name=\"twitter:[^>]*>", "");
        String canonical = absoluteUrl(path);
        StringBuilder head = new StringBuilder("<title>").append(html(title)).append("</title><meta name=\"description\" content=\"").append(html(description)).append("\"/><link rel=\"canonical\" href=\"").append(html(canonical)).append("\"/>");
        meta(head, "property", "og:type", type); meta(head, "property", "og:locale", "zh_CN"); meta(head, "property", "og:site_name", "懒一夕智能科技");
        meta(head, "property", "og:title", title); meta(head, "property", "og:description", description); meta(head, "property", "og:url", canonical);
        meta(head, "name", "twitter:card", hasText(image) ? "summary_large_image" : "summary"); meta(head, "name", "twitter:title", title); meta(head, "name", "twitter:description", description);
        if (hasText(image)) { meta(head, "property", "og:image", absoluteUrl(image)); meta(head, "name", "twitter:image", absoluteUrl(image)); }
        if (structuredData != null) head.append("<script type=\"application/ld+json\">").append(json(structuredData)).append("</script>");
        String html = page.replace("</head>", head + "</head>");
        if (hasText(fallback)) html = html.replace("</body>", fallback + "</body>");
        return ResponseEntity.ok().contentType(HTML_UTF8).body(html);
    }

    private String websiteShell() {
        try { return StreamUtils.copyToString(new ClassPathResource("static/official-ui/index.html").getInputStream(), StandardCharsets.UTF_8); }
        catch (IOException exception) { throw new IllegalStateException("官网静态页面不存在，请先构建并部署前端", exception); }
    }
    private Map<String, Object> jsonLd(String type) { Map<String, Object> data = new LinkedHashMap<>(); data.put("@context", "https://schema.org"); data.put("@type", type); return data; }
    private Map<String, Object> organizationReference() { Map<String, Object> data = new LinkedHashMap<>(); data.put("@type", "Organization"); data.put("name", "懒一夕智能科技"); data.put("url", publicBaseUrl); return data; }
    private String productFallback(WebsiteProduct product, String description) {
        StringBuilder fallback = new StringBuilder("<noscript><main id=\"seo-product-fallback\"><article><h1>")
                .append(html(product.getName())).append("</h1><p>").append(html(description)).append("</p>");
        if (hasText(product.getCoverUrl())) fallback.append("<img src=\"").append(html(absoluteUrl(product.getCoverUrl()))).append("\" alt=\"").append(html(product.getName())).append(" 产品界面\"/>");
        appendTextSection(fallback, "产品介绍", product.getContent());
        if (hasText(product.getDownloadJson()) || hasText(product.getDownloadUrl())) fallback.append("<p>此产品提供可下载的安装包，请在页面中选择适合的平台和版本。</p>");
        return fallback.append("</article></main></noscript>").toString();
    }
    private String articleFallback(WebsiteArticle article, String description) {
        StringBuilder fallback = new StringBuilder("<noscript><main id=\"seo-article-fallback\"><article><h1>")
                .append(html(article.getTitle())).append("</h1><p>").append(html(description)).append("</p>");
        if (article.getPublishedAt() != null) fallback.append("<time datetime=\"").append(article.getPublishedAt()).append("\">").append(article.getPublishedAt().toLocalDate()).append("</time>");
        if (hasText(article.getCoverUrl())) fallback.append("<img src=\"").append(html(absoluteUrl(article.getCoverUrl()))).append("\" alt=\"").append(html(article.getTitle())).append("\"/>");
        appendTextSection(fallback, "正文", article.getContent());
        return fallback.append("</article></main></noscript>").toString();
    }
    private String pageFallback(WebsitePage page, String description) {
        StringBuilder fallback = new StringBuilder("<noscript><main id=\"seo-page-fallback\"><article><h1>")
                .append(html(page.getTitle())).append("</h1><p>").append(html(description)).append("</p>");
        appendTextSection(fallback, "页面内容", page.getContent());
        return fallback.append("</article></main></noscript>").toString();
    }
    private void appendTextSection(StringBuilder target, String heading, String richText) {
        String plain = plainText(richText);
        if (hasText(plain)) target.append("<section><h2>").append(html(heading)).append("</h2><p>").append(html(plain)).append("</p></section>");
    }
    private String plainText(String value) {
        if (!hasText(value)) return "";
        String plain = value.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ").replaceAll("(?s)<[^>]*>", " ")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replaceAll("\\s+", " ").trim();
        return plain.length() > 3000 ? plain.substring(0, 3000).trim() + "…" : plain;
    }
    private String json(Map<String, Object> value) { try { return objectMapper.writeValueAsString(value).replace("</", "<\\/"); } catch (JsonProcessingException exception) { throw new IllegalStateException("无法生成结构化数据", exception); } }
    private void meta(StringBuilder target, String key, String name, String value) { target.append("<meta ").append(key).append("=\"").append(html(name)).append("\" content=\"").append(html(value)).append("\"/>"); }
    private void appendUrl(StringBuilder xml, String path, LocalDateTime updatedAt, String priority) { xml.append("<url><loc>").append(xml(absoluteUrl(path))).append("</loc>"); if (updatedAt != null) xml.append("<lastmod>").append(updatedAt.toLocalDate().format(SITEMAP_DATE)).append("</lastmod>"); xml.append("<changefreq>weekly</changefreq><priority>").append(priority).append("</priority></url>"); }
    private String absoluteUrl(String value) { if (!hasText(value)) return null; return value.startsWith("http://") || value.startsWith("https://") ? value : publicBaseUrl + (value.startsWith("/") ? value : "/" + value); }
    private static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
    private static String text(String value, String fallback) { return hasText(value) ? value.trim() : fallback; }
    private static String html(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); }
    private static String xml(String value) { return html(value).replace("'", "&apos;"); }
}
