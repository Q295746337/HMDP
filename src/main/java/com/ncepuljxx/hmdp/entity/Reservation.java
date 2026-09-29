package com.ncepuljxx.hmdp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 到店消费预约单
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long id;
    private String name;
    private String phone;
    private LocalDateTime communicationTime;
    private String shopName;
}
