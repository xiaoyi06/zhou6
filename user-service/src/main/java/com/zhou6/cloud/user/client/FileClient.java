package com.zhou6.cloud.user.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.FileIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文件服务客户端，用于用户模块校验头像文件是否真实存在。
 */
@FeignClient(name = "file-service")
public interface FileClient {

    /**
     * 判断文件元数据对应的对象存储文件是否存在，保存头像前必须校验。
     *
     * @param request 文件 ID 参数
     * @return true 表示文件存在
     */
    @PostMapping("/file/fileManagement/exists")
    R<Boolean> exists(@RequestBody FileIdRequest request);
}
