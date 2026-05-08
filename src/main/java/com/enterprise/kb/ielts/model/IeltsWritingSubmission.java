package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsWritingSubmission {
    /** 主键 */
    private UUID id;
    /** 关联写作任务 ID，可为空表示自由作文 */
    private UUID taskId;
    /** 用户提交的作文原文 */
    private String originalText;
    /** 修改后的作文文本 */
    private String revisedText;
    /** 目标 band 分 */
    private BigDecimal targetBand;
    /** 预估 band 分 */
    private BigDecimal estimatedBand;
    /** 反馈意见 */
    private String feedback;
    /** 四项评分明细，当前以文本或 JSON 字符串保存 */
    private String criteriaScores;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
}
