package com.github.dvindas.mcpserver.s3toolbox.service;

import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectMetadataResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author dvindas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3ActionsServiceImpl implements S3ActionsService {

    private final S3Client s3Client;

    @Override
    public List<String> listBuckets() {
        try {
            final var response = s3Client.listBuckets();

            return Optional.ofNullable(response.buckets())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("S3 error while listing buckets", e);
            throw new RuntimeException("Failed to list buckets from S3", e);
        } catch (Exception e) {
            log.error("Error while listing buckets", e);
            throw new RuntimeException("Error while listing buckets", e);
        }
    }

    @Override
    public List<String> listObjects(final String bucketName, final String prefix) {
        try {
            final var request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            final var response = s3Client.listObjectsV2(request);

            return Optional.ofNullable(response.contents())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("S3 error while listing objects in bucket '{}' with prefix '{}'", bucketName, prefix, e);
            throw new RuntimeException("Failed to list objects from S3", e);
        } catch (Exception e) {
            log.error("Error while listing objects in bucket '{}' with prefix '{}'", bucketName, prefix, e);
            throw new RuntimeException("Error while listing objects", e);
        }
    }

    @Override
    public GetS3ObjectMetadataResponse getObjectMetadata(final String bucketName, final String key) {
        try {
            final var headFuture = CompletableFuture.supplyAsync(() -> {
                var headRequest = HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                return s3Client.headObject(headRequest);
            });

            final var attrFuture = CompletableFuture.supplyAsync(() -> {
                var attrRequest = GetObjectAttributesRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .objectAttributes(ObjectAttributes.STORAGE_CLASS)
                        .build();
                return s3Client.getObjectAttributes(attrRequest);
            });

            return headFuture.thenCombine(attrFuture, (headResponse, attrResponse) ->
                    new GetS3ObjectMetadataResponse(headResponse.contentType(), headResponse.contentLength(),
                            headResponse.lastModified(), headResponse.eTag(), attrResponse.storageClassAsString(),
                            headResponse.metadata())
            ).join();

        } catch (S3Exception e) {
            log.error("S3 error while getting metadata for object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Failed to get object metadata from S3", e);
        } catch (Exception e) {
            log.error("Error while getting metadata for object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Error while getting object metadata", e);
        }
    }

    @Override
    public PutS3ObjectResponse putObject(final PutS3ObjectRequest putS3ObjectRequest) {
        try {
            final var request = PutObjectRequest.builder()
                    .bucket(putS3ObjectRequest.bucketName())
                    .key(putS3ObjectRequest.prefix().concat(putS3ObjectRequest.fileName()))
                    .contentType(putS3ObjectRequest.contentType())
                    .build();

            final var response = s3Client.putObject(request,
                    RequestBody.fromBytes(Base64.getDecoder().decode(putS3ObjectRequest.base64Content())));

            return new PutS3ObjectResponse(response.eTag());

        } catch (S3Exception e) {
            log.error("S3 error while uploading object to bucket '{}'", putS3ObjectRequest.bucketName(), e);
            throw new RuntimeException("Failed to upload object to S3", e);
        } catch (Exception e) {
            log.error("Error while uploading object to bucket '{}'", putS3ObjectRequest.bucketName(), e);
            throw new RuntimeException("Error while uploading object", e);
        }
    }

    @Override
    public GetS3ObjectResponse getObject(final String bucketName, final String key) {
        try {
            final var request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            final var responseBytes = s3Client.getObjectAsBytes(request);
            final var metadata = responseBytes.response();

            final var fileName = key.contains("/") ? key.substring(key.lastIndexOf("/") + 1) : key;

            return new GetS3ObjectResponse(fileName, responseBytes.asByteArray(), metadata.contentType());

        } catch (S3Exception e) {
            log.error("S3 error while getting object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Failed to getting object from S3", e);
        } catch (Exception e) {
            log.error("Error while getting object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Error while getting object", e);
        }
    }


    @Override
    public void deleteObject(final String bucketName, final String key) {
        try {
            final var request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);

        } catch (S3Exception e) {
            log.error("S3 error while deleting object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Failed to delete object from S3", e);
        } catch (Exception e) {
            log.error("Error while deleting object '{}/{}'", bucketName, key, e);
            throw new RuntimeException("Error while deleting object", e);
        }
    }

}
