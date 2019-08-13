package com.ysx.util.handler;

/**
 *  用户 自定义忽略属性的扩展接口
 */
public interface MapIgnoreHandler {
    /**
     *  是否忽略本次 key-value 添加到map中
     * @param key 本次添加的key
     * @param value 本次添加的value
     * @return  true 表示忽略添加到map
     */
    boolean ignoreHandler(String key, Object value);
}
