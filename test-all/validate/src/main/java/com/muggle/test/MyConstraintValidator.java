package com.muggle.test;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.anno.CreateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @program: demo
 * @description:
 * @author: muggle
 * @create: 2018-12-29 16:00
 **/
@Component
public class MyConstraintValidator implements ConstraintValidator<MyTest, String> {
//    @CreateCache(expire = 100)
//    private Cache<Long, VolidatorBean> cache;
    @Autowired
    TestRepository repository;
    @Autowired
    VolidatorBean myservice;
//    private CaseMode caseMode;
    @Override
    public void initialize(MyTest constraintAnnotation) {
//        this.caseMode = constraintAnnotation.value();
    }
    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {


        VolidatorBean beanById = myservice;
        IcGeneralBody byBarcode=null;
        String defaultConstraintMessageTemplate = constraintContext.getDefaultConstraintMessageTemplate();
        boolean equals =defaultConstraintMessageTemplate.equals(beanById.getField())&&beanById.getType()==1;
        if(equals){
            byBarcode = repository.findByBarcode(object);
        }
        System.out.println("sss");
        return true;
    }
}
