package com.atguigu.service.impl;

import com.atguigu.cofig.VodProperties;
import com.atguigu.entity.TrackInfo;
import com.atguigu.service.VodService;
import com.atguigu.util.UploadFileUtil;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class VodServiceImpl implements VodService {
    @Autowired
    private VodProperties vodProperties;
    @SneakyThrows
    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {
        //声音上传的临时文件
        String tempPath = UploadFileUtil.uploadTempPath(vodProperties.getTempPath(), file);
        VodUploadClient client = new VodUploadClient(vodProperties.getSecretId(),
                vodProperties.getSecretKey());
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath(tempPath);
        VodUploadResponse response = client.upload(vodProperties.getRegion(), request);
        Map<String, Object> map = new HashMap<>();
        map.put("mediaFileId",response.getFileId());
        map.put("mediaUrl",response.getMediaUrl());
        return map;

}
    @SneakyThrows
    @Override
    public void getTrackMediaInfo(TrackInfo trackInfo) {
        Credential cred = new Credential(vodProperties.getSecretId(), vodProperties.getSecretKey());
        // 实例化要请求产品的client对象,clientProfile是可选的
        VodClient client = new VodClient(cred, vodProperties.getRegion());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
        String[] mediaFileIds = {trackInfo.getMediaFileId()};
        req.setFileIds(mediaFileIds);
        // 返回的resp是一个DescribeMediaInfosResponse的实例，与请求对象对应
        DescribeMediaInfosResponse resp = client.DescribeMediaInfos(req);
        if(resp.getMediaInfoSet().length>0){
            MediaInfo mediaInfo = resp.getMediaInfoSet()[0];
            trackInfo.setMediaSize(mediaInfo.getMetaData().getSize());
            trackInfo.setMediaDuration(BigDecimal.valueOf(mediaInfo.getMetaData().getDuration()));
            trackInfo.setMediaType(mediaInfo.getBasicInfo().getType());
        }

    }
    @SneakyThrows
    @Override
    public void removeTrack(String mediaFileId) {

            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(vodProperties.getSecretId(),vodProperties.getSecretKey());
//            // 实例化一个http选项，可选的，没有特殊需求可以跳过
//            HttpProfile httpProfile = new HttpProfile();
//            httpProfile.setEndpoint("vod.tencentcloudapi.com");
//            // 实例化一个client选项，可选的，没有特殊需求可以跳过
//            ClientProfile clientProfile = new ClientProfile();
//            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient client = new VodClient(cred, vodProperties.getRegion());
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DeleteMediaRequest req = new DeleteMediaRequest();
            req.setFileId(mediaFileId);
            // 返回的resp是一个DeleteMediaResponse的实例，与请求对象对应
            DeleteMediaResponse resp = client.DeleteMedia(req);



    }

}
