package com.zhou6.cloud.file.controller;

import java.nio.charset.StandardCharsets;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.file.constant.FileApiPathConstants;
import com.zhou6.cloud.file.vo.DownloadFile;
import com.zhou6.cloud.file.dto.FileIdDTO;
import com.zhou6.cloud.file.vo.FileUploadVO;
import com.zhou6.cloud.file.service.FileObjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理接口，提供上传、详情、删除、存在性判断和下载能力。
 */
@Tag(name = "文件管理", description = "提供文件上传、详情查询、删除、存在性判断和下载能力")
@RestController
@RequestMapping(FileApiPathConstants.FILE_MANAGEMENT)
public class FileController {

    private final FileObjectService fileObjectService;

    public FileController(FileObjectService fileObjectService) {
        this.fileObjectService = fileObjectService;
    }

    /**
     * 上传文件到当前激活的对象存储平台，并保存文件元数据。
     *
     * @param file multipart 表单中的文件字段，字段名固定为 file
     * @return 文件元数据和访问地址
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传文件到当前激活的对象存储平台，并保存文件元数据")
    public R<FileUploadVO> upload(@RequestPart("file") MultipartFile file) {
        return R.ok(fileObjectService.upload(file));
    }

    /**
     * 根据文件 ID 查询文件元数据。
     *
     * @param dto 文件 ID 参数
     * @return 文件元数据
     */
    @PostMapping("/detail")
    @Operation(summary = "查询文件详情", description = "根据文件ID查询文件元数据和访问地址")
    public R<FileUploadVO> detail(@RequestBody FileIdDTO dto) {
        return R.ok(fileObjectService.detail(dto == null ? null : dto.getId()));
    }

    /**
     * 删除对象存储中的文件，并同步删除 sys_file 元数据。
     *
     * @param dto 文件 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    @Operation(summary = "删除文件", description = "删除对象存储中的文件，并同步删除 sys_file 元数据")
    public R<Void> delete(@RequestBody FileIdDTO dto) {
        fileObjectService.delete(dto == null ? null : dto.getId());
        return R.ok(null);
    }

    /**
     * 判断文件元数据对应的对象存储文件是否存在。
     *
     * @param dto 文件 ID 参数
     * @return true 表示对象存在，false 表示元数据不存在或对象不存在
     */
    @PostMapping("/exists")
    @Operation(summary = "判断文件是否存在", description = "判断文件元数据对应的对象存储文件是否存在")
    public R<Boolean> exists(@RequestBody FileIdDTO dto) {
        return R.ok(fileObjectService.exists(dto == null ? null : dto.getId()));
    }

    /**
     * 下载文件内容。下载接口返回二进制响应，不再包裹统一 R 响应体。
     *
     * @param dto 文件 ID 参数
     * @return 文件流
     */
    @PostMapping("/download")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件内容，返回二进制响应")
    public ResponseEntity<InputStreamResource> download(@RequestBody FileIdDTO dto) {
        DownloadFile file = fileObjectService.download(dto == null ? null : dto.getId());
        BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        file.getContentType() == null || file.getContentType().isBlank()
                                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                                : file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString());
        if (file.getFileSize() != null) {
            builder.contentLength(file.getFileSize());
        }
        return builder.body(new InputStreamResource(file.getInputStream()));
    }
}
