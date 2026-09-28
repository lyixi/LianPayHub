package com.lianpayhub.bootstrap;

import com.lianpayhub.domain.website.*;
import com.lianpayhub.repository.*;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WebsiteContentInitializer implements ApplicationRunner {
    private final WebsiteProductRepository products; private final WebsiteArticleRepository articles;
    private final WebsitePageRepository pages; private final WebsiteSiteSettingsRepository settings;
    public WebsiteContentInitializer(WebsiteProductRepository products,WebsiteArticleRepository articles,WebsitePageRepository pages,WebsiteSiteSettingsRepository settings){this.products=products;this.articles=articles;this.pages=pages;this.settings=settings;}

    @Override @Transactional public void run(ApplicationArguments args){
        if(settings.count()==0)settings.save(defaultSettings());
        seedPages();
        if(products.count()==0)seedProducts();
        products.findBySlug("writing-world").ifPresent(p->p.initializePreviewDefaults("北境来信 · 长篇项目","卷一 · 风从海上来","没有寄出的信","雨是在傍晚停的。积水沿着屋檐落下，像一段迟迟不肯结束的省略号。","木盒中的信与第三章出现的旧邮戳形成呼应。","旧站台","末班车离开以后，站台只剩风穿过长椅。","漫长的回声","山谷把那个名字送回来，一次比一次更轻。","林岑拆开抽屉最深处的木盒，里面只有一封没有地址的信，纸页边缘已经泛黄。","他把信放在灯下，终于看清第一行字"));
        if(articles.count()==0){WebsiteArticle a=new WebsiteArticle("welcome-to-new-lyixi","动态","新的官网，新的产品入口","我们正在把散落的产品、资源和服务整理为更清晰的体验。",null,"这里会持续更新产品进展、使用技巧与团队动态。","新的官网，新的产品入口","懒一夕产品与动态的统一入口。");a.changeStatus(ContentStatus.PUBLISHED);articles.save(a);}
    }

    private WebsiteSiteSettings defaultSettings(){WebsiteSiteSettings s=new WebsiteSiteSettings(1L);s.update("懒一夕智能科技","LYIXI INTELLIGENCE","杭州软件产品团队","让复杂留在系统里","把效率还给每一个人。","杭州懒一夕智能科技有限公司成立于 2020 年，持续为创作者、直播运营与高效工作者研发软件产品。","围绕真实需求，持续构建有用的软件。","从智能写作、直播互动到桌面效率，每款产品独立解决问题，也共享稳定的技术基础。","产品各自生长，能力统一沉淀。","我们覆盖人工智能应用、数字文化创意、物联网、数据服务与专业软件开发，让新产品更快抵达用户。","产品进展与公司动态","懒到极致，是把复杂交给技术。","杭州懒一夕智能科技有限公司成立于 2020 年，总部位于杭州余杭。公司从新型写作集成环境出发，逐步拓展到网络直播、影音媒体、数据统计与办公工具等软件领域。","一起做一件真正有用的事。","产品合作、技术接入或加入团队，欢迎与我们联系。","wxy@iwxyi.com","持续更新","6 年","writing-world","杭州余杭 · 2020 至今","后台发布后自动统计","专注软件产品研发","合作与能力接入","LYIXI CORE","统一产品中枢","Product · Service · AI","产品研发","从需求、设计到交付","稳定服务","数据、同步与业务基础设施","开放能力","为自有软件提供统一 API","敢于创新","敢闯敢试，用技术为用户减少重复工作。","坚守诚信","重视用户信任与隐私，长期维护每一款仍在使用的产品。","追求卓越","以稳定、可靠和持续改进作为产品质量标准。","CONTACT LYIXI","© 2026 杭州懒一夕智能科技有限公司","浙ICP备17046339号-9");return s;}

    private void seedPages(){
        if(!pages.existsBySlug("about")){WebsitePage p=new WebsitePage("about","关于懒一夕","一家从写作工具出发，持续研发创作、直播与效率软件的杭州科技公司。",null,"<h2>让技术承担复杂，让人保持专注</h2><p>杭州懒一夕智能科技有限公司成立于 2020 年，最初致力于研发新型写作集成环境，提高作家的创作效率。我们以“懒到极致是生产力”为品牌主张，相信真正有价值的软件，应当主动承担重复和复杂的工作。</p><h2>从创作工具走向多元软件产品</h2><p>团队核心成员来自北京大学信息技术高等研究院。业务覆盖文学创作、网络直播、聊天通讯、影音媒体、数据统计与办公工具等软件领域，并持续探索人工智能应用、数字文化创意、物联网和互联网数据服务。</p><h2>扎根杭州，长期构建</h2><p>公司总部位于杭州余杭。我们坚持实干、创新和以信立业，重视产品稳定性、用户隐私与长期维护，希望用持续迭代的软件，为用户创造真实而可感知的效率。</p>","关于我们｜杭州懒一夕智能科技有限公司","了解懒一夕智能科技的发展、团队背景、业务领域与产品理念。",1);p.changeStatus(ContentStatus.PUBLISHED);pages.save(p);}
        if(!pages.existsBySlug("culture")){WebsitePage p=new WebsitePage("culture","企业文化","创新、诚信、关爱与卓越，是我们长期构建产品的共同标准。",null,"<h2>敢于创新</h2><p>敢闯敢试、敢想敢做。创新不是装饰性的口号，而是我们理解需求、设计产品和解决问题的方式。</p><h2>坚守诚信</h2><p>尊重每一位用户的信任，保护用户隐私，对承诺负责，对仍在使用的产品保持长期维护。</p><h2>关爱团队</h2><p>员工是公司持续发展的核心。我们鼓励成员增长能力、分享成果，在坦诚协作中共同创造长期价值。</p><h2>追求卓越</h2><p>对产品质量、工程稳定性和使用体验保持高标准，让每一次更新都比上一次更可靠。</p><h2>开放合作</h2><p>尊重不同文化和专业背景的融合，欢迎产品、技术与内容领域的伙伴一起探索新的可能。</p>","企业文化｜懒一夕智能科技","懒一夕智能科技关于创新、诚信、团队、卓越与开放合作的价值观。",2);p.changeStatus(ContentStatus.PUBLISHED);pages.save(p);}
    }

    private void seedProducts(){WebsiteProduct p=new WebsiteProduct("writing-world",null,"写作天下","网文作家集成写作环境","面向网络文学作者的智能写作软件，让资料、章节和创作辅助进入同一条工作流。","#F06A3A",null,null,null,"<p>写作天下从真实的长篇创作流程出发，帮助作者管理灵感、资料与章节，在需要时提供智能辅助。</p>",1);p.changeStatus(ContentStatus.PUBLISHED);products.save(p);WebsiteProduct q=new WebsiteProduct("floating-universe",null,"悬浮宇宙","桌面悬浮效率工具","可放置文件、网址与文字的无限桌面画布，在鼠标贴近屏幕顶边时快速呼出。","#2D78E8",null,null,null,"<p>悬浮宇宙是一款桌面悬浮窗软件，可承载文件、网址和文字等内容，以轻量交互快速呼出常用信息。</p>",2);q.changeStatus(ContentStatus.PUBLISHED);products.save(q);}
}
