package com.github.dvindas.mcpserver.s3toolbox.tool;

import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectMetadataResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectRequest;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectResponse;
import com.github.dvindas.mcpserver.s3toolbox.service.S3ActionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dvindas
 */
@Component
@RequiredArgsConstructor
public class S3Tool {

    private final S3ActionsService s3ActionsService;

    @Tool(name = "list_s3_buckets", description = "Lists all the s3 buckets for the given account.")
    public List<String> listS3Buckets() {
        return s3ActionsService.listBuckets();
    }

    @Tool(name = "list_s3_files", description = "Returns a list of object keys in the specified S3 bucket, optionally filtered by a prefix.")
    public List<String> listS3Files(@ToolParam(description = "Name of the S3 bucket to search") String bucketName, @ToolParam(description = "Optional prefix to filter results (e.g. 'invoices/'). Use empty string or omit to list all objects.") String prefix) {
        return s3ActionsService.listObjects(bucketName, prefix);
    }

    @Tool(name = "get_s3_object_metadata", description = "Retrieves metadata (e.g., size, content-type, last modified) for the specified S3 object.")
    public GetS3ObjectMetadataResponse getS3ObjectMetadata(@ToolParam(description = "Name of the S3 bucket containing the object") String bucketName, @ToolParam(description = "Key (path and filename) of the S3 object to retrieve metadata for") String key) {
        return s3ActionsService.getObjectMetadata(bucketName, key);
    }

    @Tool(name = "put_s3_object", description = "Upload file bytes to S3 with optional prefix, returning ETag")
    public PutS3ObjectResponse putS3Object(PutS3ObjectRequest putS3ObjectRequest) {
        return s3ActionsService.putObject(putS3ObjectRequest);
    }

    @Tool(name = "get_s3_object", description = "Download an object from S3 by bucket name and key, returning its content and metadata such as content type, size, and ETag.")
    public GetS3ObjectResponse getS3Object(@ToolParam(description = "The name of the S3 bucket where the object is stored.") String bucketName,
                                           @ToolParam(description = "The full key (path/filename) of the object to retrieve.") String key) {
        return s3ActionsService.getObject(bucketName, key);
    }

    @Tool(name = "delete_s3_object", description = "Delete an object from S3 by bucket name and key. Returns DeleteMarker and VersionId if applicable.")
    public void deleteS3Object(@ToolParam(description = "Name of the S3 bucket that contains the object to delete.") String bucketName, @ToolParam(description = "Key (path/filename) of the object to delete from the bucket.") String key) {
        s3ActionsService.deleteObject(bucketName, key);
    }

}
