package com.zhou6.cloud.file.service.impl;

import java.io.IOException;
import java.io.InputStream;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.vo.DownloadFile;
import com.zhou6.cloud.file.vo.FileUploadVO;
import com.zhou6.cloud.file.entity.SysFile;
import com.zhou6.cloud.file.mapper.SysFileMapper;
import com.zhou6.cloud.file.service.FileObjectService;
import com.zhou6.cloud.file.storage.FileStorageService;
import com.zhou6.cloud.file.storage.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件对象业务实现，负责对象存储操作和 sys_file 元数据的一致性维护。
 */
@Service
public class FileObjectServiceImpl implements FileObjectService {

    private final FileStorageService storageService;
    private final SysFileMapper fileMapper;
    private final StorageProperties storageProperties;

    public FileObjectServiceImpl(FileStorageService storageService, SysFileMapper fileMapper,
            StorageProperties storageProperties) {
        this.storageService = storageService;
        this.fileMapper = fileMapper;
        this.storageProperties = storageProperties;
    }

    /**
     * 上传文件并记录元数据。对象存储成功后再写入数据库，数据库失败时触发事务回滚。
     *
     * @param file 上传文件
     * @return 文件元数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadVO upload(MultipartFile file) {
        require(file != null && !file.isEmpty(), "上传文件不能为空");
        String originalFilename = normalizeFileName(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            StoredFile storedFile = storageService.upload(inputStream, originalFilename, file.getContentType(),
                    file.getSize());
            SysFile sysFile = new SysFile();
            sysFile.setFileName(originalFilename);
            sysFile.setFileSize(file.getSize());
            sysFile.setContentType(file.getContentType());
            sysFile.setObjectKey(storedFile.getObjectKey());
            sysFile.setPlatform(storageService.getPlatform());
            sysFile.setBucketName(storageProperties.activeProvider().getBucketName());
            sysFile.setUrl(storedFile.getUrl());
            fileMapper.insert(sysFile);
            return toVo(sysFile);
        } catch (IOException ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "上传文件失败", ex);
        }
    }

    /**
     * 查询文件元数据详情。
     *
     * @param id 文件 ID
     * @return 文件元数据
     */
    @Override
    public FileUploadVO detail(String id) {
        return toVo(getRequiredFile(id));
    }

    /**
     * 根据文件 ID 下载对象存储中的文件内容。
     *
     * @param id 文件 ID
     * @return 下载文件内容和响应头所需信息
     */
    @Override
    public DownloadFile download(String id) {
        SysFile sysFile = getRequiredFile(id);
        require(sysFile.getObjectKey() != null && !sysFile.getObjectKey().isBlank(), "文件对象不存在");
        InputStream inputStream = storageService.download(sysFile.getObjectKey());
        return new DownloadFile(normalizeFileName(sysFile.getFileName()), sysFile.getFileSize(),
                sysFile.getContentType(), inputStream);
    }

    /**
     * 删除文件。先删除对象存储文件，再删除数据库元数据。
     *
     * @param id 文件 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        SysFile sysFile = getRequiredFile(id);
        storageService.delete(sysFile.getObjectKey());
        fileMapper.deleteById(sysFile.getId());
    }

    /**
     * 判断文件对象是否真实存在于当前对象存储平台。元数据不存在时直接返回 false。
     *
     * @param id 文件 ID
     * @return true 表示对象存在，false 表示元数据不存在或对象不存在
     */
    @Override
    public boolean exists(String id) {
        Long fileId = parseRequiredId(id, "文件ID不能为空");
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null || sysFile.getObjectKey() == null || sysFile.getObjectKey().isBlank()) {
            return false;
        }
        return storageService.exists(sysFile.getObjectKey());
    }

    /**
     * 查询必需存在的文件元数据，不存在时抛出业务异常。
     */
    private SysFile getRequiredFile(String id) {
        Long fileId = parseRequiredId(id, "文件ID不能为空");
        SysFile sysFile = fileMapper.selectById(fileId);
        require(sysFile != null, "文件不存在");
        return sysFile;
    }

    /**
     * 将数据库实体转换为接口响应对象，避免直接暴露持久化对象。
     */
    private FileUploadVO toVo(SysFile sysFile) {
        FileUploadVO vo = new FileUploadVO();
        vo.setId(String.valueOf(sysFile.getId()));
        vo.setFileName(sysFile.getFileName());
        vo.setFileSize(sysFile.getFileSize());
        vo.setContentType(sysFile.getContentType());
        vo.setObjectKey(sysFile.getObjectKey());
        vo.setPlatform(sysFile.getPlatform());
        vo.setUrl(sysFile.getUrl());
        return vo;
    }

    /**
     * 将前端传入的字符串 ID 转为 Long，统一处理空值和格式错误。
     */
    private Long parseRequiredId(String value, String message) {
        require(value != null && !value.isBlank(), message);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "文件ID不正确");
        }
    }

    /**
     * 参数断言工具方法，失败时抛出统一业务异常。
     */
    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unnamed";
        }
        String normalized = fileName.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        return normalized.isBlank() ? "unnamed" : normalized;
    }
}
