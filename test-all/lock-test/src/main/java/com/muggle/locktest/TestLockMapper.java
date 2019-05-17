package com.muggle.locktest;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TestLockMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestLock record);

    int insertSelective(TestLock record);

    TestLock selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TestLock record);

    int updateByPrimaryKey(TestLock record);

    TestLock test1(Long id);
    void test2();
    void test3();

    TestLock test4(@Param("test") String test);
}