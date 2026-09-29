package com.ncepuljxx.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.Shop;
import com.ncepuljxx.hmdp.entity.ShopType;
import com.ncepuljxx.hmdp.mapper.ShopTypeMapper;
import com.ncepuljxx.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ncepuljxx.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryShopTypeSet() {
        String key = CACHE_SHOP_TYPE_KEY;
        Set<String> shopTypeJsonSet = stringRedisTemplate.opsForZSet().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2.判断是否存在
        if (!shopTypeJsonSet.isEmpty()) {
            // 3.存在，直接返回
            List<ShopType> shopTypes = new ArrayList<>();
            for (String json : shopTypeJsonSet) {
                shopTypes.add(JSONUtil.toBean(json, ShopType.class));
            }
            return Result.ok(shopTypes);
        }
        // 4.不存在，查询数据库
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //5.不存在，返回错误
        if (shopTypes == null || shopTypes.isEmpty()) {
            return Result.fail("分类不存在！");
        }
        //6.存在，写入redis
        for (ShopType shopType : shopTypes) {
            stringRedisTemplate.opsForZSet().add(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType), shopType.getSort());
        }
        //7.返回
        return Result.ok(shopTypes);
    }
}
