package com.sky.service;


import com.sky.entity.AddressBook;

import java.util.List;

public interface OrderService {

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    void add(AddressBook addressBook);

    /**
     * 查看地址
     * @return
     */
    List<AddressBook> list();

    /**
     * 查看默认地址
     * @return
     */
    AddressBook listDefault();

    /**
     * 修改地址
     * @return
     */
    void update(AddressBook addressBook);

    /**
     * 删除地址
     * @return
     */
    void delteById(Long id);

    /**
     * 根据id查询地址
     * @return
     */
    AddressBook listById(Long id);

    /**
     * 设置默认地址
     * @return
     */
    void setDefault(Long id);
}
