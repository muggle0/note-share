package com.hiram.erp.entity;/**
 * Created by dzk on 2019/1/2.
 */

/**
 * @ClassName ValidatorRule
 * @Description
 * @Author dzk
 * @Date 2019/1/2
 **/
public class ValidatorRule {

    private Long id;

    private String fieldName;

    private Object fieldValue;

    private String beanName;

    private String validatorName;

    private String validatorDescription;

    private Integer gradeId;

    private Object object;

    private String mapperName;

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getValidatorName() {
        return validatorName;
    }

    public void setValidatorName(String validatorName) {
        this.validatorName = validatorName;
    }

    public String getValidatorDescription() {
        return validatorDescription;
    }

    public void setValidatorDescription(String validatorDescription) {
        this.validatorDescription = validatorDescription;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    @Override
    public String toString() {
        return "ValidatorRule{" +
                "id=" + id +
                ", fieldName='" + fieldName + '\'' +
                ", fieldValue=" + fieldValue +
                ", beanName='" + beanName + '\'' +
                ", validatorName='" + validatorName + '\'' +
                ", validatorDescription='" + validatorDescription + '\'' +
                ", gradeId=" + gradeId +
                '}';
    }
}
