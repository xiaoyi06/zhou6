package com.zhou6.cloud.sys.dto;

import java.time.LocalDateTime;

/**
 * 已完成类型校验的登录日志删除条件，仅供持久层使用。
 */
public record LoginLogDeleteParam(Long id, LocalDateTime loginTime) {
}
