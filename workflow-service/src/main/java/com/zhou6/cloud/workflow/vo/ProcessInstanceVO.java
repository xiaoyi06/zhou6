package com.zhou6.cloud.workflow.vo;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor; import lombok.Data;
@Data @AllArgsConstructor public class ProcessInstanceVO { private String processInstanceId; private String processDefinitionKey; private String processDefinitionName; private String businessKey; private String status; private String startUserId; private String startUserName; private String currentTaskName; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private Date startTime; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private Date endTime; }
