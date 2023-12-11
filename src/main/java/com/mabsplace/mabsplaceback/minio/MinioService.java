package com.mabsplace.mabsplaceback.minio;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioService {

  private final MinioClient minioClient;

  private final MinioProperties minioProperties;

  public MinioService(MinioClient minioClient, MinioProperties minioProperties) {
    this.minioClient = minioClient;
    this.minioProperties = minioProperties;
  }

  public void uploadImage(String objectName, InputStream inputStream, String contentType) throws RuntimeException{
    try {
      minioClient.putObject(PutObjectArgs.builder()
              .bucket(minioProperties.getBucketName())
              .object(objectName)
              .stream(inputStream, inputStream.available(), -1)
              .contentType(contentType)
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to upload image");
    }
  }

  public InputStream downloadImage(String objectName) {
    try {
      return minioClient.getObject(GetObjectArgs.builder().bucket(minioProperties.getBucketName()).object(objectName).build());
    } catch (Exception e) {
      // Handle exception
      return null;
    }
  }
}