package com.zhou6.cloud.user.service;

import java.util.List;

import com.zhou6.cloud.user.dto.OrgAddDTO;
import com.zhou6.cloud.user.dto.OrgChangeStatusDTO;
import com.zhou6.cloud.user.dto.OrgChildrenQueryDTO;
import com.zhou6.cloud.user.dto.OrgDetailVO;
import com.zhou6.cloud.user.dto.OrgEditDTO;
import com.zhou6.cloud.user.dto.OrgIdDTO;
import com.zhou6.cloud.user.dto.OrgTreeQueryDTO;
import com.zhou6.cloud.user.dto.OrgTreeVO;
import com.zhou6.cloud.user.dto.OrgUserAddDTO;
import com.zhou6.cloud.user.dto.OrgUserPageDTO;
import com.zhou6.cloud.user.dto.OrgUserRemoveDTO;
import com.zhou6.cloud.user.dto.OrgUserSetPrimaryDTO;
import com.zhou6.cloud.user.dto.OrgUserVO;
import com.zhou6.cloud.user.dto.PageResponse;

/**
 * 组织部门业务接口，统一承载组织维护、组织树和部门人员关系操作。
 */
public interface OrganizationService {

    /**
     * 新增部门。
     *
     * @param dto 新增部门参数
     */
    void addOrganization(OrgAddDTO dto);

    /**
     * 修改部门。
     *
     * @param dto 修改部门参数
     */
    void editOrganization(OrgEditDTO dto);

    /**
     * 删除部门及其子部门。
     *
     * @param dto 部门 ID 参数
     */
    void deleteOrganization(OrgIdDTO dto);

    /**
     * 修改部门启停状态。
     *
     * @param dto 状态修改参数
     */
    void changeStatus(OrgChangeStatusDTO dto);

    /**
     * 查询部门详情。
     *
     * @param dto 部门 ID 参数
     * @return 部门详情
     */
    OrgDetailVO getById(OrgIdDTO dto);

    /**
     * 查询完整部门树。
     *
     * @param dto 树查询过滤参数
     * @return 部门树列表
     */
    List<OrgTreeVO> getTree(OrgTreeQueryDTO dto);

    /**
     * 懒加载查询直接子部门。
     *
     * @param dto 父部门和过滤参数
     * @return 子部门列表
     */
    List<OrgDetailVO> getChildren(OrgChildrenQueryDTO dto);

    /**
     * 查询指定部门下所有层级子部门。
     *
     * @param dto 部门 ID 参数
     * @return 子孙部门列表
     */
    List<OrgDetailVO> getDescendants(OrgIdDTO dto);

    /**
     * 分页查询部门人员。
     *
     * @param dto 分页查询参数
     * @return 部门人员分页结果
     */
    PageResponse<OrgUserVO> pageUsers(OrgUserPageDTO dto);

    /**
     * 向部门添加人员。
     *
     * @param dto 部门人员新增参数
     */
    void addUsers(OrgUserAddDTO dto);

    /**
     * 从部门移除人员。
     *
     * @param dto 部门人员移除参数
     */
    void removeUser(OrgUserRemoveDTO dto);

    /**
     * 设置用户主部门。
     *
     * @param dto 主部门设置参数
     */
    void setPrimary(OrgUserSetPrimaryDTO dto);
}
