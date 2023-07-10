package com.testcontainers.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    private final MessageSender messageSender;
    private final StorageService storageService;
    private final ApplicationProperties properties;

    MessageController(MessageSender messageSender, StorageService storageService, ApplicationProperties properties) {
        this.messageSender = messageSender;
        this.storageService = storageService;
        this.properties = properties;
    }

    @PostMapping("/api/messages")
    public Map<String, String> create(@RequestBody Message message) {
        messageSender.publish(properties.bucket(), message);
        String key = message.uuid().toString();
        String content = message.content();
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        storageService.upload(properties.bucket(), key, is);
        return Map.of("uuid", key);
    }

    @GetMapping("/api/messages/{uuid}")
    public Map<String, String> get(@PathVariable String uuid) throws IOException {
        String content = storageService.downloadAsString(properties.bucket(), uuid);
        return Map.of(
                "uuid", uuid,
                "content", content);
    }
}
