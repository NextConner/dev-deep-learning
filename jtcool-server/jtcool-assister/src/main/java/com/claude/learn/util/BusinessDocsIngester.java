package com.claude.learn.util;

import com.claude.learn.service.DocumentIngestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 批量导入业务文档到RAG系统
 * 运行方式：在application.yml中设置 business-docs.auto-ingest: true
 */
@Component
public class BusinessDocsIngester implements CommandLineRunner {

    @Autowired
    private DocumentIngestService documentIngestService;

    @Override
    public void run(String... args) {
        String autoIngest = System.getProperty("business-docs.auto-ingest", "false");
        if (!"true".equals(autoIngest)) {
            return;
        }

        String basePath = "src/main/resources/business-docs";
        try {
            ingestDirectory(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to ingest business docs", e);
        }
    }

    private void ingestDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".md"))
                 .forEach(this::ingestFile);
        }
    }

    private void ingestFile(Path filePath) {
        try {
            byte[] content = Files.readAllBytes(filePath);
            String filename = filePath.getFileName().toString();
            documentIngestService.ingest(content, filename);
        } catch (Exception e) {
            System.err.println("Failed to ingest: " + filePath + " - " + e.getMessage());
        }
    }
}
