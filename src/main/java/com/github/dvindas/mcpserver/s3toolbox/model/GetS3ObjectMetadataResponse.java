package com.github.dvindas.mcpserver.s3toolbox.model;

import java.time.Instant;
import java.util.Map;

/**
 * @author dvindas
 */
public record GetS3ObjectMetadataResponse(String contentType, Long contentLength, Instant lastModified, String eTag,
                                          String storageClass, Map<String, String> customMetadata) {
}
