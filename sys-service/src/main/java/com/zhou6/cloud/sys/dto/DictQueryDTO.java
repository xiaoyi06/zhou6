package com.zhou6.cloud.sys.dto;

/**
 * 字典查询参数。
 */
public class DictQueryDTO extends PageQueryDTO {

    private String dictName;
    private String dictType;
    private Integer isStatus;

    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
}
