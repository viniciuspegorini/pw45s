package br.edu.utfpr.pb.pw45s.server.storage.service.impl;

import br.edu.utfpr.pb.pw45s.server.storage.config.StorageConfig;
import br.edu.utfpr.pb.pw45s.server.storage.payload.FileResponse;
import br.edu.utfpr.pb.pw45s.server.storage.service.StorageService;
import br.edu.utfpr.pb.pw45s.server.storage.util.StorageUtil;
import software.amazon.awssdk.services.s3.model.Bucket;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final StorageUtil storageUtil;
    private final StorageConfig storageProperties;

    public StorageServiceImpl(StorageUtil storageUtil, StorageConfig storageProperties) {
        this.storageUtil = storageUtil;
        this.storageProperties = storageProperties;
    }

    @SneakyThrows
    @Override
    public FileResponse putObject(MultipartFile multipartFile,
                                  String bucketName,
                                  String fileType) {
        try {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : storageProperties.getBucketName();
            if (!this.bucketExists(bucketName)) {
                this.makeBucket(bucketName);
            }
            String fileName = multipartFile.getOriginalFilename();
            Long fileSize = multipartFile.getSize();
            String objectName = UUID.randomUUID().toString().replaceAll("-", "")
                    + fileName.substring(fileName.lastIndexOf("."));
            LocalDateTime createdTime = LocalDateTime.now();
            storageUtil.putObject(bucketName, multipartFile, objectName,fileType);

            return FileResponse.builder()
                    .filename(objectName)
                    .fileSize(fileSize)
                    .contentType(fileType)
                    .createdTime(createdTime)
                    .build();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public InputStream downloadObject(String bucketName, String objectName) {
        return storageUtil.getObject(bucketName,objectName);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return storageUtil.bucketExists(bucketName);
    }

    @Override
    public void makeBucket(String bucketName) {
        storageUtil.makeBucket(bucketName);
    }

    @Override
    public List<String> listBucketName() {
        return storageUtil.listBucketNames();
    }

    @Override
    public List<Bucket> listBuckets() {
        return storageUtil.listBuckets();
    }

    @Override
    public boolean removeBucket(String bucketName) {
        return storageUtil.removeBucket(bucketName);
    }

    @Override
    public List<String> listObjectNames(String bucketName) {
        return storageUtil.listObjectNames(bucketName);
    }

    @Override
    public boolean removeObject(String bucketName, String objectName) {
        return storageUtil.removeObject(bucketName, objectName);
    }

    @Override
    public boolean removeListObject(String bucketName, List<String> objectNameList) {
        return storageUtil.removeObject(bucketName,objectNameList);
    }

    @Override
    public String getObjectUrl(String bucketName, String objectName) {
        return storageUtil.getObjectUrl(bucketName, objectName);
    }
}
