package com.github.dvindas.mcpserver.s3toolbox.model;

/**
 * @author dvindas
 */
public record GetS3ObjectResponse(String fileName, byte[] content, String contentType) {
}
