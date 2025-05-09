package com.atguigu.controller;

import com.atguigu.minio.MinioUploader;
import com.atguigu.result.RetVal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("/api/album")
public class FileUploadController {
    private MinioUploader minioUploader;

    public FileUploadController(MinioUploader minioUploader) {
        this.minioUploader = minioUploader;
    }

    @Operation(summary = "文件上传")
    @PostMapping("fileUpload")
    public RetVal fileUpload(MultipartFile file) throws Exception {
        String retUrl = minioUploader.uploadFile((file));
        return RetVal.ok(retUrl);

    }
}
