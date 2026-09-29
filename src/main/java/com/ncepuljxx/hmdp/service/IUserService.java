package com.ncepuljxx.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ncepuljxx.hmdp.dto.LoginFormDTO;
import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
