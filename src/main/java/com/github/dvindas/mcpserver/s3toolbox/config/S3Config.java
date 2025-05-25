package com.github.dvindas.mcpserver.s3toolbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;


/**
 * @author dvindas
 */
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

}