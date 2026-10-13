package br.edu.utfpr.pb.pw45s.server.storage.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageUtil {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    // Bucket policy that allows anonymous read (GET) of the objects, used by the client to display the images
    private static final String PUBLIC_READ_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }""";

    // Upload Files
    @SneakyThrows
    public void putObject(String bucketName, MultipartFile multipartFile, String filename, String fileType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .contentType(fileType)
                        .build(),
                RequestBody.fromBytes(multipartFile.getBytes()));
    }

    // Get a file object as a stream from the specified bucket
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .build());
            }
        }
        return null;
    }

    // Check if bucket name exists
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    // Create bucket name (with public read access to its objects)
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);

        if (!flag) {
            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build());
            s3Client.putBucketPolicy(
                    PutBucketPolicyRequest.builder()
                            .bucket(bucketName)
                            .policy(PUBLIC_READ_POLICY.formatted(bucketName))
                            .build());
            return true;
        } else {
            return false;
        }
    }

    // List all buckets
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return s3Client.listBuckets().buckets();
    }

    // List all bucket names
    @SneakyThrows
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();

        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    // List all objects from the specified bucket
    @SneakyThrows
    public Iterable<S3Object> listObjects(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return s3Client.listObjectsV2Paginator(
                    ListObjectsV2Request.builder().bucket(bucketName).build()).contents();
        }
        return null;
    }

    // Delete Bucket by its name from the specified bucket
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<S3Object> myObjects = listObjects(bucketName);

            for (S3Object item : myObjects) {
                //  Delete failed when There are object files in bucket
                if (item.size() > 0) {
                    return false;
                }
            }

            //  Delete bucket when bucket is empty
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);

            return !flag;
        }
        return false;
    }

    // List all object names from the specified bucket
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);

        if (flag) {
            Iterable<S3Object> myObjects = listObjects(bucketName);
            for (S3Object item : myObjects) {
                listObjectNames.add(item.key());
            }
        } else {
            listObjectNames.add("Bucket does not exist");
        }

        return listObjectNames;
    }

    // Delete object from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(objectName).build());
            return true;
        }
        return false;
    }

    // Get file path from the specified bucket
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        String url = "";
        if (flag) {
            url = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(2))
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(objectName)
                                    .build())
                            .build())
                    .url().toString();
        }
        return url;
    }

    // Get metadata of the object from the specified bucket
    @SneakyThrows
    public HeadObjectResponse statObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
        }
        return null;
    }

    // Get a file object as a stream from the specified bucket （ Breakpoint download )
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName, long offset, Long length) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .range("bytes=" + offset + "-" + (offset + length - 1))
                                .build());
            }
        }
        return null;
    }

    // Delete multiple file objects from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, List<String> objectNames) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<ObjectIdentifier> objects = new ArrayList<>();
            for (String objectName : objectNames) {
                objects.add(ObjectIdentifier.builder().key(objectName).build());
            }
            DeleteObjectsResponse response = s3Client.deleteObjects(
                    DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder().objects(objects).build())
                            .build());

            return response.errors().isEmpty();
        }
        return true;
    }

    // Upload InputStream object to the specified bucket
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream inputStream, String contentType) {
        boolean flag = bucketExists(bucketName);

        if (flag) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(inputStream.readAllBytes()));
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return true;
            }
        }
        return false;
    }
}
