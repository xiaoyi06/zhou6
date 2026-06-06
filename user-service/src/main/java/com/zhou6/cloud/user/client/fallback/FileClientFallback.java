package com.zhou6.cloud.user.client.fallback;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.client.FileClient;
import com.zhou6.cloud.user.dto.FileIdRequest;
import com.zhou6.cloud.user.vo.FileDetailVO;
import org.springframework.stereotype.Component;

/**
 * file-service 不可用时的用户侧降级响应。
 */
@Component
public class FileClientFallback implements FileClient {

    @Override
    public R<Boolean> exists(FileIdRequest request) {
        return R.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), "文件服务暂不可用，请稍后再试");
    }

    @Override
    public R<FileDetailVO> detail(FileIdRequest request) {
        return R.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), "文件服务暂不可用，请稍后再试");
    }
}
