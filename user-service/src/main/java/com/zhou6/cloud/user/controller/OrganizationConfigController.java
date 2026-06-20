package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.dto.OrgConfigAssignUsersDTO;
import com.zhou6.cloud.user.dto.OrgConfigRemoveUserDTO;
import com.zhou6.cloud.user.dto.OrgConfigUnassignedQueryDTO;
import com.zhou6.cloud.user.dto.OrgConfigUserQueryDTO;
import com.zhou6.cloud.user.vo.OrgConfigUserVO;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 机构配置接口，负责维护机构与用户的关联关系。
 */
@Tag(name = "机构配置", description = "维护机构与用户的关联关系，支持批量分配和移除")
@RestController
@RequestMapping(UserApiPathConstants.ORGANIZATION_CONFIG)
public class OrganizationConfigController {

    private final OrganizationService organizationService;

    public OrganizationConfigController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 分页查询机构已分配用户。
     *
     * @param dto 查询参数
     * @return 已分配用户分页结果
     */
    @PostMapping("/users")
    @Operation(summary = "查询机构已分配用户", description = "分页查询指定机构下已分配的用户，支持用户名和昵称过滤")
    public R<PageResponse<OrgConfigUserVO>> users(@RequestBody OrgConfigUserQueryDTO dto) {
        return R.ok(organizationService.users(dto));
    }

    /**
     * 批量分配用户到机构。
     *
     * @param dto 分配参数
     * @return 空响应
     */
    @PostMapping("/assignUsers")
    @Operation(summary = "分配用户到机构", description = "批量将用户分配到指定机构")
    public R<Void> assignUsers(@RequestBody OrgConfigAssignUsersDTO dto) {
        organizationService.assignUsers(dto);
        return R.ok(null);
    }

    /**
     * 从机构移除用户。
     *
     * @param dto 移除参数
     * @return 空响应
     */
    @PostMapping("/removeUser")
    @Operation(summary = "移除机构用户", description = "批量将用户从指定机构移除")
    public R<Void> removeUser(@RequestBody OrgConfigRemoveUserDTO dto) {
        organizationService.removeConfigUser(dto);
        return R.ok(null);
    }

    /**
     * 分页查询未分配机构的用户（优化接口，替代前端全量加载后过滤）。
     *
     * @param dto 查询参数
     * @return 未分配用户分页结果
     */
    @PostMapping("/unassignedUsers")
    @Operation(summary = "查询未分配机构用户", description = "分页查询尚未分配到指定机构的用户，支持用户名和昵称过滤")
    public R<PageResponse<OrgConfigUserVO>> unassignedUsers(@RequestBody OrgConfigUnassignedQueryDTO dto) {
        return R.ok(organizationService.unassignedUsers(dto));
    }
}
