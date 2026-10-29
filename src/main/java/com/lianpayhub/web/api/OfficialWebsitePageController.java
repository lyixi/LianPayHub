package com.lianpayhub.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

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
        return seoPage(name + "｜为创作者打造的工具", description, "/", null, "website", organization,
                "<main><h1>" + html(name) + "</h1><p>" + html(description) + "</p>" + directoryContent(false) + directoryContent(true) + "</main>");
    }

    @GetMapping(value = "/product/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> product(@PathVariable String slug) {
        WebsiteProduct product = content.publicProduct(slug);
        String description = text(product.getSummary(), product.getTagline());
        Map<String, Object> software = jsonLd("SoftwareApplication");
        software.put("name", product.getName()); software.put("description", description);
        software.put("applicationCategory", "ProductivityApplication");
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
                news ? "/news" : "/product", null, "website", null,
                "<main><h1>" + (news ? "产品动态" : "产品") + "</h1>" + directoryContent(news) + "</main>");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> pageError(BusinessException exception) {
        int status = exception.errorCode().code();
        String title = status == 404 ? "页面不存在" : "页面暂时无法访问";
        return ResponseEntity.status(status).contentType(HTML_UTF8).header("X-Robots-Tag", "noindex")
                .body("<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\"><meta name=\"robots\" content=\"noindex\"><title>"
                        + title + "</title></head><body><main><h1>" + title + "</h1><a href=\"/\">返回首页</a></main></body></html>");
    }

    @GetMapping("/contact") public String contact() { return "redirect:/#contact"; }
    @GetMapping({"/admin", "/admin/"}) public String websiteAdmin() { return "forward:/website-admin/index.html"; }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String body = "User-agent: *\nAllow: /\nAllow: /api/site/v1/\nDisallow: /admin\nDisallow: /console\nDisallow: /api/\nSitemap: " + publicBaseUrl + "/sitemap.xml\n";
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

    // Select the shell before body parsing; API latency must not expose the text fallback.
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
        head.append("<script>document.documentElement.setAttribute('data-site-js','true')</script>");
        head.append("<style>html:not([data-site-js]) body > :not(#seo-content):not(script),"
                + "html[data-site-failed] body > :not(#seo-content):not(script){display:none!important}"
                + "html[data-site-js]:not([data-site-failed]) #seo-content{display:none}"
                + "#seo-content{max-width:1120px;margin:auto;padding:48px 24px;line-height:1.8;overflow-wrap:anywhere}"
                + "#seo-content img{max-width:100%;height:auto}#seo-content table{display:block;overflow:auto}"
                + "#seo-content a{text-decoration:underline}</style>");
        String html = page.replace("</head>", head + "</head>");
        if (hasText(fallback)) html = html.replace("</body>", "<div id=\"seo-content\"><nav><a href=\"/\">首页</a> · <a href=\"/product\">产品</a> · <a href=\"/news\">动态</a></nav>" + fallback + "</div></body>");
        return ResponseEntity.ok().contentType(HTML_UTF8).body(html);
    }

    private String websiteShell() {
        try { return StreamUtils.copyToString(new ClassPathResource("static/official-ui/index.html").getInputStream(), StandardCharsets.UTF_8); }
        catch (IOException exception) { throw new IllegalStateException("官网静态页面不存在，请先构建并部署前端", exception); }
    }
    private Map<String, Object> jsonLd(String type) { Map<String, Object> data = new LinkedHashMap<>(); data.put("@context", "https://schema.org"); data.put("@type", type); return data; }
    private Map<String, Object> organizationReference() { Map<String, Object> data = new LinkedHashMap<>(); data.put("@type", "Organization"); data.put("name", "懒一夕智能科技"); data.put("url", publicBaseUrl); return data; }
    private String productFallback(WebsiteProduct product, String description) {
        StringBuilder fallback = new StringBuilder("<main id=\"seo-product-fallback\"><article><h1>")
                .append(html(product.getName())).append("</h1><p>").append(html(description)).append("</p>");
        if (hasText(product.getCoverUrl())) fallback.append("<img src=\"").append(html(absoluteUrl(product.getCoverUrl()))).append("\" alt=\"").append(html(product.getName())).append(" 产品界面\"/>");
        appendTextSection(fallback, "产品介绍", product.getContent());
        Map<String, String> downloads = new LinkedHashMap<>();
        if (hasText(product.getDownloadJson())) {
            try {
                com.fasterxml.jackson.databind.JsonNode assets = objectMapper.readTree(product.getDownloadJson());
                if (assets.isArray()) for (com.fasterxml.jackson.databind.JsonNode asset : assets) {
                    String url = asset.path("url").asText("");
                    if (safeDownloadUrl(url)) downloads.put(url, text(asset.path("title").asText(""), asset.path("fileName").asText("下载安装包")));
                }
            } catch (JsonProcessingException ignored) { /* The legacy download URL remains usable. */ }
        }
        if (safeDownloadUrl(product.getDownloadUrl())) downloads.putIfAbsent(product.getDownloadUrl(), "下载安装包");
        if (!downloads.isEmpty()) {
            fallback.append("<section><h2>产品下载</h2><ul>");
            downloads.forEach((url, label) -> fallback.append("<li><a href=\"").append(html(url)).append("\" rel=\"noopener noreferrer\">").append(html(label)).append("</a></li>"));
            fallback.append("</ul></section>");
        }
        return fallback.append("</article></main>").toString();
    }
    private boolean safeDownloadUrl(String url) {
        if (!hasText(url)) return false;
        try {
            java.net.URI uri = new java.net.URI(url);
            if (url.startsWith("/") && !url.startsWith("//") && uri.getRawAuthority() == null) return true;
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) && uri.getHost() != null;
        } catch (java.net.URISyntaxException exception) { return false; }
    }
    private String articleFallback(WebsiteArticle article, String description) {
        StringBuilder fallback = new StringBuilder("<main id=\"seo-article-fallback\"><article><h1>")
                .append(html(article.getTitle())).append("</h1><p>").append(html(description)).append("</p>");
        if (article.getPublishedAt() != null) fallback.append("<time datetime=\"").append(article.getPublishedAt()).append("\">").append(article.getPublishedAt().toLocalDate()).append("</time>");
        if (hasText(article.getCoverUrl())) fallback.append("<img src=\"").append(html(absoluteUrl(article.getCoverUrl()))).append("\" alt=\"").append(html(article.getTitle())).append("\"/>");
        appendTextSection(fallback, "正文", article.getContent());
        return fallback.append("</article></main>").toString();
    }
    private String pageFallback(WebsitePage page, String description) {
        StringBuilder fallback = new StringBuilder("<main id=\"seo-page-fallback\"><article><h1>")
                .append(html(page.getTitle())).append("</h1><p>").append(html(description)).append("</p>");
        appendTextSection(fallback, "页面内容", page.getContent());
        return fallback.append("</article></main>").toString();
    }

    private String directoryContent(boolean news) {
        StringBuilder links = new StringBuilder("<section><h2>").append(news ? "产品动态" : "全部产品").append("</h2><ul>");
        if (news) {
            for (WebsiteArticle article : content.publicArticles(null)) appendDirectoryItem(links, "/news/" + article.getSlug(), article.getTitle(), article.getExcerpt());
        } else {
            for (WebsiteProduct product : content.publicProducts()) appendDirectoryItem(links, "/product/" + product.getSlug(), product.getName(), text(product.getSummary(), product.getTagline()));
        }
        return links.append("</ul></section>").toString();
    }

    private void appendDirectoryItem(StringBuilder target, String path, String title, String description) {
        target.append("<li><a href=\"").append(html(path)).append("\">").append(html(title)).append("</a><p>").append(html(description)).append("</p></li>");
    }

    private void appendTextSection(StringBuilder target, String heading, String richText) {
        if (!hasText(richText)) return;
        String markup = richText.matches("(?s).*<[a-zA-Z][^>]*>.*") ? richText : "<p>" + html(richText).replace("\n", "<br>") + "</p>";
        String safe = Jsoup.clean(markup, publicBaseUrl, Safelist.basicWithImages()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "table", "thead", "tbody", "tfoot", "tr", "th", "td", "hr")
                .addAttributes("th", "colspan", "rowspan").addAttributes("td", "colspan", "rowspan")
                .addEnforcedAttribute("a", "rel", "noopener noreferrer"));
        target.append("<section><h2>").append(html(heading)).append("</h2>").append(safe).append("</section>");
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
