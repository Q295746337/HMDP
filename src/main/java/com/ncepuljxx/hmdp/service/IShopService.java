package com.ncepuljxx.hmdp.service;

import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

    Shop findShop(String shopName);

    List<Shop> findShopsByKeyword(String keyword);
}
