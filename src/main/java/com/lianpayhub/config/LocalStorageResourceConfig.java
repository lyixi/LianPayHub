package com.lianpayhub.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 仅在本地存储模式下生效。
 * 将磁盘 storage/ 目录映射到 /files/** 路径，供开发环境下载文件。
 * 生产环境使用 OSS 签名 URL，无需此配置。
 */
@Configuration
@ConditionalOnProperty(name = "lianpayhub.storage.backend", havingValue = "local", matchIfMissing = true)
public class LocalStorageResourceConfig implements WebMvcConfigurer {

    private final StorageProperties props;

    public LocalStorageResourceConfig(StorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(props.getLocalPath()).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
