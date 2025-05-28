package com.diploma.backend.config;

import com.diploma.backend.dto.CommentDto;
import com.diploma.backend.entity.Comment;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();

        // Разрешаем маппить приватные поля напрямую и пропускать null
        mm.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true);

        // Явное маппирование для полей authorName и authorId
        mm.addMappings(new PropertyMap<Comment, CommentDto>() {
            @Override
            protected void configure() {
                // Проверяем, что автор не null
                if (source.getAuthor() != null) {
                    map(source.getAuthor().getFirstName() + " " + source.getAuthor().getLastName(), destination.getAuthorName());
                    map(source.getAuthor().getId(), destination.getAuthorId());
                } else {
                    // Если автор null, устанавливаем значение по умолчанию или пустое
                    map("", destination.getAuthorName());
                    map(null, destination.getAuthorId());
                }
            }
        });

        return mm;
    }
}
