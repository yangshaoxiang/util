package com.ysx.util.annotation;



import java.lang.annotation.*;

@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)
/**
 *  bean字段注解，用于希望字段对应的对象 全部的属性值转为map对应的键值 转化规则同一般对象相同
 *   该注解 在Object,String类型，基本类型，及基本类型包装类型,数组，集合上时 字段上标注会忽略该注解
 *   ps :枚举类型会忽略 内部的枚举类型 及$VALUES 属性
 */
public @interface CatchAllProperty {
    Class stopClass() default Void.class;
}