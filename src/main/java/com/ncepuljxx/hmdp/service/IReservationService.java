package com.ncepuljxx.hmdp.service;

import com.ncepuljxx.hmdp.entity.Reservation;

import java.util.List;

/**
 * <p>
 *  预约服务类
 * </p>
 *
 */
public interface IReservationService {

    /**
     * 添加预约信息
     *
     * @param reservation 预约单数据
     */
    void insert(Reservation reservation);

    /**
     * 根据手机号查询预约单
     *
     * @param phone 用户手机号
     * @return 预约单列表
     */
    List<Reservation> findByPhone(String phone);
}
