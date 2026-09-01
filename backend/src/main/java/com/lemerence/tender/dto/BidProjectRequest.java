package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 新增或更新招投标项目的请求数据。
 *
 * @param projectName 项目名称
 * @param tenderNo 招标编号
 * @param tenderer 招标人
 * @param agency 代理机构
 * @param industry 所属行业
 * @param region 项目地区
 * @param budgetAmount 预算金额
 * @param bidAmount 投标金额
 * @param deadline 投标截止时间
 * @param status 项目状态
 * @param result 投标结果
 * @param ownerName 项目负责人
 * @param notes 备注
 */
public record BidProjectRequest(
        @NotBlank String projectName,
        String tenderNo,
        String tenderer,
        String agency,
        String industry,
        String region,
        BigDecimal budgetAmount,
        BigDecimal bidAmount,
        OffsetDateTime deadline,
        String status,
        String result,
        String ownerName,
        String notes
) {}
