package com.muggle.test;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: test
 * @description:
 * @author: muggle
 * @create: 2019-01-03 15:09
 **/
@Repository
public interface TestRepository extends CrudRepository<IcGeneralBody, String>, JpaSpecificationExecutor<IcGeneralBody> {
    IcGeneralBody findByBarcode(String barcode);
}
