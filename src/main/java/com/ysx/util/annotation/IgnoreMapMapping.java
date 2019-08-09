package com.ysx.util.annotation;



import java.lang.annotation.*;

@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)
/**
 *  bean字段注解，忽略该字段的转化
 */
public @interface IgnoreMapMapping {
    /**
     * 忽略该属性的转化
     */
    boolean value() default true;
}