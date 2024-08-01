package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.Dish;
import com.sky.entity.User;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);


}
