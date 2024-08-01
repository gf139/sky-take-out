package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@Slf4j
@Api(tags = "套餐管理")
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 修改套餐状态
     * @param status
     * @return
     */
    @ApiOperation("修改套餐状态")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result setstatus(@PathVariable Integer status, Long id){
        log.info("启用禁用套餐:{},{}",status,id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }


    /**
     * 新增套餐
     * @return
     */
    @ApiOperation("新增套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key="#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询菜品
     * @param setmealPageQueryDTO
     * @return
     */
    @ApiOperation("套餐分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("菜品分页查询菜品:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @ApiOperation("删除套餐")
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result deleteSetmeal(@RequestParam List<Long> ids){
        log.info("删除套餐:{}",ids);
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }

    /**
     * 根据套餐id进行查询
     * @param id
     * @return
     */
    @ApiOperation("根据套餐id进行查询")
    @GetMapping("/{id}")
    public Result<SetmealVO> getByIdWithDish(@PathVariable Long id){
        log.info("根据套餐id进行查询:{}",id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealVO
     * @return
     */
    @ApiOperation("修改套餐")
    @PutMapping()
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result setstatus(@RequestBody SetmealVO setmealVO){
        log.info("修改套餐:{}",setmealVO);
        setmealService.update(setmealVO);
        return Result.success();
    }
}
