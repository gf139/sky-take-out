package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@Api(tags = "菜品相关接口")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 新增菜品和对应口味
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //精确清理缓存
        String key = "dish_" + dishDTO.getCategoryId();
        cleanredis(key);

        return Result.success();
    }

    /**
     * 菜品分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询菜品:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @ApiOperation("菜品批量删除")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除:{}",ids);
        dishService.deleteBatch(ids);

        //全局清除缓存
        cleanredis("dish_*");
        return Result.success();
    }

    /**
     * 根据ID查询菜品和对应的口味数据
     * @return
     */
    @ApiOperation("根据ID查询菜品和对应的口味数据")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据ID查询菜品:{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //全局清除缓存
        cleanredis("dish_*");
        return Result.success();
    }

    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类ID查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类ID查询菜品{}",categoryId);
        List<Dish> dish = dishService.list(categoryId);
        return Result.success(dish);
    }

    /**
     * 修改套餐状态
     * @param status
     * @return
     */
    @ApiOperation("修改菜品状态")
    @PostMapping("/status/{status}")
    public Result setstatus(@PathVariable Integer status, Long id){
        log.info("修改菜品状态:{},{}",status,id);
        dishService.startOrStop(status,id);

        //全局清除缓存
        cleanredis("dish_*");
        return Result.success();
    }

    /**
     * 清除缓存
     * @param pattern
     */
    private void cleanredis(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
