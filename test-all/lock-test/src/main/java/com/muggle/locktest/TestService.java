package com.muggle.locktest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: lock-test
 * @description: test
 * @author: muggle
 * @create: 2019-03-25
 **/
@Service
public class TestService {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    DataSourceTransactionManager transactionManager;

    @Autowired
    TestLockMapper testLockMapper;


    public /*synchronized*/ String  test(){
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
//        sqlSession.
        try {
            TestLockMapper mapper = sqlSession.getMapper(TestLockMapper.class);
            TestLock testLock = mapper.test1(1L);
            Long id = testLock.getId();
            TestLock testLock1 = new TestLock();
            testLock1.setTest1("ss");
            testLock1.setId(id+1);
            mapper.insert(testLock1);
            return "ss";
        }finally {
            sqlSession.commit(true);
            sqlSession.close();
        }
    }

    public String test1(){
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        TestLockMapper mapper = sqlSession.getMapper(TestLockMapper.class);
        mapper.test2();
        TestLock testLock = mapper.test1(1L);
        Long id = testLock.getId();
        TestLock testLock1 = new TestLock();
        testLock1.setTest1("ss");
        testLock1.setId(id+1);
        mapper.insert(testLock1);
        mapper.test3();
        return "ss";
    }
    @Transactional
    public String test2(){
        testLockMapper.test4(" SELECT *" +
                "FROM `test_lock` WHERE id=(SELECT max(id) FROM test_lock )" +
                "    for update ;");
        TestLock testLock = new TestLock();
        Long id = testLock.getId();
        TestLock testLock1 = new TestLock();
        testLock1.setTest1("ss");
        testLock1.setId(id+1);
        testLockMapper.insert(testLock1);
        return "ss";
    }
    public String test4

}
