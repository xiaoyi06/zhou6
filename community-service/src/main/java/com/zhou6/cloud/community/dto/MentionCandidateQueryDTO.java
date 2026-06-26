package com.zhou6.cloud.community.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MentionCandidateQueryDTO extends PageQueryDTO {

    private String keyword;
}
