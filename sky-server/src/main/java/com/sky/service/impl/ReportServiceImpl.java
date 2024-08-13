package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内的营业额数量
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //用于保存begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)){
            //加到日期最后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList){
            //查询某日营业额数量以及状态为已完成的订单金额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover ==null ? 0.0 :turnover;

            //存放每一天的数据进入列表
            turnoverList.add(turnover);
        }

        //封装返回结果
        return TurnoverReportVO.builder()
                .turnoverList(StringUtils.join(turnoverList,","))
                .dateList(StringUtils.join(dateList,","))
                .build();
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //用于保存begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)){
            //加到日期最后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天新增用户数量
        List<Integer> newuserList = new ArrayList<>();
        //存放每天总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dateList){
            //查询某日营业额数量以及状态为已完成的订单金额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("end",endTime);

            //总用户数量
            Integer totalUser = userMapper.sumByMap(map);

            //新增数量
            map.put("begin",beginTime);
            Integer newUser = userMapper.sumByMap(map);

            //存放每一天的数据进入列表
            totalUserList.add(totalUser);
            newuserList.add(newUser);
        }

        //封装返回结果
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newuserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStastistics(LocalDate begin, LocalDate end) {
        //用于保存begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)){
            //加到日期最后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天有效订单总数
        List<Integer> validOrderCountList = new ArrayList<>();

        for(LocalDate date : dateList){
            //查询某日营业额数量以及状态为已完成的订单金额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //每天总订单
            Integer orderCount = getOrderCount(beginTime,endTime,null);

            //每天有效订单（已完成就是）
            Integer validorderCount = getOrderCount(beginTime,endTime,Orders.COMPLETED);

            //计算时间区间内有效订单数量

            //存放每一天的数据进入列表
            validOrderCountList.add(validorderCount);
            orderCountList.add(orderCount);
        }

        //计算时间内订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //计算时间内有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompleteRate = 0.0;
        if(totalOrderCount != 0){
            //订单完成率
            orderCompleteRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //封装返回结果
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompleteRate)
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countByMap(map);
    }

    /**
     * 销量排名TOP10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10ReportStastistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //将此时间段菜品放入
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop(beginTime,endTime);
        //封装为集合
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numbersList = StringUtils.join(numbers, ",");

        //封装返回结果数据
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numbersList)
                .build();
    }

    /**
     * 导出运营数据报表
     * @param httpServletResponse
     * @return
     */
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        //查询数据库
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin,LocalTime.MIN) ,LocalDateTime.of(dateEnd,LocalTime.MIN));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模版文件创建新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格Sheet分页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" +dateEnd);

            //获得第四行
            XSSFRow xssfRow =  sheet.getRow(3);
            xssfRow.getCell(2).setCellValue(businessDataVO.getTurnover());
            xssfRow.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            xssfRow.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第五行
            xssfRow =  sheet.getRow(4);
            xssfRow.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            xssfRow.getCell(4).setCellValue(businessDataVO.getValidOrderCount());

            for(int i = 0;i<30;i++){
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天
                BusinessDataVO businessDataVO1 = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));

                //获得某一行
                xssfRow = sheet.getRow(7 + i);
                xssfRow.getCell(1).setCellValue(date.toString());
                xssfRow.getCell(2).setCellValue(businessDataVO1.getTurnover());
                xssfRow.getCell(3).setCellValue(businessDataVO1.getValidOrderCount());
                xssfRow.getCell(4).setCellValue(businessDataVO1.getOrderCompletionRate());
                xssfRow.getCell(5).setCellValue(businessDataVO1.getUnitPrice());
                xssfRow.getCell(6).setCellValue(businessDataVO1.getNewUsers());
            }

            //通过输出流将Excel保存下载到客户端浏览器
            ServletOutputStream out = httpServletResponse.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
