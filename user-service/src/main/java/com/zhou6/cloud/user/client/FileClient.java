package com.zhou6.cloud.user.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.client.fallback.FileClientFallback;
import com.zhou6.cloud.common.constant.ApiPathConstants;
import com.zhou6.cloud.user.dto.FileIdRequest;
import com.zhou6.cloud.user.vo.FileDetailVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文件服务客户端，用于用户模块校验头像文件是否真实存在。
 */
@FeignClient(name = "file-service", fallback = FileClientFallback.class)
public interface FileClient {

    String FILE_SERVICE_CONTEXT = "/file";
    String FILE_MANAGEMENT_API = FILE_SERVICE_CONTEXT + ApiPathConstants.API_V1 + "/file-api";

    /**
     * 判断文件元数据对应的对象存储文件是否存在，保存头像前必须校验。
     *
     * @param request 文件 ID 参数
     * @return true 表示文件存在
     */
    @PostMapping(FILE_MANAGEMENT_API + "/exists")
    R<Boolean> exists(@RequestBody FileIdRequest request);

    /**
     * 查询文件元数据详情，用于返回头像访问地址。
     *
     * @param request 文件 ID 参数
     * @return 文件详情
     */
    @PostMapping(FILE_MANAGEMENT_API + "/detail")
    R<FileDetailVO> detail(@RequestBody FileIdRequest request);
}
