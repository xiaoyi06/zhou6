package com.zhou6.cloud.workflow.dto;
import lombok.Data;
@Data public class InitiatedProcessPageQueryDTO { private String status; private Integer pageNum = 1; private Integer pageSize = 10; }
