package com.lianpayhub.web.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.admin.ConfigurationTransferService;
import com.lianpayhub.service.admin.ConfigurationBackupService;
import java.io.*;
import java.nio.file.*;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/configuration-transfer")
public class AdminConfigurationTransferController {
    private final ConfigurationTransferService service;
    private final ConfigurationBackupService backups;

    public AdminConfigurationTransferController(ConfigurationTransferService service, ConfigurationBackupService backups) {
        this.service = service; this.backups = backups;
    }

    @GetMapping("/backup")
    public void exportBackup(HttpServletResponse response) throws IOException {
        Path archive = backups.exportBackup();
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=lianpayhub-backup.zip");
            response.setHeader("Cache-Control", "no-store");
            response.setContentLengthLong(Files.size(archive));
            Files.copy(archive, response.getOutputStream());
        } finally { Files.deleteIfExists(archive); }
    }

    @PostMapping(value = "/restore", consumes = "multipart/form-data")
    public ApiResponse<JsonNode> restoreBackup(@RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "KEEP_LOCAL") String strategy,
            @RequestParam(defaultValue = "true") boolean preview) throws IOException {
        try (InputStream input = file.getInputStream()) {
            return ApiResponse.ok(backups.importBackup(input, strategy, preview));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<JsonNode> exportConfiguration() {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=lianpayhub-configuration.json")
                .header("Cache-Control", "no-store")
                .body(service.exportConfiguration());
    }

    @PostMapping("/import")
    public ApiResponse<JsonNode> importConfiguration(@RequestBody JsonNode configuration,
            @RequestParam(defaultValue = "KEEP_LOCAL") String strategy,
            @RequestParam(defaultValue = "true") boolean preview) {
        return ApiResponse.ok(service.importConfiguration(configuration, strategy, preview));
    }
}
