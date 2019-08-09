package com.ysx.util.demo;

import com.ysx.util.annotation.CatchSingleProperty;
import com.ysx.util.annotation.IgnoreMapMapping;

public class School {

    private String schoolName;

    @CatchSingleProperty("xian")
    private Address address;

    @IgnoreMapMapping
    private Integer schoolNum;

    public Integer getSchoolNum() {
        return schoolNum;
    }

    public void setSchoolNum(Integer schoolNum) {
        this.schoolNum = schoolNum;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "School{" +
                "schoolName='" + schoolName + '\'' +
                ", address=" + address +
                '}';
    }
}
