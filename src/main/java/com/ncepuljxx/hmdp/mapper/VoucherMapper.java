package com.ncepuljxx.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncepuljxx.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);

    @Select("select * from tb_voucher where shop_id=#{shopId}")
    List<Voucher> findVoucherByShopId(Long shopId);

    @Select("select * from tb_voucher where id=#{id}")
    Voucher findByIds(Long id);
}
