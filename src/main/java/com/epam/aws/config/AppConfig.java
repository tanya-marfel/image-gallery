package com.epam.aws.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.epam.aws")
public class AppConfig {

    @Value("${amazon.s3.access-key}")
    private String accessKey;

    @Value("${amazon.s3.secret-key}")
    private String secretKey;

    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.EU_CENTRAL_1)
                                    .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                                    .build();
    }

    @Bean
    public AmazonSNS snsClient() {
        return AmazonSNSClient.builder()
                              .withRegion(Regions.EU_CENTRAL_1)
                              .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                              .build();
    }
}
