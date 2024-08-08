package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 订单明细数据
     *
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 取消订单
     * @param id
     * @return
     */
    void delete(Integer id);

    /**
     * 再来一单
     * @param orderVO
     * @return
     */
    List<OrderDetail> getByOrderId(OrderVO orderVO);
}
