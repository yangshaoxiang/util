package com.ysx.util.demo;

import com.ysx.util.annotation.CatchAllProperty;
import com.ysx.util.annotation.MapKeyMapping;

public class Student extends Person{
    @MapKeyMapping("stu_no")
    private Integer stuNo;

    @CatchAllProperty
    private School school;

    public Integer getStuNo() {
        return stuNo;
    }

    public void setStuNo(Integer stuNo) {
        this.stuNo = stuNo;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    @Override
    public String toString() {
        return "Student{" +
                "stuNo=" + stuNo +
                ", school=" + school +
                '}'+super.toString();
    }
}
