package com.ysx.util.handler.impl;

import com.ysx.util.handler.MapIgnoreHandler;

/**
 *  对于值为空的属性 忽略添加到map中 字符串多种空 均算空
 */
public class ValueEmptyHandler implements MapIgnoreHandler {
    /**
     * 是否忽略本次 key-value 添加到map中
     *
     * @param key   本次添加的key
     * @param value 本次添加的value
     * @return true 表示忽略添加到map
     */
    @Override
    public boolean ignoreHandler(String key, Object value) {
        return isValueEmpty(value);
    }


    /**
     * 判断对象是否为 null 对于字符串 null " " 等也视为空
     *
     * @param object 要判断的对象
     * @return 对象是不是空
     */
    private boolean isValueEmpty(Object object) {
        if (object == null) {
            return true;
        }
        return object instanceof String && ("".equals(((String) object).trim()) || "null".equalsIgnoreCase((String) object));
    }
}
