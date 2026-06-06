package com.zhou6.cloud.sys.dto;

/**
 * 字典数据保存参数。
 */
public class DictDataDTO {

    private String id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private Integer isDefault;
    private Integer isStatus;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getDictLabel() { return dictLabel; }
    public void setDictLabel(String dictLabel) { this.dictLabel = dictLabel; }
    public String getDictValue() { return dictValue; }
    public void setDictValue(String dictValue) { this.dictValue = dictValue; }
    public Integer getDictSort() { return dictSort; }
    public void setDictSort(Integer dictSort) { this.dictSort = dictSort; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
