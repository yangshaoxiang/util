package com.ysx.util.annotation;



import java.lang.annotation.*;

@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)
/**
 *  bean字段注解，用于希望map的值为该注解标注对象内部某个属性的值，而非整个对象 (注意 此时key生成规则基于属性值字段生成)
 *   该注解 在Object,String类型，基本类型，及基本类型包装类型,数组，集合上时 字段上标注会忽略该注解
 */
public @interface CatchSingleProperty {
    /**
     *  希望获取的属性字段名
     */
    String value() default "";


}