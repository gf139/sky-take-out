package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {

    /**
     * 统计指定时间区间内的营业额数量
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStastistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名TOP10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10ReportStastistics(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     * @param httpServletResponse
     * @return
     */
    void exportBusinessData(HttpServletResponse httpServletResponse);
}
