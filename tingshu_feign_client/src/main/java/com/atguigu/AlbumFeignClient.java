package com.atguigu;

import com.atguigu.entity.AlbumAttributeValue;
import com.atguigu.entity.AlbumInfo;
import com.atguigu.entity.TrackInfo;
import com.atguigu.result.RetVal;
import com.atguigu.vo.AlbumStatVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "tingshu-album")
public interface AlbumFeignClient {
    @GetMapping("/api/album/albumInfo/getAlbumInfoById/{albumId}")
    public RetVal<AlbumInfo> getAlbumInfoById(@PathVariable Long albumId);
    @GetMapping("/api/album/albumInfo/getAlbumPropertyValue/{albumId}")
    public List<AlbumAttributeValue> getAlbumPropertyValue(@PathVariable Long albumId);
    @GetMapping("/api/album/albumInfo/getAlbumStatInfo/{albumId}")
    public AlbumStatVo getAlbumStatInfo(@PathVariable Long albumId);

    @GetMapping("/api/album/trackInfo/getTrackListPrepareToBuy/{trackId}/{buyNum}")
    public RetVal<List<TrackInfo>> getTrackListPrepareToBuy(@PathVariable Long trackId, @PathVariable Integer buyNum);

    @GetMapping("/api/album/trackInfo/getTrackInfoById/{trackId}")
    public RetVal<TrackInfo> getTrackInfoById(@PathVariable Long trackId);
}
