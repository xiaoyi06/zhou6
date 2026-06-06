package com.zhou6.cloud.sys.dto;

/**
 * 字典类型保存参数。
 */
public class DictTypeDTO {

    private String id;
    private String dictName;
    private String dictType;
    private Integer isStatus;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
