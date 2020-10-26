package com.epam.aws.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AwsUtils {

    private static final long DEFAULT_SESSION_EXPIRATION_IN_SECONDS = 60L * 60L;
    public static GeneratePresignedUrlRequest getPresignedUrlRequest(String bucket, String key) {
        return new GeneratePresignedUrlRequest(bucket, FileUtils.normalizeFileName(key))
                .withMethod(HttpMethod.GET)
                .withExpiration(getExpirationDate());
    }

    private static Date getExpirationDate() {
        return Date.from(LocalDateTime.now().plusSeconds(DEFAULT_SESSION_EXPIRATION_IN_SECONDS)
                                      .atZone(ZoneId.systemDefault())
                                      .toInstant());
    }
}
