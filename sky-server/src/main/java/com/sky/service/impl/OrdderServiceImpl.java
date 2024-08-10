package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;


    /**
     * 发送订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种异常业务
        AddressBook addressBook = addressBookMapper.listById(ordersSubmitDTO.getAddressBookId());
        //地址表
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车表
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list == null || list.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //添加一条订单
        Orders orders = new Orders();
        String address = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setAddress(address);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //添加多条订单细节
        for(ShoppingCart cart : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //封装VO返回
        OrderSubmitVO orderSubmitVO =OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单

//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

        paySuccess(ordersPaymentDTO.getOrderNumber());

        String orderNumber = ordersPaymentDTO.getOrderNumber(); //订单号

        Long orderid = orderMapper.getorderId(orderNumber);//根据订单号查主键



        JSONObject jsonObject = new JSONObject();//本来没有2

        jsonObject.put("code", "ORDERPAID"); //本来没有3

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);

        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改

        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付

        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新

        LocalDateTime check_out_time = LocalDateTime.now();

        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderid);

        return vo;  //  修改支付方法中的代码
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);

        for(OrderVO ov:page){
            List<OrderDetail> detailsByUserId = orderMapper.getDetailsByUserId(ov.getId());
            //设置菜品细节
            ov.setOrderDetailList(detailsByUserId);
            //查询地址
            AddressBook addressBook = addressBookMapper.listById(ov.getAddressBookId());
            String address = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
            ov.setAddress(address);
        }

        long total = page.getTotal();
        List<OrderVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);

        for(OrderVO ov:page){
            //查询地址
            AddressBook addressBook = addressBookMapper.listById(ov.getAddressBookId());
            String address = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
            ov.setAddress(address);
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ov);
            String orders = new String();
            for(OrderDetail orderDetail:orderDetails){
                orders+=orderDetail.getNumber() + "份" + orderDetail.getName() +"、";
            }
            ov.setOrderDishes(orders);
        }

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO

        long total = page.getTotal();
        List<OrderVO> records = page.getResult();

        return new PageResult(total, records);

    }

    /**
     * 各个状态的订单数量统计
     * @param
     * @return
     */
    public OrderStatisticsVO statistics() {
        //先查找出各个的数量列表
        List<Orders> list = orderMapper.getAll();

        Long toBeConfirmed = new Long(0);
        Long confirmed = new Long(0);
        Long deliveryInProgress = new Long(0);

        //将其赋值给变量
        for(Orders od:list){
            if(od.getStatus() == 2){
                toBeConfirmed+=1;
            } else if (od.getStatus() == 3) {
                confirmed +=1 ;
            } else if (od.getStatus() == 4) {
                deliveryInProgress +=1;
            }
        }

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(Math.toIntExact(confirmed));
        orderStatisticsVO.setToBeConfirmed(Math.toIntExact(toBeConfirmed));
        orderStatisticsVO.setDeliveryInProgress(Math.toIntExact(deliveryInProgress));

        //进行返回
        return orderStatisticsVO;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    public OrderVO getorderDetail(Integer id){

        //数据封装到vo里
        OrderVO orderVO = new OrderVO();
        orderVO.setId(Long.valueOf(id));
        Orders orders = orderMapper.get(orderVO);

        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        BeanUtils.copyProperties(orders,orderVO);

        //将vo的list进行查询
        List<OrderDetail> detailsByUserId = orderMapper.getDetailsByUserId(orderVO.getId());

        //查询地址
        orderVO.setAddress(orderVO.getAddress());

        //查询赋值后返回
        orderVO.setOrderDetailList(detailsByUserId);

        return orderVO;
    }

    /**直接删掉不太合理，其实还得保存订单，只是修改订单状态
     * 取消订单
     * @param id
     * @return
     */
    public void cancelOrder(Integer id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(Long.valueOf(id));

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
//        orderMapper.delete(id);
//        orderDetailMapper.delete(id);
        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            log.info("已退款:{}",ordersDB.getAmount());
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @Transactional
    public void repetition(Integer id) {
        //复杂
//        //将原有订单进行复制
//        OrderVO orderVO = new OrderVO();
//        orderVO.setId(Long.valueOf(id));
//
//        //获取原有订单
//        Orders orders = orderMapper.get(orderVO);
//
//        AddressBook addressBook = addressBookMapper.listById(orders.getAddressBookId());
//
//        //添加一条订单
//        if(orders == null){
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//        }
//        orders.setOrderTime(LocalDateTime.now());
//        orders.setPayStatus(Orders.UN_PAID);
//        orders.setStatus(Orders.PENDING_PAYMENT);
//        orders.setNumber(String.valueOf(System.currentTimeMillis()));
//        orders.setPhone(addressBook.getPhone());
//        orders.setConsignee(addressBook.getConsignee());
//        orders.setUserId(orders.getUserId());
//
//        orderMapper.insert(orders);
//
//        List<OrderDetail> orderDetailList = new ArrayList<>();
//
//        //获得原来订单细节的菜品列表
//        List<OrderDetail> list = orderDetailMapper.getByOrderId(orderVO);
//
//        //添加多条订单细节
//        for(OrderDetail cart : list){
//            OrderDetail orderDetail = new OrderDetail();
//            BeanUtils.copyProperties(cart,orderDetail);
//            orderDetail.setOrderId(orders.getId());
//            orderDetailList.add(orderDetail);
//        }
//
//        orderDetailMapper.insertBatch(orderDetailList);
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();
        OrderVO orderVO = new OrderVO();
        orderVO.setId(Long.valueOf(id));

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderVO);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 接单
     * @param id
     * @return
     */
    public void confirm(Integer id) {
        Orders orders = new Orders().builder()
                .id(Long.valueOf(id))
                .status(Orders.CONFIRMED)
                .build();
        //更新状态为接单状态
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();

        Orders orders = new Orders().builder()
                .id(Long.valueOf(ordersRejectionDTO.getId()))
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();

        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            orders.setStatus(7);
            log.info("申请退款：{}",ordersDB.getAmount());
        }

        //更新状态为接单状态
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        Orders orders = new Orders().builder()
                .id(Long.valueOf(ordersCancelDTO.getId()))
                .cancelReason(ordersCancelDTO.getCancelReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();
        if (payStatus == 1) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            orders.setStatus(7);
            log.info("申请退款：{}",ordersDB.getAmount());
        }

        //更新状态为接单状态
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //支付状态
        Integer status = ordersDB.getStatus();

        // 订单只有存在且已付款才可以派送
        if (ordersDB == null || status != 3) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders().builder()
                .id(Long.valueOf(id))
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        //更新状态为接单状态
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        

        // 订单只有存在且派送中
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders().builder()
                .id(Long.valueOf(id))
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        //更新状态为接单状态
        orderMapper.update(orders);
    }
}
