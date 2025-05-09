package com.atguigu.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@EnableConfigurationProperties({MinioProperties.class})
public class MinioUploader {
@Autowired
private MinioProperties minioProperties;
@Autowired
private MinioClient minioClient;
@SneakyThrows
@Bean
public MinioClient minioClient(){
    // Create a minioClient with the MinIO server playground, its access key and secret key.
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
    //create bucket
    boolean found =
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
    if (!found) {
        // Make a new bucket called 'asiatrip'.
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
    } else {
        System.out.println("Bucket "+minioProperties.getBucketName()+" already exists.");
    }

return minioClient;

}

public String uploadFile(MultipartFile file)throws Exception{
    //设置存储对象名称
    String prefix = UUID.randomUUID().toString().replace("-", "");
    String originalFilename = file.getOriginalFilename();
    String suffix = FilenameUtils.getExtension(originalFilename);
    String fileName=prefix+"."+suffix;
    minioClient.putObject(
            PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileName)
                    .stream(file.getInputStream(),file.getSize(),-1)
                    .build());
    //http://192.168.159.131:9000/tingshu/baby.jpg
   String retUrl= minioProperties.getEndpoint()+"/"+minioProperties.getBucketName()+"/"+fileName;
   return retUrl;
//    public static void main(String[] args) {
//        String a = "girl.jpg";
//        String extension = FilenameUtils.getExtension(a);
//        System.out.println(extension);
//        String suffix = a.substring(a.lastIndexOf("."));
//        System.out.println(suffix);
//    }
}

}
