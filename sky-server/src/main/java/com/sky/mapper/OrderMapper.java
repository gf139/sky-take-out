package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    /**
     * 获取orderid的mapper层方法，写在OrderMapper.java文件下
     * @param orderNumber
     * @return
     */
    @Select("select id from orders where number=#{orderNumber}")
    Long getorderId (String orderNumber);


    /**
     * 用于替换微信支付更新数据库状态的问题
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus,LocalDateTime check_out_time, Long id);


    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);



    /**
     * 根据ID查询细节
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getDetailsByUserId(Long orderId);

    /**
     * 查询语句
     * @param orderVO
     * @return
     */
    Orders get(OrderVO orderVO);

    /**
     * 取消订单
     * @param id
     * @return
     */
    void delete(Integer id);

    /**
     * 各个状态的订单数量统计
     * @param
     * @return
     */
    @Select("select * from orders")
    List<Orders> getAll();

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);
}
