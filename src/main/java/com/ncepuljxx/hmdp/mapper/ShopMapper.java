package com.ncepuljxx.hmdp.mapper;

import com.ncepuljxx.hmdp.entity.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface ShopMapper extends BaseMapper<Shop> {

    @Select("select * from tb_shop where name=#{shopName}")
    Shop findShop(String shopName);

    @Select("select * from tb_shop where name like concat('%',#{keyword},'%')")
    List<Shop> findShopsByKeyword(String keyword);
}
