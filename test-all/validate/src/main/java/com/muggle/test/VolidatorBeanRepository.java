package com.muggle.test;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @program: test
 * @description:
 * @author: muggle
 * @create: 2019-01-03 16:28
 **/

public interface VolidatorBeanRepository extends CrudRepository<VolidatorBean, Long>, JpaSpecificationExecutor<VolidatorBean> {
}
