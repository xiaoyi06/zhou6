package com.zhou6.cloud.community.service;

import java.util.List;

import com.zhou6.cloud.community.dto.CommentSaveDTO;
import com.zhou6.cloud.community.dto.CommentTreeQueryDTO;
import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.vo.CommentVO;

public interface CommunityCommentService {

    String add(CommentSaveDTO dto);

    List<CommentVO> tree(CommentTreeQueryDTO dto);

    void delete(IdDTO dto);

    void like(IdDTO dto);

    void unlike(IdDTO dto);
}
