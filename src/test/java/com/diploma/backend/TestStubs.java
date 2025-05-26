package com.diploma.backend;

import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestStubs {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://stub", 9000, false)
                .credentials("k", "s")
                .build();
    }
}
