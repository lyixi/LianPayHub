package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "website_site_settings")
public class WebsiteSiteSettings extends BaseEntity {
    @Id private Long id;
    @Column(nullable=false,length=80) private String name;
    @Column(name="english_name",length=120) private String englishName;
    @Column(name="hero_eyebrow",length=120) private String heroEyebrow;
    @Column(name="hero_title",length=160) private String heroTitle;
    @Column(name="hero_accent_title",length=160) private String heroAccentTitle;
    @Column(name="hero_description",length=600) private String heroDescription;
    @Column(name="hero_location",length=120) private String heroLocation;
    @Column(name="product_count_description",length=120) private String productCountDescription;
    @Column(name="years_description",length=120) private String yearsDescription;
    @Column(name="products_heading",length=200) private String productsHeading;
    @Column(name="products_intro",length=600) private String productsIntro;
    @Column(name="system_heading",length=200) private String systemHeading;
    @Column(name="system_intro",length=600) private String systemIntro;
    @Column(name="system_cta_label",length=120) private String systemCtaLabel;
    @Column(name="system_core_eyebrow",length=80) private String systemCoreEyebrow;
    @Column(name="system_core_title",length=120) private String systemCoreTitle;
    @Column(name="system_core_meta",length=160) private String systemCoreMeta;
    @Column(name="system_node_one_title",length=80) private String systemNodeOneTitle;
    @Column(name="system_node_one_meta",length=80) private String systemNodeOneMeta;
    @Column(name="system_node_two_title",length=80) private String systemNodeTwoTitle;
    @Column(name="system_node_two_meta",length=80) private String systemNodeTwoMeta;
    @Column(name="system_node_three_title",length=80) private String systemNodeThreeTitle;
    @Column(name="system_node_three_meta",length=80) private String systemNodeThreeMeta;
    @Column(name="journal_heading",length=200) private String journalHeading;
    @Column(name="company_heading",length=240) private String companyHeading;
    @Column(name="company_description",length=800) private String companyDescription;
    @Column(name="principle_one_title",length=80) private String principleOneTitle;
    @Column(name="principle_one_description",length=240) private String principleOneDescription;
    @Column(name="principle_two_title",length=80) private String principleTwoTitle;
    @Column(name="principle_two_description",length=240) private String principleTwoDescription;
    @Column(name="principle_three_title",length=80) private String principleThreeTitle;
    @Column(name="principle_three_description",length=240) private String principleThreeDescription;
    @Column(name="contact_heading",length=200) private String contactHeading;
    @Column(name="contact_description",length=600) private String contactDescription;
    @Column(name="contact_eyebrow",length=120) private String contactEyebrow;
    @Column(length=160) private String email;
    @Column(name="product_count_label",length=40) private String productCountLabel;
    @Column(name="years_label",length=40) private String yearsLabel;
    @Column(name="featured_product_slug",length=80) private String featuredProductSlug;
    @Column(name="footer_copyright",length=160) private String footerCopyright;
    @Column(name="icp_number",length=80) private String icpNumber;

    protected WebsiteSiteSettings() {}

    public WebsiteSiteSettings(Long id) { this.id = id; }

    public void update(String name,String englishName,String heroEyebrow,String heroTitle,String heroAccentTitle,
                       String heroDescription,String productsHeading,String productsIntro,String systemHeading,
                       String systemIntro,String journalHeading,String companyHeading,String companyDescription,
                       String contactHeading,String contactDescription,String email,String productCountLabel,
                       String yearsLabel,String featuredProductSlug,String heroLocation,String productCountDescription,
                       String yearsDescription,String systemCtaLabel,String systemCoreEyebrow,String systemCoreTitle,
                       String systemCoreMeta,String systemNodeOneTitle,String systemNodeOneMeta,String systemNodeTwoTitle,
                       String systemNodeTwoMeta,String systemNodeThreeTitle,String systemNodeThreeMeta,
                       String principleOneTitle,String principleOneDescription,String principleTwoTitle,
                       String principleTwoDescription,String principleThreeTitle,String principleThreeDescription,
                       String contactEyebrow,String footerCopyright,String icpNumber) {
        this.name=name;this.englishName=englishName;this.heroEyebrow=heroEyebrow;this.heroTitle=heroTitle;
        this.heroAccentTitle=heroAccentTitle;this.heroDescription=heroDescription;this.productsHeading=productsHeading;
        this.productsIntro=productsIntro;this.systemHeading=systemHeading;this.systemIntro=systemIntro;
        this.journalHeading=journalHeading;this.companyHeading=companyHeading;this.companyDescription=companyDescription;
        this.contactHeading=contactHeading;this.contactDescription=contactDescription;this.email=email;
        this.productCountLabel=productCountLabel;this.yearsLabel=yearsLabel;this.featuredProductSlug=featuredProductSlug;
        this.heroLocation=heroLocation;this.productCountDescription=productCountDescription;this.yearsDescription=yearsDescription;
        this.systemCtaLabel=systemCtaLabel;this.systemCoreEyebrow=systemCoreEyebrow;this.systemCoreTitle=systemCoreTitle;
        this.systemCoreMeta=systemCoreMeta;this.systemNodeOneTitle=systemNodeOneTitle;this.systemNodeOneMeta=systemNodeOneMeta;
        this.systemNodeTwoTitle=systemNodeTwoTitle;this.systemNodeTwoMeta=systemNodeTwoMeta;
        this.systemNodeThreeTitle=systemNodeThreeTitle;this.systemNodeThreeMeta=systemNodeThreeMeta;
        this.principleOneTitle=principleOneTitle;this.principleOneDescription=principleOneDescription;
        this.principleTwoTitle=principleTwoTitle;this.principleTwoDescription=principleTwoDescription;
        this.principleThreeTitle=principleThreeTitle;this.principleThreeDescription=principleThreeDescription;
        this.contactEyebrow=contactEyebrow;this.footerCopyright=footerCopyright;this.icpNumber=icpNumber;
    }

    public Long getId(){return id;} public String getName(){return name;} public String getEnglishName(){return englishName;}
    public String getHeroEyebrow(){return heroEyebrow;} public String getHeroTitle(){return heroTitle;}
    public String getHeroAccentTitle(){return heroAccentTitle;} public String getHeroDescription(){return heroDescription;}
    public String getProductsHeading(){return productsHeading;} public String getProductsIntro(){return productsIntro;}
    public String getSystemHeading(){return systemHeading;} public String getSystemIntro(){return systemIntro;}
    public String getJournalHeading(){return journalHeading;} public String getCompanyHeading(){return companyHeading;}
    public String getCompanyDescription(){return companyDescription;} public String getContactHeading(){return contactHeading;}
    public String getContactDescription(){return contactDescription;} public String getEmail(){return email;}
    public String getProductCountLabel(){return productCountLabel;} public String getYearsLabel(){return yearsLabel;}
    public String getFeaturedProductSlug(){return featuredProductSlug;}
    public String getHeroLocation(){return heroLocation;} public String getProductCountDescription(){return productCountDescription;}
    public String getYearsDescription(){return yearsDescription;} public String getSystemCtaLabel(){return systemCtaLabel;}
    public String getSystemCoreEyebrow(){return systemCoreEyebrow;} public String getSystemCoreTitle(){return systemCoreTitle;}
    public String getSystemCoreMeta(){return systemCoreMeta;} public String getSystemNodeOneTitle(){return systemNodeOneTitle;}
    public String getSystemNodeOneMeta(){return systemNodeOneMeta;} public String getSystemNodeTwoTitle(){return systemNodeTwoTitle;}
    public String getSystemNodeTwoMeta(){return systemNodeTwoMeta;} public String getSystemNodeThreeTitle(){return systemNodeThreeTitle;}
    public String getSystemNodeThreeMeta(){return systemNodeThreeMeta;} public String getPrincipleOneTitle(){return principleOneTitle;}
    public String getPrincipleOneDescription(){return principleOneDescription;} public String getPrincipleTwoTitle(){return principleTwoTitle;}
    public String getPrincipleTwoDescription(){return principleTwoDescription;} public String getPrincipleThreeTitle(){return principleThreeTitle;}
    public String getPrincipleThreeDescription(){return principleThreeDescription;} public String getContactEyebrow(){return contactEyebrow;}
    public String getFooterCopyright(){return footerCopyright;} public String getIcpNumber(){return icpNumber;}
}
