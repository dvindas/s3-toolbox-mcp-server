package com.github.dvindas.mcpserver.s3toolbox.model;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author dvindas
 */
public record PutS3ObjectRequest(
        @ToolParam(description = "Name of the target S3 bucket (e.g., 'my-bucket')")
        String bucketName,

        @ToolParam(description = "Optional path prefix/folder inside the bucket (e.g., 'invoices/2025/'). Leave empty or omit to upload to the bucket root.")
        String prefix,

        @ToolParam(description = "Filename to save in S3 (e.g., 'report.pdf')")
        String fileName,

        @ToolParam(description = "MIME type of the file (e.g., 'application/pdf', 'image/png')")
        String contentType,

        @ToolParam(description = "The file's binary content, provided as a Base64-encoded string and deserialized into a byte array.")
        String base64Content
) {

}