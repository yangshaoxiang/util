package com.ysx.util.demo;

import com.ysx.util.ObjectMappingMapUtil;
import com.ysx.util.demo.enums.Sex;
import com.ysx.util.handler.impl.ValueEmptyHandler;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Test {
    public static void main(String[] args) {
        System.out.println("---------测试Bean-->Map--------------");
        Student student = getStudent();
        System.out.println(student);
        Map<String, Object> map = ObjectMappingMapUtil.objectToMap(student, Object.class);
        mapPrint(map);
        System.out.println("-----bean->map 新加自定义属性过滤器");
        Map<String, Object> stringObjectMap = ObjectMappingMapUtil.objectToMap(student, Object.class, new ValueEmptyHandler());
        mapPrint(stringObjectMap);

        System.out.println("---------测试Map-->Bean--------------");
        map.put("shi","合肥市");
        Student student1 = ObjectMappingMapUtil.mapToObject(map, Student.class, null);
        System.out.println(student1);

        System.out.println("---------测试枚举转化--------------");
        Map<String, Object> enumMap = ObjectMappingMapUtil.objectToMap(Sex.MAN, null);
        mapPrint(enumMap);

        Sex sex = ObjectMappingMapUtil.mapToObject(map, Sex.class, null);
        System.out.println(sex);

    }

    private static Student getStudent(){
        Address address = new Address();
        address.setSheng("安徽省");
        address.setShi("合肥市");
        address.setXian("肥东县");

        School school = new School();
        school.setSchoolName("第二中学");
        school.setAddress(address);
        school.setCreateDate(new Date());

        Student student = new Student();
        student.setName("张三");
        student.setAge(10);
        student.setStuNo(1);
        student.setSex(Sex.MAN);
        student.setSchool(school);

        return student;
    }

    private static void mapPrint(Map<String, Object> map){
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey()+"---"+entry.getValue());
        }
       // System.out.println("===============================================");
    }
}
