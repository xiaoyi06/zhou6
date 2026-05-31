package com.zhou6.cloud.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置，按 storage.type 选择当前激活的平台。
 */
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** 当前启用的存储平台：minio、obs、oss 或 cos。 */
    private String type = "minio";
    /** MinIO 存储配置。 */
    private Provider minio = new Provider();
    /** 华为云 OBS 存储配置。 */
    private Provider obs = new Provider();
    /** 阿里云 OSS 存储配置。 */
    private Provider oss = new Provider();
    /** 腾讯云 COS 存储配置。 */
    private Provider cos = new Provider();
    /**
     * 获取当前 storage.type 对应的平台配置。
     *
     * @return 当前激活平台配置
     */
    public Provider activeProvider() {
        return switch (type == null ? "" : type.toLowerCase()) {
            case "minio" -> minio;
            case "obs" -> obs;
            case "oss" -> oss;
            case "cos" -> cos;
            default -> throw new IllegalArgumentException("不支持的存储平台: " + type);
        };
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Provider getMinio() {
        return minio;
    }

    public void setMinio(Provider minio) {
        this.minio = minio;
    }

    public Provider getObs() {
        return obs;
    }

    public void setObs(Provider obs) {
        this.obs = obs;
    }

    public Provider getOss() {
        return oss;
    }

    public void setOss(Provider oss) {
        this.oss = oss;
    }

    public Provider getCos() {
        return cos;
    }

    public void setCos(Provider cos) {
        this.cos = cos;
    }

    public static class Provider {

        /** SDK 访问对象存储的 endpoint。 */
        private String endpoint;
        /** 对外返回的公开访问 endpoint，未配置时回退到 endpoint。 */
        private String publicEndpoint;
        /** 对象存储 region。MinIO 可使用默认值 us-east-1。 */
        private String region = "us-east-1";
        /** 访问密钥 ID。 */
        private String accessKey;
        /** 访问密钥 Secret。 */
        private String secretKey;
        /** 存储桶名称。 */
        private String bucketName;
        /** 是否使用路径风格访问，MinIO 通常需要开启。 */
        private boolean pathStyleAccess = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getPublicEndpoint() {
            return publicEndpoint;
        }

        public void setPublicEndpoint(String publicEndpoint) {
            this.publicEndpoint = publicEndpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }
}
