package com.ncepuljxx.hmdp.service;

import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IShopTypeService extends IService<ShopType> {
    Result queryShopTypeSet();
}
