package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class IeltsMockTest {
    /** 主键 */
    private UUID id;
    /** 模考日期 */
    private LocalDate testDate;
    /** 模考来源，如 Cambridge、真题套卷、手动记录 */
    private String source;
    /** 模考总分 */
    private BigDecimal overallScore;
    /** 复盘备注 */
    private String notes;
    /** 下阶段训练重点 */
    private String nextFocus;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
    /** 非列字段，四科分项成绩 */
    private List<IeltsMockTestSection> sections;
}
