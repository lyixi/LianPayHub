package com.lianpayhub.service.storage;

public enum AllowedFileType {

    // 配置文件：只校验扩展名（文本类型无固定 magic bytes）
    JSON(FileCategory.CONFIG, null,
            "application/json", ".json"),
    YAML(FileCategory.CONFIG, null,
            "text/yaml", ".yml", ".yaml"),
    TOML(FileCategory.CONFIG, null,
            "text/plain", ".toml"),
    XML(FileCategory.CONFIG, null,
            "text/xml", ".xml"),
    INI(FileCategory.CONFIG, null,
            "text/plain", ".ini", ".conf", ".properties"),
    TXT(FileCategory.CONFIG, null,
            "text/plain", ".txt"),

    // 图片：校验 Magic Bytes + ImageIO 解码
    JPEG(FileCategory.IMAGE,
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/jpeg", ".jpg", ".jpeg"),
    PNG(FileCategory.IMAGE,
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
            "image/png", ".png"),
    GIF(FileCategory.IMAGE,
            new byte[]{0x47, 0x49, 0x46, 0x38},
            "image/gif", ".gif"),
    WEBP(FileCategory.IMAGE,
            new byte[]{0x52, 0x49, 0x46, 0x46}, // RIFF header
            "image/webp", ".webp");

    final FileCategory category;
    final byte[] magicBytes;       // null 表示不校验 magic bytes
    final String mimeType;
    final String[] extensions;

    AllowedFileType(FileCategory category, byte[] magicBytes, String mimeType, String... extensions) {
        this.category = category;
        this.magicBytes = magicBytes;
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    public FileCategory getCategory() { return category; }
    public byte[] getMagicBytes() { return magicBytes; }
    public String getMimeType() { return mimeType; }
    public String[] getExtensions() { return extensions; }
}
