package com.ysx.util.annotation;



import java.lang.annotation.*;

@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)
/**
 *  声明将日期类型和 字符串类型互转
 */
public @interface DateMapping {
    /**
     *  日期转字符串的模板
     */
    String value() default "yyyy-MM-dd HH:mm:ss";

}