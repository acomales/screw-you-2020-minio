package com.github.acomales.minio.fileupload;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.acomales.minio.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUploadService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final String S3_PATH_PREFIX = "public/";

    private final S3Service s3Service;

    public FileUploadResponse uploadFile(final MultipartFile file) throws IOException {
        final String filename = getFilename(file.getName(), file.getOriginalFilename());

        if (!s3Service.bucketExists(bucketName)) {
            s3Service.createPrivateBucketWithPublicDirectory(bucketName);
        }

        final ObjectMetadata metadata = s3Service.createObjectMetadata(file.getContentType(), file.getSize());
        final String objectKey = createObjectKey(filename);
        final String url = s3Service.saveObject(bucketName, objectKey, file.getInputStream(), metadata);
        return new FileUploadResponse(url);
    }

    private String createObjectKey(final String filename) {
        return String.format("%s%s", S3_PATH_PREFIX, filename);
    }

    private String getFilename(final String name, final String originalFilename) {
        final String requestFilename = Objects.nonNull(originalFilename) && !originalFilename.isBlank() ? originalFilename : name;

        final int lastDotIndex = requestFilename.lastIndexOf('.');
        final String filename = requestFilename.substring(0, lastDotIndex);
        final String extension = requestFilename.substring(lastDotIndex + 1);

        return String.format("%s_%s.%s", filename, getCurrentTimestamp(), extension);
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
