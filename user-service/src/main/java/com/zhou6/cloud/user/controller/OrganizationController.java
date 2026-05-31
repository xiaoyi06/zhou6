package com.zhou6.cloud.user.controller;

import java.io.IOException;
import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.OrgAddDTO;
import com.zhou6.cloud.user.dto.OrgChangeStatusDTO;
import com.zhou6.cloud.user.dto.OrgChildrenQueryDTO;
import com.zhou6.cloud.user.vo.OrgDetailVO;
import com.zhou6.cloud.user.dto.OrgEditDTO;
import com.zhou6.cloud.user.dto.OrgIdDTO;
import com.zhou6.cloud.user.dto.OrgTreeQueryDTO;
import com.zhou6.cloud.user.vo.OrgTreeVO;
import com.zhou6.cloud.user.dto.OrgUserAddDTO;
import com.zhou6.cloud.user.dto.OrgUserPageDTO;
import com.zhou6.cloud.user.dto.OrgUserRemoveDTO;
import com.zhou6.cloud.user.dto.OrgUserSetPrimaryDTO;
import com.zhou6.cloud.user.vo.OrgUserVO;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织机构接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@Tag(name = "组织机构管理", description = "维护单位/公司、部门、班组树，以及组织机构与用户的关系")
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 新增组织机构。
     *
     * @param dto 新增部门参数
     * @return 空响应
     */
    @PostMapping("/add")
    @Operation(summary = "新增组织机构", description = "新增单位/公司、部门或班组，并自动维护树路径和层级")
    public R<Void> add(@RequestBody OrgAddDTO dto) {
        organizationService.addOrganization(dto);
        return R.ok(null);
    }

    /**
     * 修改组织机构。
     *
     * @param dto 修改部门参数
     * @return 空响应
     */
    @PostMapping("/edit")
    @Operation(summary = "修改组织机构", description = "修改组织机构基础信息；父级变化时同步刷新所有子级树路径")
    public R<Void> edit(@RequestBody OrgEditDTO dto) {
        organizationService.editOrganization(dto);
        return R.ok(null);
    }

    /**
     * 删除组织机构及其子级。
     *
     * @param dto 部门 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    @Operation(summary = "删除组织机构", description = "逻辑删除指定组织机构及其所有子级，并清理用户与组织机构关系")
    public R<Void> delete(@RequestBody OrgIdDTO dto) {
        organizationService.deleteOrganization(dto);
        return R.ok(null);
    }

    /**
     * 修改组织机构启停状态。
     *
     * @param dto 状态修改参数
     * @return 空响应
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "修改组织机构状态", description = "启用或停用指定组织机构")
    public R<Void> changeStatus(@RequestBody OrgChangeStatusDTO dto) {
        organizationService.changeStatus(dto);
        return R.ok(null);
    }

    /**
     * 查询组织机构详情。
     *
     * @param dto 部门 ID 参数
     * @return 部门详情
     */
    @PostMapping("/getById")
    @Operation(summary = "查询组织机构详情", description = "根据机构ID查询未删除组织机构的完整信息")
    public R<OrgDetailVO> getById(@RequestBody OrgIdDTO dto) {
        return R.ok(organizationService.getById(dto));
    }

    /**
     * 查询完整组织机构树。
     *
     * @param dto 树查询过滤参数
     * @return 部门树
     */
    @PostMapping("/getTree")
    @Operation(summary = "查询组织机构树", description = "查询全部未删除组织机构，并按父子关系组装树结构")
    public R<List<OrgTreeVO>> getTree(@RequestBody OrgTreeQueryDTO dto) {
        return R.ok(organizationService.getTree(dto));
    }

    /**
     * 导出组织机构列表。
     *
     * @param dto 导出过滤参数
     * @param response 文件响应
     */
    @PostMapping("/export")
    @Operation(summary = "导出组织机构列表", description = "按查询条件导出 sys_organization 扁平列表，不按树结构输出")
    public void export(@RequestBody OrgTreeQueryDTO dto, HttpServletResponse response) throws IOException {
        organizationService.export(dto, response);
    }

    /**
     * 懒加载查询直接子级组织机构。
     *
     * @param dto 父部门和过滤参数
     * @return 子部门列表
     */
    @PostMapping("/getChildren")
    @Operation(summary = "查询直接子级组织机构", description = "根据父级机构ID查询一层子级，适用于树组件懒加载")
    public R<List<OrgDetailVO>> getChildren(@RequestBody OrgChildrenQueryDTO dto) {
        return R.ok(organizationService.getChildren(dto));
    }

    /**
     * 查询指定组织机构下所有层级子级。
     *
     * @param dto 部门 ID 参数
     * @return 子孙部门列表
     */
    @PostMapping("/getDescendants")
    @Operation(summary = "查询所有子级组织机构", description = "根据树路径查询指定组织机构下的全部后代节点")
    public R<List<OrgDetailVO>> getDescendants(@RequestBody OrgIdDTO dto) {
        return R.ok(organizationService.getDescendants(dto));
    }

    /**
     * 分页查询组织机构人员。
     *
     * @param dto 分页查询参数
     * @return 部门人员分页结果
     */
    @PostMapping("/user/page")
    @Operation(summary = "分页查询组织机构人员", description = "查询指定组织机构下的用户列表，并标识是否主部门")
    public R<PageResponse<OrgUserVO>> pageUsers(@RequestBody OrgUserPageDTO dto) {
        return R.ok(organizationService.pageUsers(dto));
    }

    /**
     * 向组织机构添加人员。
     *
     * @param dto 部门人员新增参数
     * @return 空响应
     */
    @PostMapping("/user/add")
    @Operation(summary = "向组织机构添加人员", description = "批量维护用户与组织机构关系，可同时设置为主部门")
    public R<Void> addUsers(@RequestBody OrgUserAddDTO dto) {
        organizationService.addUsers(dto);
        return R.ok(null);
    }

    /**
     * 从组织机构移除人员。
     *
     * @param dto 部门人员移除参数
     * @return 空响应
     */
    @PostMapping("/user/remove")
    @Operation(summary = "从组织机构移除人员", description = "删除指定用户与指定组织机构的关系")
    public R<Void> removeUser(@RequestBody OrgUserRemoveDTO dto) {
        organizationService.removeUser(dto);
        return R.ok(null);
    }

    /**
     * 设置用户主组织机构。
     *
     * @param dto 主部门设置参数
     * @return 空响应
     */
    @PostMapping("/user/setPrimary")
    @Operation(summary = "设置用户主组织机构", description = "将指定组织机构设置为用户主部门，并清除该用户其他主部门标识")
    public R<Void> setPrimary(@RequestBody OrgUserSetPrimaryDTO dto) {
        organizationService.setPrimary(dto);
        return R.ok(null);
    }
}
