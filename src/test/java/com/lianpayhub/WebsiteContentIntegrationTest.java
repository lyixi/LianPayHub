package com.lianpayhub;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MySqlIntegrationTest
@SpringBootTest(properties={"spring.main.banner-mode=off","logging.level.root=WARN","lianpayhub.security.jwt-secret=website-test-secret","lianpayhub.admin.default-username=admin","lianpayhub.admin.default-password=admin123456"})
@AutoConfigureMockMvc
class WebsiteContentIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 @Test void websiteContentLifecycleShouldProtectDrafts() throws Exception {
  mvc.perform(get("/")).andExpect(status().isOk()); mvc.perform(get("/official-ui/app.js")).andExpect(status().isOk()); mvc.perform(get("/admin/")).andExpect(status().isOk()); mvc.perform(get("/console/")).andExpect(status().isOk());
  mvc.perform(get("/api/site/v1/pages")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].slug").value("about"));
  mvc.perform(get("/about")).andExpect(status().isOk()).andExpect(content().string(containsString("关于我们｜杭州懒一夕智能科技有限公司"))).andExpect(content().string(containsString("seo-page-fallback")));
  MvcResult login=mvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\",\"password\":\"admin123456\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andReturn(); String token=mapper.readTree(login.getResponse().getContentAsString()).path("data").path("token").asText();
  mvc.perform(get("/admin/website/pages").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data[1].slug").value("culture"));
  for (String kind : new String[]{"products", "articles", "pages"}) {
   String slug="one-click-"+kind;
   com.fasterxml.jackson.databind.node.ObjectNode payload=mapper.createObjectNode().put("slug",slug).put("name","直接发布产品").put("tagline","发布验证").put("title","直接发布内容").put("category","动态").put("content","直接发布正文").put("publish",true);
   if (kind.equals("products")) payload.put("downloadUrl","/downloads/test.zip").put("downloadJson","[{\"url\":\"https://example.com/app.zip\",\"title\":\"安全下载\"},{\"url\":\"javascript:alert(1)\",\"title\":\"危险下载\"}]");
   JsonNode saved=mapper.readTree(mvc.perform(post("/admin/website/"+kind).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
    .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PUBLISHED")).andReturn().getResponse().getContentAsString()).path("data");
   String publicPath=kind.equals("products")?"/product/":kind.equals("articles")?"/news/":"/page/";
   mvc.perform(get(publicPath+slug)).andExpect(status().isOk()).andExpect(content().string(containsString("直接发布正文")));
   if(kind.equals("products")) {
    Document downloadPage=Jsoup.parse(mvc.perform(get(publicPath+slug)).andReturn().getResponse().getContentAsString());
    assertEquals(1,downloadPage.select("#seo-content a[href='/downloads/test.zip']").size());
    assertEquals(1,downloadPage.select("#seo-content a[href='https://example.com/app.zip']").size());
    assertFalse(downloadPage.selectFirst("#seo-content").html().contains("javascript:"));
   }
   mvc.perform(patch("/admin/website/"+kind+"/"+saved.path("id").asLong()+"/status").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DRAFT\"}"));
   payload.put("content","重新发布的正文");
   mvc.perform(put("/admin/website/"+kind+"/"+saved.path("id").asLong()).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
    .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PUBLISHED"));
   mvc.perform(get(publicPath+slug)).andExpect(status().isOk()).andExpect(content().string(containsString("重新发布的正文")));
   for(String invalid:new String[]{"bad/path","bad?query","Bad Slug",new String(new char[81]).replace('\0','a')}) {
    payload.put("slug",invalid);
    mvc.perform(post("/admin/website/"+kind).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
     .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(0)));
   }
  }
  MvcResult created=mvc.perform(post("/admin/website/articles").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"website-test\",\"category\":\"动态\",\"title\":\"官网测试\",\"content\":\"内容\"}" )).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DRAFT")).andReturn(); long id=mapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
  mvc.perform(get("/api/site/v1/articles/website-test")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
  mvc.perform(get("/news/website-test")).andExpect(status().isNotFound()).andExpect(header().string("X-Robots-Tag","noindex"));
  mvc.perform(patch("/admin/website/articles/{id}/status",id).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PUBLISHED"));
  mvc.perform(get("/api/site/v1/articles/website-test")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.title").value("官网测试"));
  mvc.perform(get("/news/website-test")).andExpect(status().isOk())
    .andExpect(content().string(containsString("<title>官网测试</title>")))
    .andExpect(content().string(containsString("https://lyixi.com/news/website-test")))
    .andExpect(content().string(containsString("\"@type\":\"Article\"")));
 mvc.perform(post("/api/site/v1/leads").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"访客\",\"contact\":\"test@example.com\",\"message\":\"合作咨询\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
  mvc.perform(get("/admin/website/leads").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].name").value("访客"));

  JsonNode currentSite=mapper.readTree(mvc.perform(get("/admin/website/site").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("data");
  ((com.fasterxml.jackson.databind.node.ObjectNode)currentSite).put("heroTitle","由后台驱动的企业官网").put("systemCoreTitle","统一能力测试").put("principleOneTitle","持续演进");
  mvc.perform(put("/admin/website/site").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(currentSite)))
    .andExpect(status().isOk()).andExpect(jsonPath("$.data.heroTitle").value("由后台驱动的企业官网"));
  mvc.perform(get("/api/site/v1/site")).andExpect(status().isOk()).andExpect(jsonPath("$.data.systemCoreTitle").value("统一能力测试")).andExpect(jsonPath("$.data.principleOneTitle").value("持续演进"));

  String productJson="{\"slug\":\"cms-preview-test\",\"appId\":\"internal-app-secret\",\"name\":\"内容驱动产品\",\"tagline\":\"验证产品示例\",\"content\":\"<p>可被搜索引擎理解的产品正文。</p>\",\"previewProject\":\"可编辑项目\",\"previewChapter\":\"第一幕\",\"previewExcerpt\":\"第一幕正文\",\"previewChapterTwo\":\"第二幕\",\"previewExcerptTwo\":\"第二幕正文\",\"previewChapterThree\":\"第三幕\",\"previewExcerptThree\":\"第三幕正文\",\"previewBody\":\"后台维护的补充段落\",\"previewDraft\":\"后台维护的输入内容\",\"previewInsight\":\"后台维护的智能提示\"}";
  com.fasterxml.jackson.databind.node.ObjectNode productPayload=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(productJson);
  StringBuilder longContent=new StringBuilder("<p>可被搜索引擎理解的产品正文。</p>");
  for(int i=0;i<400;i++)longContent.append("<p>完整产品说明文本。</p>");
  longContent.append("<h2>正文末尾标记</h2><table><tr><td>平台信息</td></tr></table><a href='/product'>产品目录</a><script>window.unsafeSeo=true</script><img src='/brand/favicon.png' onerror='alert(1)'><a href='javascript:alert(1)'>危险链接</a>");
  productPayload.put("content",longContent.toString());
  MvcResult product=mvc.perform(post("/admin/website/products").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(productPayload))).andExpect(status().isOk()).andExpect(jsonPath("$.data.appId").value("internal-app-secret")).andExpect(jsonPath("$.data.previewChapterTwo").value("第二幕")).andReturn();
  long productId=mapper.readTree(product.getResponse().getContentAsString()).path("data").path("id").asLong();
  mvc.perform(get("/product/cms-preview-test")).andExpect(status().isNotFound()).andExpect(header().string("X-Robots-Tag","noindex"));
  mvc.perform(get("/sitemap.xml")).andExpect(content().string(org.hamcrest.Matchers.not(containsString("/product/cms-preview-test"))));
  mvc.perform(patch("/admin/website/products/{id}/status",productId).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/site/v1/products/cms-preview-test")).andExpect(status().isOk()).andExpect(jsonPath("$.data.appId").doesNotExist()).andExpect(jsonPath("$.data.previewExcerptThree").value("第三幕正文")).andExpect(jsonPath("$.data.previewInsight").value("后台维护的智能提示"));
  mvc.perform(get("/product/cms-preview-test")).andExpect(status().isOk())
    .andExpect(content().string(containsString("<title>内容驱动产品｜验证产品示例</title>")))
    .andExpect(content().string(containsString("https://lyixi.com/product/cms-preview-test")))
    .andExpect(content().string(containsString("\"@type\":\"SoftwareApplication\"")))
    .andExpect(content().string(containsString("seo-product-fallback")))
    .andExpect(content().string(containsString("可被搜索引擎理解的产品正文。")))
    .andExpect(content().string(org.hamcrest.Matchers.not(containsString("internal-app-secret"))));
  Document productHtml=Jsoup.parse(mvc.perform(get("/product/cms-preview-test")).andReturn().getResponse().getContentAsString());
  assertEquals(1,productHtml.select("title").size());
  assertEquals(1,productHtml.select("link[rel=canonical]").size());
  assertEquals("内容驱动产品",productHtml.selectFirst("#seo-content h1").text());
  assertTrue(productHtml.selectFirst("#seo-content").text().contains("可被搜索引擎理解的产品正文。"));
  assertTrue(productHtml.selectFirst("#seo-content").text().contains("正文末尾标记"));
  assertEquals("平台信息",productHtml.selectFirst("#seo-content table td").text());
  assertFalse(productHtml.selectFirst("#seo-content").html().contains("window.unsafeSeo"));
  assertTrue(productHtml.select("#seo-content script, #seo-content [onerror], #seo-content a[href^=javascript:]").isEmpty());
  assertTrue(productHtml.select("noscript #seo-content, noscript #seo-product-fallback").isEmpty());
  for(String directory:new String[]{"/","/product"}) {
    Document directoryHtml=Jsoup.parse(mvc.perform(get(directory)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertEquals(1,directoryHtml.select("#seo-content a[href='/product/cms-preview-test']").size());
  }
  mvc.perform(get("/robots.txt")).andExpect(status().isOk()).andExpect(content().string(containsString("Sitemap: https://lyixi.com/sitemap.xml")))
    .andExpect(content().string(containsString("Allow: /api/site/v1/")));
  mvc.perform(get("/sitemap.xml")).andExpect(status().isOk()).andExpect(content().string(containsString("https://lyixi.com/product/cms-preview-test")));
  for(String missing:new String[]{"/product/missing-seo-product","/news/missing-seo-article","/page/missing-seo-page"}) {
    mvc.perform(get(missing)).andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
      .andExpect(header().string("X-Robots-Tag","noindex"));
  }
  mvc.perform(patch("/admin/website/products/{id}/status",productId).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ARCHIVED\"}")).andExpect(status().isOk());
  mvc.perform(get("/product/cms-preview-test")).andExpect(status().isNotFound());
  mvc.perform(get("/sitemap.xml")).andExpect(content().string(org.hamcrest.Matchers.not(containsString("/product/cms-preview-test"))));

  byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
  MockMultipartFile image=new MockMultipartFile("file","website-test.png","image/png",png);
  MvcResult uploaded=mvc.perform(multipart("/admin/website/media").file(image).param("title","官网测试图片").param("altText","一像素测试图").header("Authorization","Bearer "+token))
    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.title").value("官网测试图片")).andReturn();
  JsonNode media=mapper.readTree(uploaded.getResponse().getContentAsString()).path("data");long mediaId=media.path("id").asLong();String mediaUrl=media.path("url").asText();
  mvc.perform(get(mediaUrl)).andExpect(status().isOk());
  mvc.perform(put("/admin/website/media/{id}",mediaId).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"已编辑图片\",\"altText\":\"替代文本\",\"description\":\"媒体说明\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.data.altText").value("替代文本"));
  mvc.perform(delete("/admin/website/media/{id}",mediaId).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
 }
}
