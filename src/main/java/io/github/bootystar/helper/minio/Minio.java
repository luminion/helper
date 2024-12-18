package io.github.bootystar.helper.minio;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class Minio {

    protected String defaultBucket = "defaultBucket";


    protected MinioClient minioClient;

    public Minio(MinioClient minioClient) {
        this.minioClient = minioClient;
        init();
    }
    public Minio(MinioClient minioClient, String defaultBucket) {
        this.minioClient = minioClient;
        this.defaultBucket = defaultBucket;
        init();
    }
    
    private void init(){
        if(defaultBucket!=null && !defaultBucket.isEmpty()){
            createBucket(defaultBucket);
        }
    }


    public boolean bucketExists(String bucketName) {
        try {
           return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        }catch (Exception e){
            log.error("createBucket => bucket error" , e);
        }
        return false;
    }

    public boolean createBucket(String bucketName) {
        try {
            boolean b = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!b){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                return true;
            }
        }catch (Exception e){
            log.error("createBucket => bucket error" , e);
        }
        return false;
    }

    public boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            return true;
        }catch (Exception e){
            log.error("createBucket => bucket error" , e);
        }
        return false;
    }



    public ObjectWriteResponse upload(String bucketName, String filename, InputStream is) {
        try {
            ObjectWriteResponse put = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            // The field file exceeds its maximum permitted size of 1048576 bytes
                            .stream(is, -1, 10485760)
                            //                        .stream(is, is.available(), -1)
                            .build()
            );
            return put;
        }  catch (Exception e) {
            log.error("upload => file error" , e);
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error("upload => close  inputStream failed" , e);
            }
        }
        return null;
    }
    public ObjectWriteResponse upload(String filename, InputStream is) {
        if (defaultBucket==null) throw new IllegalStateException("defaultBucket is null, please set defaultBucket");
        return upload(defaultBucket, filename, is);
    }


    public boolean fileExists(String bucketName, String filename){
        StatObjectResponse statObjectResponse = null;
        try {
            statObjectResponse = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        System.out.println(statObjectResponse);
        return statObjectResponse.size() > 0;
    }

    public boolean fileExists(String filename){
        if (defaultBucket==null) throw new IllegalStateException("defaultBucket is null, please set defaultBucket");
        return fileExists(defaultBucket, filename);
    }


    public boolean download(String bucketName, String filename, OutputStream os){
        try (InputStream is =
                     minioClient.getObject(
                             GetObjectArgs.builder()
                                     .bucket(bucketName)
                                     .object(filename)
                                     .build());){
            byte[] buf = new byte[16384];
            int bytesRead;
            while ((bytesRead = is.read(buf, 0, buf.length)) >= 0) {
                os.write(buf, 0, bytesRead);
            }
            os.flush();
            return true;
        }catch (Exception e){
            log.error("download => close  inputStream failed" , e);
        }finally {
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("download => close  outputStream failed" , e);
                }
            }
        }
        return false;
    }

    public boolean download(String filename, OutputStream os){
        if (defaultBucket==null) throw new IllegalStateException("defaultBucket is null, please set defaultBucket");
        return download(defaultBucket, filename, os);
    }









}
