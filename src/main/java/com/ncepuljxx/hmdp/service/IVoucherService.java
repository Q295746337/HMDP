package com.ncepuljxx.hmdp.service;

import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    List<Voucher> findVoucherByShopName(String shopName);

    List<Voucher> findVoucherByUserPhone(String userPhone);

}
