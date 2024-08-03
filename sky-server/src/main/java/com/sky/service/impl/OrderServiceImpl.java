package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    public void add(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        orderMapper.add(addressBook);
    }

    /**
     * 查看地址
     * @return
     */
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = orderMapper.getById(userId);
        return list;
    }

    /**
     * 查看默认地址
     * @return
     */
    public AddressBook listDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook list = orderMapper.getDefaultById(userId);
        return list;
    }

    /**
     * 修改地址
     * @return
     */
    public void update(AddressBook addressBook) {
        orderMapper.update(addressBook);
    }

    /**
     * 删除地址
     * @return
     */
    public void delteById(Long id) {
        orderMapper.deleteById(id);
    }

    /**
     * 根据id查询地址
     * @return
     */
    public AddressBook listById(Long id) {
        AddressBook addressBook = orderMapper.listById(id);
        return addressBook;
    }

    /**
     * 设置默认地址
     * @return
     */
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();
        //得到原来默认地址
        AddressBook list = orderMapper.getDefaultById(userId);
        //将原来默认地址修改为非
        list.setIsDefault(0);

        log.info("111");
        //传入进行修改将原来默认地址改为非
        orderMapper.update(list);

        //设置默认地址
        orderMapper.setDefault(id);
    }

}
