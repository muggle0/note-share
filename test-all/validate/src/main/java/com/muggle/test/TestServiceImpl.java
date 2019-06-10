package com.muggle.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @program: test
 * @description:
 * @author: muggle
 * @create: 2019-01-03 16:18
 **/
@Service
public class TestServiceImpl implements Myservice{
    @Autowired
    VolidatorBeanRepository repository;
    @Override
    public VolidatorBean getVolidatorBeanById(long id) {
        Optional<VolidatorBean> byId = repository.findById(id);
        return byId.get();
    }

    @Override
    public void updateVolidatorBean(VolidatorBean volidatorBean) {

    }

    @Override
    public void deleteVolidatorBean(long id) {

    }

    @Override
    public void insertVolidatorBean(VolidatorBean volidatorBean) {
        
    }
}
