package com.ncepuljxx.hmdp.service.impl;

import com.ncepuljxx.hmdp.entity.SeckillVoucher;
import com.ncepuljxx.hmdp.mapper.SeckillVoucherMapper;
import com.ncepuljxx.hmdp.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
