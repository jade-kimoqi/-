package com.atguigu.service.impl;

import com.atguigu.SearchFeignClient;
import com.atguigu.cache.TingShuCache;
import com.atguigu.constant.KafkaConstant;
import com.atguigu.constant.RedisConstant;
import com.atguigu.constant.SystemConstant;
import com.atguigu.entity.AlbumAttributeValue;
import com.atguigu.entity.AlbumInfo;
import com.atguigu.entity.AlbumStat;
import com.atguigu.mapper.AlbumInfoMapper;
import com.atguigu.service.AlbumAttributeValueService;
import com.atguigu.service.AlbumInfoService;
import com.atguigu.service.AlbumStatService;
import com.atguigu.service.KafkaService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.util.MongoUtil;
import com.atguigu.vo.AlbumTempVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 专辑信息 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-03-07
 */
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {
    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;
    @Autowired
    private AlbumStatService albumStatService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;
    @Autowired
    private SearchFeignClient searchFeignClient;
    @Autowired
    private KafkaService kafkaService;
    @Override
    public void saveAlbumInfo(AlbumInfo albumInfo){
        Long userId = AuthContextHolder.getUserId();
        albumInfo.setUserId(userId);
        albumInfo.setStatus(SystemConstant.ALBUM_APPROVED);
        if (!SystemConstant.FREE_ALBUM.equals(albumInfo.getPayType())) {
            albumInfo.setTracksForFree(5);
        }
        save(albumInfo);
        List<AlbumAttributeValue> albumPropertyValueList = albumInfo.getAlbumPropertyValueList();
        if (!CollectionUtils.isEmpty(albumPropertyValueList)) {
            for (AlbumAttributeValue albumAttributeValueValue : albumPropertyValueList) {
                albumAttributeValueValue.setAlbumId(albumInfo.getId());
            }
            albumAttributeValueService.saveBatch(albumPropertyValueList);
        }

        List<AlbumStat> albumStatList = buildAlbumStatData(albumInfo.getId());
        albumStatService.saveBatch(albumStatList);
//        TODO专辑的统计信息
//        专辑私密信息
        if(SystemConstant.OPEN_ALBUM.equals((albumInfo.getIsOpen()))){
//            searchFeignClient.onSaleAlbum(albumInfo.getId());
            kafkaService.sendMessage(KafkaConstant.ONSALE_ALBUM_QUEUE,String.valueOf(albumInfo.getId()));
        }

    }
    @TingShuCache("albumInfo")
    @Override
    public AlbumInfo getAlbumInfoById(Long albumId) {
        AlbumInfo albumInfo = getAlbumInfoFromDB(albumId);
//        AlbumInfo albumInfo= getAlbumInfoFromRedis(albumId);
//        AlbumInfo albumInfo= getAlbumInfoFromRedisson(albumId);
        return albumInfo;
    }

    private AlbumInfo getAlbumInfoFromRedisson(Long albumId) {

        String cacheKey = RedisConstant.ALBUM_INFO_PREFIX+albumId;
        AlbumInfo albumInfoRedis =(AlbumInfo) redisTemplate.opsForValue().get(cacheKey);
        String lockKey="lock-"+albumId;
        RLock lock = redissonClient.getLock(lockKey);

        if(albumInfoRedis==null){

            try {
                lock.forceUnlock();
                boolean flag = bloomFilter.contains(albumId);
                if (flag){
                    AlbumInfo albumInfoDB = getAlbumInfoFromDB(albumId);
                    redisTemplate.opsForValue().set(cacheKey,albumInfoDB);
                    return albumInfoDB;
                }

            } finally {
            lock.unlock();
            }
        }
        return albumInfoRedis;

    }

    @Override
    public void updateAlbumInfo(AlbumInfo albumInfo) {
        updateById(albumInfo);
        //删除属性信息
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId,albumInfo.getId());
        albumAttributeValueService.remove(wrapper);
//        保存专辑标签
        List<AlbumAttributeValue> albumPropertyValueList = albumInfo.getAlbumPropertyValueList();
        if (!CollectionUtils.isEmpty(albumPropertyValueList)) {
           for(AlbumAttributeValue albumAttributeValue:albumPropertyValueList){
               albumAttributeValue.setAlbumId(albumInfo.getId());
            }
           albumAttributeValueService.saveBatch(albumPropertyValueList);
        }
        //如果公开专辑 把专辑信息添加到ES中
        if(SystemConstant.OPEN_ALBUM.equals(albumInfo.getIsOpen())){
            //searchFeignClient.onSaleAlbum(albumInfo.getId());
            kafkaService.sendMessage(KafkaConstant.ONSALE_ALBUM_QUEUE,String.valueOf(albumInfo.getId()));
        }else{
            //searchFeignClient.offSaleAlbum(albumInfo.getId());
            kafkaService.sendMessage(KafkaConstant.OFFSALE_ALBUM_QUEUE,String.valueOf(albumInfo.getId()));
        }
    }

    @Override
    public void deleteAlbumInfo(Long albumId) {
        removeById(albumId);
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId,albumId);
        albumAttributeValueService.remove(wrapper);
        //删除统计
        LambdaQueryWrapper<AlbumStat> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(AlbumStat::getAlbumId,albumId);
        albumStatService.remove(wrapper1);
        kafkaService.sendMessage(KafkaConstant.OFFSALE_ALBUM_QUEUE,String.valueOf(albumId));

    }
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public boolean isSubscribe(Long albumId) {
        Long userId = AuthContextHolder.getUserId();
        Query query = Query.query(Criteria.where("userId").is(userId).and("albumId").is(albumId));
        long count = mongoTemplate.count(query, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_SUBSCRIBE, userId));
        if(count>0){
            return true;
        }
        return false;

    }

    @Override
    public List<AlbumTempVo> getAlbumTempList(List<Long> albumIdList) {
        List<AlbumInfo> albumInfoList = listByIds(albumIdList);
        return albumInfoList.stream().map(albumInfo -> {
            AlbumTempVo albumTempVo = new AlbumTempVo();
            BeanUtils.copyProperties(albumInfo, albumTempVo);
            albumTempVo.setAlbumId(albumInfo.getId());
            return albumTempVo;
        }).collect(Collectors.toList());
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private AlbumInfo getAlbumInfoFromRedis(Long albumId){
        String cacheKey = RedisConstant.ALBUM_INFO_PREFIX+albumId;
        AlbumInfo albumInfoRedis =(AlbumInfo) redisTemplate.opsForValue().get(cacheKey);
        if(albumInfoRedis==null){
           AlbumInfo albumInfoDB = getAlbumInfoFromDB(albumId);
           redisTemplate.opsForValue().set(cacheKey,albumInfoDB);
           return albumInfoDB;
        }else {
            return albumInfoRedis;
        }
    }

    private AlbumInfo getAlbumInfoFromDB(Long albumId) {
        AlbumInfo albumInfo = getById(albumId);
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId,albumId);
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueService.list(wrapper);
        albumInfo.setAlbumPropertyValueList(albumAttributeValueList);
        return albumInfo;
    }

    private List<AlbumStat> buildAlbumStatData(Long albumId) {
        ArrayList<AlbumStat> albumStatList = new ArrayList<>();
        initAlbumStat(albumId, albumStatList, SystemConstant.PLAY_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.SUBSCRIBE_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.BUY_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.COMMENT_NUM_ALBUM);
        return albumStatList;
    }
    private static void initAlbumStat(Long albumId, ArrayList<AlbumStat> albumStatList, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);
        albumStat.setStatNum(0);
        albumStatList.add(albumStat);
    }

}
