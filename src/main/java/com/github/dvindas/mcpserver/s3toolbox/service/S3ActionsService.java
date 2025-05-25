package com.github.dvindas.mcpserver.s3toolbox.service;

import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectMetadataResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.GetS3ObjectResponse;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectRequest;
import com.github.dvindas.mcpserver.s3toolbox.model.PutS3ObjectResponse;

import java.util.List;

/**
 * @author dvindas
 */
public interface S3ActionsService {

    List<String> listBuckets();

    List<String> listObjects(String bucketName, String prefix);

    GetS3ObjectMetadataResponse getObjectMetadata(String bucketName, String keyName);

    PutS3ObjectResponse putObject(PutS3ObjectRequest putS3ObjectRequest);

    GetS3ObjectResponse getObject(String bucketName, String key);

    void deleteObject(String bucketName, String keyName);

}