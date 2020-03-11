package com.onix.featureflags.books;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BooksServiceConfig {

    @Bean
    @ConditionalOnProperty(
            name = "feature-flags.is-new-books-service-enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public BooksService booksDefaultService() {
        return new BooksDefaultService();
    }

    @Bean
    @ConditionalOnProperty(
            name = "feature-flags.is-new-books-service-enabled",
            havingValue = "true"
    )
    public BooksService booksNewService() {
        return new BooksNewService();
    }

}
