package com.enterprise.kb.ielts.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 模考分数趋势响应。
 */
public record MockTestTrendResponse(
        /** 总分趋势 */
        List<Point> overall,
        /** 听力趋势 */
        List<Point> listening,
        /** 阅读趋势 */
        List<Point> reading,
        /** 写作趋势 */
        List<Point> writing,
        /** 口语趋势 */
        List<Point> speaking
) {
    /**
     * 趋势图上的单个数据点。
     */
    public record Point(
            /** 模考日期 */
            LocalDate date,
            /** 分数 */
            BigDecimal score
    ) {}
}
