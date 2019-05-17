package com.muggle.locktest;

public class TestLock {
    private Long id;

    private String test1;

    private String test2;

    private String test3;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1 == null ? null : test1.trim();
    }

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2 == null ? null : test2.trim();
    }

    public String getTest3() {
        return test3;
    }

    public void setTest3(String test3) {
        this.test3 = test3 == null ? null : test3.trim();
    }
}