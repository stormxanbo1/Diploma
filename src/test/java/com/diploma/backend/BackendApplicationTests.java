// src/test/java/com/diploma/backend/BackendApplicationTests.java
package com.diploma.backend;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke-тест: проверяем, что Spring-контекст поднимается.
 * Включаем MockMvc, чтобы тестовый SecurityHelpers мог его использовать,
 * и подменяем MinioClient мок-объектом.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@AutoConfigureMockMvc
@Import(BackendApplicationTests.MinioMockConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// если контекст собрался — тест проходит
	}

	/** Тестовая конфигурация: мок MinioClient */
	@TestConfiguration
	static class MinioMockConfig {
		@Bean
		MinioClient minioClient() {
			return Mockito.mock(MinioClient.class);
		}
	}
}
