package com.zhou6.cloud.file.service;

import com.zhou6.cloud.file.vo.DownloadFile;
import com.zhou6.cloud.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件对象业务接口，统一封装对象存储操作和文件元数据维护。
 */
public interface FileObjectService {

    /**
     * 上传文件并保存元数据。
     *
     * @param file 上传文件
     * @return 文件元数据
     */
    FileUploadVO upload(MultipartFile file);

    /**
     * 查询文件元数据。
     *
     * @param id 文件 ID
     * @return 文件元数据
     */
    FileUploadVO detail(String id);

    /**
     * 下载文件内容。
     *
     * @param id 文件 ID
     * @return 下载文件
     */
    DownloadFile download(String id);

    /**
     * 删除文件和元数据。
     *
     * @param id 文件 ID
     */
    void delete(String id);

    /**
     * 判断文件元数据对应的对象存储文件是否存在。
     *
     * @param id 文件 ID
     * @return true 表示对象存在，false 表示元数据不存在或对象不存在
     */
    boolean exists(String id);
}
