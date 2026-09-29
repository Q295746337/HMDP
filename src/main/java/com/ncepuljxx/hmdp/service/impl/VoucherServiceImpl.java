package com.ncepuljxx.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.Shop;
import com.ncepuljxx.hmdp.entity.Voucher;
import com.ncepuljxx.hmdp.mapper.ShopMapper;
import com.ncepuljxx.hmdp.mapper.VoucherMapper;
import com.ncepuljxx.hmdp.mapper.VoucherOrderMapper;
import com.ncepuljxx.hmdp.entity.SeckillVoucher;
import com.ncepuljxx.hmdp.service.ISeckillVoucherService;
import com.ncepuljxx.hmdp.service.IVoucherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.ncepuljxx.hmdp.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private VoucherMapper voucherMapper;

    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    //1.查询商家的优惠券信息(商家名称先精确匹配,匹配不到时模糊匹配唯一结果)
    @Override
    public List<Voucher> findVoucherByShopName(String shopName) {
        Shop shop = shopMapper.findShop(shopName);
        if (shop == null) {
            // 精确匹配不到时尝试模糊匹配,仅当唯一匹配时才使用
            List<Shop> shops = shopMapper.findShopsByKeyword(shopName);
            if (shops.size() == 1) {
                shop = shops.get(0);
            }
        }
        if (shop == null) {
            return new ArrayList<>();
        }
        return getBaseMapper().findVoucherByShopId(shop.getId());
    }

    //2.查询用户拥有的优惠券
    public List<Voucher> findVoucherByUserPhone(String userPhone) {
        List<Long> voucherIds = voucherOrderMapper.findByPhone(userPhone);
        String ids = StringUtils.join(voucherIds, ",");
        // System.out.println("用户拥有的优惠券id是: " + ids);
        List<Voucher> vouchers = new ArrayList<>();
        for (Long voucherId : voucherIds) {
            vouchers.add(voucherMapper.findByIds(voucherId));
        }
        // System.out.println("查询用户拥有的优惠券: " + vouchers);
        return vouchers;
    }

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
