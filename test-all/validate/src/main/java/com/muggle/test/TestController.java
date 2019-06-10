package com.muggle.test;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.anno.CreateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @program: test
 * @description:
 * @author: muggle
 * @create: 2019-01-03 15:42
 **/
@RestController
public class TestController {
    @CreateCache(expire = 10000)
    private Cache<Long, VolidatorBean> cache;
    @Autowired
    Validator validator;

    @GetMapping("/")
    public String test(){
        IcGeneralBody test = new IcGeneralBody().setBarcode("test");
        VolidatorBean volidatorBean = new VolidatorBean();
        cache.PUT(11L,volidatorBean);
        CacheGetResult<VolidatorBean> get = cache.GET(11L);
        VolidatorBean value = get.getValue();
        Set<ConstraintViolation<IcGeneralBody>> validate = validator.validate(test);
        validate.forEach(csss->{

            System.out.println(csss.getMessage());
        });
        System.out.println(",,,,");
        return "ss";
    }
}
