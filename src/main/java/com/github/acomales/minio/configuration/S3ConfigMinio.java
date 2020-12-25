package com.github.acomales.minio.configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

//@Profile({"minio"})
@Configuration
public class S3ConfigMinio {
    @Value("${aws.s3.accessKey}")
    private String awsId;

    @Value("${aws.s3.secretKey}")
    private String awsKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.minio-endpoint}")
    private String minioEndpoint;

    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(awsId, awsKey);
    }

    @Bean
    public AmazonS3 amazonS3() {
        final ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioEndpoint, region))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                .build();
    }
}
