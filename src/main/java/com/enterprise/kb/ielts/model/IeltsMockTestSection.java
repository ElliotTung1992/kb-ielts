package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsMockTestSection {
    /** 主键 */
    private UUID id;
    /** 所属模考记录 ID */
    private UUID mockTestId;
    /** 技能：LISTENING / READING / WRITING / SPEAKING */
    private String skill;
    /** 单项分数 */
    private BigDecimal score;
    /** 原始得分，如听力/阅读正确题数 */
    private Integer rawScore;
    /** 总题数 */
    private Integer questionCount;
    /** 错题数 */
    private Integer wrongCount;
    /** 主要问题或复盘结论 */
    private String mainIssues;
    /** 创建时间 */
    private Instant createdAt;
}
