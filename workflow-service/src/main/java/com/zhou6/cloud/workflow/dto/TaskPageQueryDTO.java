package com.zhou6.cloud.workflow.dto;
import lombok.Data;
@Data public class TaskPageQueryDTO { private String userId; private Integer pageNum = 1; private Integer pageSize = 10; }
