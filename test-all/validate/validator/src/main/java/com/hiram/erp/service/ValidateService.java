package com.hiram.erp.service;

import com.hiram.erp.dao.ValidatorRuleMapper;
import com.hiram.erp.entity.ValidatorParameter;
import com.hiram.erp.entity.ValidatorRule;
import com.hiram.erp.enums.ExceptionResultEnum;
import com.hiram.erp.excetion.HomeException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @program: hiram_erp
 * @description:
 * @author: dzk
 * @create: 2019-01-07 10:56
 **/
@Component
public class ValidateService {
    @Autowired
    private ValidatorRuleMapper validatorRuleMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private ValidatorParameter getValidatorParameter(String name, String group) {
        ValidatorParameter validatorParameter = new ValidatorParameter();
        validatorParameter.setBeanName(name);
        validatorParameter.setGroup(group);
        return validatorParameter;
    }

    public Map<String, List<ValidatorRule>> getRuleMap(Object object, String group) {
        String name = object.getClass().getName();
        ValidatorParameter validatorParameter = getValidatorParameter(name, group);
        List<ValidatorRule> validatorRules = validatorRuleMapper.getRule(validatorParameter);
        Map<String, List<ValidatorRule>> map = new HashMap<>();
        if (validatorRules != null && !validatorRules.isEmpty()) {
            // 按字段划分，将校验规则存入到map，因为字段可以有多个校验规则
            validatorRules.stream().forEach(var -> {
                List<ValidatorRule> rules = map.get(var.getFieldName());
                if (rules == null) {
                    rules = new ArrayList<>();
                }
                rules.add(var);
                map.put(var.getFieldName(), rules);
            });
        }
        return map;
    }

    public String doValidator(Object object, String group) {
        Map<String, List<ValidatorRule>> ruleMap = getRuleMap(object, group);
        StringBuffer message = new StringBuffer();
        if (!ruleMap.isEmpty()) {
            // 获取entity 所有字段
            Field[] fields = object.getClass().getDeclaredFields();
            if (fields != null && fields.length > 0) {
                // 循环遍历字段
                Arrays.stream(fields).forEach(var -> {
                    // 判断字段有没有校验规则
                    if (ruleMap.containsKey(var.getName())) {
                        // 设置为可访问
                        var.setAccessible(true);
                        // 获取对应字段的校验规则
                        List<ValidatorRule> lists = ruleMap.get(var.getName());
                        // 给集合内ValidatorRule.fieldValue 赋值
                        lists.stream().forEach(obj -> {
                            try {
                                // field 赋值
                                obj.setFieldValue(var.get(object));
                                obj.setObject(object);
                                //System.out.println(obj.toString());
                                // 执行校验方法
                                Method method = this.getClass().getMethod(obj.getValidatorName(), ValidatorRule.class);
                                Object result = method.invoke(this, obj);
                                // 获取校验细腻
                                if (result != null) {
                                    message.append(result);
                                }
                            } catch (Exception e) {
                                throw new HomeException(ExceptionResultEnum.RULE_DATA_ERROR.getCode(), ExceptionResultEnum.RULE_DATA_ERROR.getMsg());
                            }
                        });
                    }
                });
            }
        }
        return message.toString();
    }

    public String notNull(ValidatorRule rule) {
        StringBuffer stringBuffer = new StringBuffer();
        if (rule.getFieldValue() != null) {
            return null;
        }
        return stringBuffer.append(rule.getFieldName()).append("为空").toString();
    }

    public String max(ValidatorRule rule) {
        StringBuffer stringBuffer = new StringBuffer();
        if (rule.getFieldValue() != null && (Double) rule.getFieldValue() < 100) {
            return null;
        }
        return stringBuffer.append(rule.getFieldName()).append("最大值不超过100").toString();
    }

    public String min(ValidatorRule rule) {
        StringBuffer stringBuffer = new StringBuffer();
        if (rule.getFieldValue() != null && (Double) rule.getFieldValue() > 0) {
            return null;
        }
        return stringBuffer.append(rule.getFieldName()).append("最小值小于0").toString();
    }

    public String uniqueValidator(ValidatorRule rule) {
        StringBuffer stringBuffer = new StringBuffer();
        SqlSession session = null;
        try {
            session = sqlSessionFactory.openSession();
            List<Object> list = session.selectList(rule.getMapperName(), rule.getFieldValue());
            // 最后commit
            session.commit();
            if (!list.isEmpty()) {
                // 私有
                Field field = rule.getObject().getClass().getDeclaredField("rownum");
                field.setAccessible(true);
                Object object = field.get(rule.getObject());
                stringBuffer.append(object).append(rule.getFieldName()).append("不唯一");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.rollback();
        } finally {
            session.close();
        }
        return stringBuffer.toString();
    }

    public String doValidatorList(List<Object> list, String group) {
       if (list.size()>100){

       }
    }
    @Async
    public Future<String> doValidatorOne(){

    }

    @Async
    public Future<String> doValidatorTwo(){

    }
    @Async
    public Future<String> doValidatorThree(){

    }
}
