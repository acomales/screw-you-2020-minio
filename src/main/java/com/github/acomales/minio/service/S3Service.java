package com.github.acomales.minio.service;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 s3Client;

    public boolean bucketExists(final String bucketName) {
        return s3Client.doesBucketExistV2(bucketName);
    }

    public void createBucket(final String bucketName) {
        s3Client.createBucket(bucketName);
    }

    public void createPublicBucket(final String bucketName) {
        s3Client.createBucket(bucketName);

        // ObjectACL is not supported on MinIO, using BucketPolicy instead
        s3Client.setBucketPolicy(bucketName, createPublicReadPolicy(bucketName, ""));

        log.info(String.format("Public bucket %s created successfully.", bucketName));
    }

    public void createPrivateBucketWithPublicDirectory(final String bucketName) {
        s3Client.createBucket(bucketName);

        // ObjectACL is not supported on MinIO, using BucketPolicy instead
        s3Client.setBucketPolicy(bucketName, createPublicReadPolicy(bucketName, "public"));

        log.info(String.format("Bucket %s created successfully.", bucketName));
    }

    public String saveObject(final String bucketName, final String objectKey, final InputStream inputStream, final ObjectMetadata metadata) {
        if (!bucketExists(bucketName)) {
            throw new RuntimeException(String.format("Bucket %s not exists.", bucketName));
        }

        s3Client.putObject(new PutObjectRequest(bucketName, objectKey, inputStream, metadata));
        return s3Client.getUrl(bucketName, objectKey).toExternalForm();
    }


    public ObjectMetadata createObjectMetadata(final String contentType, final long size) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);
        return metadata;
    }

    /**
     * Create a public read policy on the bucket.
     *
     * @param bucketName - bucket name for which policy is applied
     * @return policy for public access
     */
    private String createPublicReadPolicy(final String bucketName, final String publicDirectory) {
        final String directory = publicDirectory != null && !publicDirectory.isBlank()
                ? String.format("/%s", publicDirectory.trim())
                : "";

        final Policy bucketPolicy = new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withPrincipals(Principal.AllUsers)
                        .withActions(S3Actions.GetObject)
                        .withResources(new Resource(
                                "arn:aws:s3:::" + bucketName + directory + "/*")));
        return bucketPolicy.toJson();
    }
}
