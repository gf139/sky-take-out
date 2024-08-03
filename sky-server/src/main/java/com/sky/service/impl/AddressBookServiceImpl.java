package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;


    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    public void add(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        addressBookMapper.add(addressBook);
    }

    /**
     * 查看地址
     * @return
     */
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.getById(userId);
        return list;
    }

    /**
     * 查看默认地址
     * @return
     */
    public AddressBook listDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook list = addressBookMapper.getDefaultById(userId);
        return list;
    }

    /**
     * 修改地址
     * @return
     */
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 删除地址
     * @return
     */
    public void delteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    /**
     * 根据id查询地址
     * @return
     */
    public AddressBook listById(Long id) {
        AddressBook addressBook = addressBookMapper.listById(id);
        return addressBook;
    }

    /**
     * 设置默认地址
     * @return
     */
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();
        //得到原来默认地址
        AddressBook list = addressBookMapper.getDefaultById(userId);
        //将原来默认地址修改为非
        list.setIsDefault(0);

        //传入进行修改将原来默认地址改为非
        addressBookMapper.update(list);

        //设置默认地址
        addressBookMapper.setDefault(id);
    }

}
