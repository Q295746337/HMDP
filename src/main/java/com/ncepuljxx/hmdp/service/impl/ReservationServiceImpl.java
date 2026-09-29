package com.ncepuljxx.hmdp.service.impl;

import com.ncepuljxx.hmdp.entity.Reservation;
import com.ncepuljxx.hmdp.mapper.ReservationMapper;
import com.ncepuljxx.hmdp.service.IReservationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class ReservationServiceImpl implements IReservationService {

    @Resource
    private ReservationMapper reservationMapper;

    //1.添加预约信息的方法
    @Override
    public void insert(Reservation reservation) {
        reservationMapper.insert(reservation);
    }

    //2.查询预约信息的方法(根据手机号查询)
    @Override
    public List<Reservation> findByPhone(String phone) {
        return reservationMapper.findByPhone(phone);
    }
}
