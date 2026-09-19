CREATE TABLE IF NOT EXISTS website_site_settings (
 id BIGINT NOT NULL, name VARCHAR(80) NOT NULL, english_name VARCHAR(120) NULL,
 hero_eyebrow VARCHAR(120) NULL, hero_title VARCHAR(160) NULL, hero_accent_title VARCHAR(160) NULL,
 hero_description VARCHAR(600) NULL, products_heading VARCHAR(200) NULL, products_intro VARCHAR(600) NULL,
 system_heading VARCHAR(200) NULL, system_intro VARCHAR(600) NULL, journal_heading VARCHAR(200) NULL,
 company_heading VARCHAR(240) NULL, company_description VARCHAR(800) NULL, contact_heading VARCHAR(200) NULL,
 contact_description VARCHAR(600) NULL, email VARCHAR(160) NULL, product_count_label VARCHAR(40) NULL,
 years_label VARCHAR(40) NULL, featured_product_slug VARCHAR(80) NULL,
 hero_location VARCHAR(120) NULL, product_count_description VARCHAR(120) NULL, years_description VARCHAR(120) NULL,
 system_cta_label VARCHAR(120) NULL, system_core_eyebrow VARCHAR(80) NULL, system_core_title VARCHAR(120) NULL,
 system_core_meta VARCHAR(160) NULL, system_node_one_title VARCHAR(80) NULL, system_node_one_meta VARCHAR(80) NULL,
 system_node_two_title VARCHAR(80) NULL, system_node_two_meta VARCHAR(80) NULL, system_node_three_title VARCHAR(80) NULL,
 system_node_three_meta VARCHAR(80) NULL, principle_one_title VARCHAR(80) NULL, principle_one_description VARCHAR(240) NULL,
 principle_two_title VARCHAR(80) NULL, principle_two_description VARCHAR(240) NULL, principle_three_title VARCHAR(80) NULL,
 principle_three_description VARCHAR(240) NULL, contact_eyebrow VARCHAR(120) NULL, footer_copyright VARCHAR(160) NULL,
 icp_number VARCHAR(80) NULL,
 created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE website_product ADD COLUMN preview_project VARCHAR(128) NULL;
ALTER TABLE website_product ADD COLUMN preview_volume VARCHAR(128) NULL;
ALTER TABLE website_product ADD COLUMN preview_chapter VARCHAR(128) NULL;
ALTER TABLE website_product ADD COLUMN preview_excerpt VARCHAR(600) NULL;
ALTER TABLE website_product ADD COLUMN preview_insight VARCHAR(600) NULL;
ALTER TABLE website_product ADD COLUMN preview_chapter_two VARCHAR(128) NULL;
ALTER TABLE website_product ADD COLUMN preview_excerpt_two VARCHAR(600) NULL;
ALTER TABLE website_product ADD COLUMN preview_chapter_three VARCHAR(128) NULL;
ALTER TABLE website_product ADD COLUMN preview_excerpt_three VARCHAR(600) NULL;
ALTER TABLE website_product ADD COLUMN preview_body VARCHAR(800) NULL;
ALTER TABLE website_product ADD COLUMN preview_draft VARCHAR(600) NULL;
