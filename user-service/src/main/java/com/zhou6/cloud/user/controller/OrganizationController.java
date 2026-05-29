package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
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
import com.zhou6.cloud.user.service.OrganizationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织部门接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 新增部门。
     *
     * @param dto 新增部门参数
     * @return 空响应
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody OrgAddDTO dto) {
        organizationService.addOrganization(dto);
        return R.ok(null);
    }

    /**
     * 修改部门。
     *
     * @param dto 修改部门参数
     * @return 空响应
     */
    @PostMapping("/edit")
    public R<Void> edit(@RequestBody OrgEditDTO dto) {
        organizationService.editOrganization(dto);
        return R.ok(null);
    }

    /**
     * 删除部门及其子部门。
     *
     * @param dto 部门 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody OrgIdDTO dto) {
        organizationService.deleteOrganization(dto);
        return R.ok(null);
    }

    /**
     * 修改部门启停状态。
     *
     * @param dto 状态修改参数
     * @return 空响应
     */
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody OrgChangeStatusDTO dto) {
        organizationService.changeStatus(dto);
        return R.ok(null);
    }

    /**
     * 查询部门详情。
     *
     * @param dto 部门 ID 参数
     * @return 部门详情
     */
    @PostMapping("/getById")
    public R<OrgDetailVO> getById(@RequestBody OrgIdDTO dto) {
        return R.ok(organizationService.getById(dto));
    }

    /**
     * 查询完整部门树。
     *
     * @param dto 树查询过滤参数
     * @return 部门树
     */
    @PostMapping("/getTree")
    public R<List<OrgTreeVO>> getTree(@RequestBody OrgTreeQueryDTO dto) {
        return R.ok(organizationService.getTree(dto));
    }

    /**
     * 懒加载查询直接子部门。
     *
     * @param dto 父部门和过滤参数
     * @return 子部门列表
     */
    @PostMapping("/getChildren")
    public R<List<OrgDetailVO>> getChildren(@RequestBody OrgChildrenQueryDTO dto) {
        return R.ok(organizationService.getChildren(dto));
    }

    /**
     * 查询指定部门下所有层级子部门。
     *
     * @param dto 部门 ID 参数
     * @return 子孙部门列表
     */
    @PostMapping("/getDescendants")
    public R<List<OrgDetailVO>> getDescendants(@RequestBody OrgIdDTO dto) {
        return R.ok(organizationService.getDescendants(dto));
    }

    /**
     * 分页查询部门人员。
     *
     * @param dto 分页查询参数
     * @return 部门人员分页结果
     */
    @PostMapping("/user/page")
    public R<PageResponse<OrgUserVO>> pageUsers(@RequestBody OrgUserPageDTO dto) {
        return R.ok(organizationService.pageUsers(dto));
    }

    /**
     * 向部门添加人员。
     *
     * @param dto 部门人员新增参数
     * @return 空响应
     */
    @PostMapping("/user/add")
    public R<Void> addUsers(@RequestBody OrgUserAddDTO dto) {
        organizationService.addUsers(dto);
        return R.ok(null);
    }

    /**
     * 从部门移除人员。
     *
     * @param dto 部门人员移除参数
     * @return 空响应
     */
    @PostMapping("/user/remove")
    public R<Void> removeUser(@RequestBody OrgUserRemoveDTO dto) {
        organizationService.removeUser(dto);
        return R.ok(null);
    }

    /**
     * 设置用户主部门。
     *
     * @param dto 主部门设置参数
     * @return 空响应
     */
    @PostMapping("/user/setPrimary")
    public R<Void> setPrimary(@RequestBody OrgUserSetPrimaryDTO dto) {
        organizationService.setPrimary(dto);
        return R.ok(null);
    }
}
