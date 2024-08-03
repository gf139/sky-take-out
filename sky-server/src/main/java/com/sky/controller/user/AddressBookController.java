package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址端相关接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;


    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @ApiOperation("新增地址")
    @PostMapping()
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址信息:{}",addressBook);
        addressBookService.add(addressBook);
        return Result.success();
    }

    /**
     * 查看地址
     * @return
     */
    @ApiOperation("查看地址")
    @GetMapping("/list")
    public Result<List<AddressBook>> list(){
        log.info("查看地址");
        List<AddressBook> list = addressBookService.list();
        return Result.success(list);
    }

    /**
     * 查看默认地址
     * @return
     */
    @ApiOperation("查看默认地址")
    @GetMapping("/default")
    public Result<AddressBook> listDefault(){
        log.info("查看默认地址");
        AddressBook list = addressBookService.listDefault();
        return Result.success(list);
    }

    /**
     * 修改地址
     * @return
     */
    @ApiOperation("修改地址")
    @PutMapping()
    public Result update(@RequestBody AddressBook addressBook){
        log.info("修改地址:{}",addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 删除地址
     * @return
     */
    @ApiOperation("删除地址")
    @DeleteMapping()
    public Result update(@RequestParam Long id){
        log.info("删除地址:{}",id);
        addressBookService.delteById(id);
        return Result.success();
    }

    /**
     * 根据id查询地址
     * @return
     */
    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> listById(@PathVariable Long id){
        log.info("根据id查询地址:{}",id);
        AddressBook addressBook = addressBookService.listById(id);
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     * @return
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        Long id = addressBook.getId();
        log.info("设置默认地址:{}",id);
        addressBookService.setDefault(id);
        return Result.success();
    }
}
