package com.atguigu.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data

@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
//    endpoint: http://192.168.159.131:9000
//    access-key: enjoy6288
//    secret-key: enjoy6288
//    bucket-name:
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

}
