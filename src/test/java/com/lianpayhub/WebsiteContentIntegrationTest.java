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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:website-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect","spring.main.banner-mode=off","logging.level.root=WARN","lianpayhub.security.jwt-secret=website-test-secret","lianpayhub.admin.default-username=admin","lianpayhub.admin.default-password=admin123456"})
@AutoConfigureMockMvc
class WebsiteContentIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 @Test void websiteContentLifecycleShouldProtectDrafts() throws Exception {
  mvc.perform(get("/")).andExpect(status().isOk()); mvc.perform(get("/official-ui/app.js")).andExpect(status().isOk()); mvc.perform(get("/admin/")).andExpect(status().isOk()); mvc.perform(get("/console/")).andExpect(status().isOk());
  mvc.perform(get("/api/site/v1/pages")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].slug").value("about"));
  mvc.perform(get("/about")).andExpect(status().isOk()).andExpect(content().string(containsString("关于我们｜杭州懒一夕智能科技有限公司"))).andExpect(content().string(containsString("seo-page-fallback")));
  MvcResult login=mvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\",\"password\":\"admin123456\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andReturn(); String token=mapper.readTree(login.getResponse().getContentAsString()).path("data").path("token").asText();
  mvc.perform(get("/admin/website/pages").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data[1].slug").value("culture"));
  MvcResult created=mvc.perform(post("/admin/website/articles").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"website-test\",\"category\":\"动态\",\"title\":\"官网测试\",\"content\":\"内容\"}" )).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DRAFT")).andReturn(); long id=mapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
  mvc.perform(get("/api/site/v1/articles/website-test")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
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
  MvcResult product=mvc.perform(post("/admin/website/products").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(productJson)).andExpect(status().isOk()).andExpect(jsonPath("$.data.appId").value("internal-app-secret")).andExpect(jsonPath("$.data.previewChapterTwo").value("第二幕")).andReturn();
  long productId=mapper.readTree(product.getResponse().getContentAsString()).path("data").path("id").asLong();
  mvc.perform(patch("/admin/website/products/{id}/status",productId).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/site/v1/products/cms-preview-test")).andExpect(status().isOk()).andExpect(jsonPath("$.data.appId").doesNotExist()).andExpect(jsonPath("$.data.previewExcerptThree").value("第三幕正文")).andExpect(jsonPath("$.data.previewInsight").value("后台维护的智能提示"));
  mvc.perform(get("/product/cms-preview-test")).andExpect(status().isOk())
    .andExpect(content().string(containsString("<title>内容驱动产品｜验证产品示例</title>")))
    .andExpect(content().string(containsString("https://lyixi.com/product/cms-preview-test")))
    .andExpect(content().string(containsString("\"@type\":\"SoftwareApplication\"")))
    .andExpect(content().string(containsString("seo-product-fallback")))
    .andExpect(content().string(containsString("可被搜索引擎理解的产品正文。")))
    .andExpect(content().string(org.hamcrest.Matchers.not(containsString("internal-app-secret"))));
  mvc.perform(get("/robots.txt")).andExpect(status().isOk()).andExpect(content().string(containsString("Sitemap: https://lyixi.com/sitemap.xml")));
  mvc.perform(get("/sitemap.xml")).andExpect(status().isOk()).andExpect(content().string(containsString("https://lyixi.com/product/cms-preview-test")));

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
