package com.github.dvindas.mcpserver.s3toolbox.service;

import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ActionsServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3ActionsServiceImpl s3ActionsService;

    @Test
    void listBuckets_Success() {
        // Arrange
        var bucket1 = Bucket.builder().name("bucket1").build();
        var bucket2 = Bucket.builder().name("bucket2").build();
        var response = ListBucketsResponse.builder()
                .buckets(bucket1, bucket2)
                .build();
        when(s3Client.listBuckets()).thenReturn(response);

        // Act
        var result = s3ActionsService.listBuckets();

        // Assert
        assertEquals(List.of("bucket1", "bucket2"), result);
        verify(s3Client).listBuckets();
    }

    @Test
    void listObjects_Success() {
        // Arrange
        var object1 = S3Object.builder().key("test/file1.txt").build();
        var object2 = S3Object.builder().key("test/file2.txt").build();
        var response = ListObjectsV2Response.builder()
                .contents(object1, object2)
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // Act
        var result = s3ActionsService.listObjects("testBucket", "test/");

        // Assert
        assertEquals(List.of("test/file1.txt", "test/file2.txt"), result);
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void getObjectMetadata_Success() {
        // Arrange
        var bucketName = "testBucket";
        var key = "test/file.txt";
        var contentType = "text/plain";
        var contentLength = 1024L;
        var lastModified = Instant.now();
        var eTag = "test-etag";
        var storageClass = "STANDARD";
        var metadata = Map.of("key1", "value1");

        var headResponse = HeadObjectResponse.builder()
                .contentType(contentType)
                .contentLength(contentLength)
                .lastModified(lastModified)
                .eTag(eTag)
                .metadata(metadata)
                .build();

        var attrResponse = GetObjectAttributesResponse.builder()
                .storageClass(StorageClass.STANDARD)
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headResponse);
        when(s3Client.getObjectAttributes(any(GetObjectAttributesRequest.class))).thenReturn(attrResponse);

        // Act
        var result = s3ActionsService.getObjectMetadata(bucketName, key);

        // Assert
        assertNotNull(result);
        assertEquals(contentType, result.contentType());
        assertEquals(contentLength, result.contentLength());
        assertEquals(lastModified, result.lastModified());
        assertEquals(eTag, result.eTag());
        assertEquals(storageClass, result.storageClass());
        assertEquals(metadata, result.customMetadata());
    }

    @Test
    void putObject_Success() {
        // Arrange
        var bucketName = "testBucket";
        var prefix = "test/";
        var fileName = "file.txt";
        var contentType = "text/plain";
        var content = "Hello World";
        var base64Content = Base64.getEncoder().encodeToString(content.getBytes());
        var expectedETag = "test-etag";

        var request = new PutS3ObjectRequest(bucketName, prefix, fileName, contentType, base64Content);
        var response = PutObjectResponse.builder().eTag(expectedETag).build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);

        // Act
        var result = s3ActionsService.putObject(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedETag, result.eTag());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void getObject_Success() {
        // Arrange
        var bucketName = "testBucket";
        var key = "test/file.txt";
        var content = "Hello World";
        var contentType = "text/plain";

        var response = ResponseBytes.fromByteArray(
            GetObjectResponse.builder().contentType(contentType).build(),
            content.getBytes()
        );

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(response);

        // Act
        var result = s3ActionsService.getObject(bucketName, key);

        // Assert
        assertNotNull(result);
        assertEquals("file.txt", result.fileName());
        assertEquals(contentType, result.contentType());
        assertArrayEquals(content.getBytes(), result.content());
    }

    @Test
    void deleteObject_Success() {
        // Arrange
        var bucketName = "testBucket";
        var key = "test/file.txt";

        // Act & Assert
        assertDoesNotThrow(() -> s3ActionsService.deleteObject(bucketName, key));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void listBuckets_WhenS3Exception_ThrowsRuntimeException() {
        // Arrange
        when(s3Client.listBuckets()).thenThrow(S3Exception.builder().build());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> s3ActionsService.listBuckets());
    }

    @Test
    void listObjects_WhenS3Exception_ThrowsRuntimeException() {
        // Arrange
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().build());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> s3ActionsService.listObjects("testBucket", "prefix"));
    }

    @Test
    void getObjectMetadata_WhenS3Exception_ThrowsRuntimeException() {
        // Arrange
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().build());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> s3ActionsService.getObjectMetadata("testBucket", "key"));
    }

    @Test
    void putObject_WhenS3Exception_ThrowsRuntimeException() {
        // Arrange
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().build());

        var request = new PutS3ObjectRequest(
                "testBucket",
                "prefix/",
                "file.txt",
                "text/plain",
                Base64.getEncoder().encodeToString("test".getBytes())
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () -> s3ActionsService.putObject(request));
    }

    @Test
    void getObject_WhenS3Exception_ThrowsRuntimeException() {
        // Arrange
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().build());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> s3ActionsService.getObject("testBucket", "key"));
    }
} 