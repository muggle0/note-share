package com.muggle.test;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import org.springframework.stereotype.Service;

//@Service
public interface Myservice {
//    @Cached(name="userCache-", key="#id", expire = 3600)
    VolidatorBean getVolidatorBeanById(long id);

//    @CacheUpdate(name="userCache-", key="#volidatorBean.id", value="#user")
    void updateVolidatorBean(VolidatorBean volidatorBean);

//    @CacheInvalidate(name="userCache-", key="#userId")
    void deleteVolidatorBean(long id);

//    @CacheUpdate(name="userCache-", key="#volidatorBean.id", value="#user")
    void insertVolidatorBean(VolidatorBean volidatorBean);
}
