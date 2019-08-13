package com.ysx.util;

import com.ysx.util.annotation.*;
import com.ysx.util.handler.MapIgnoreHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 对象转map 对象中请勿出现同名属性 因为map 键唯一 有同名属性请用 MapKeyMapping 做别名映射标记
 * ysx
 */
public class ObjectMappingMapUtil {

    /**
     * 缓存要转化为Map属性的类的字段信息
     */
    private static final Map<String, List<Field>> objFieldsCatch = new ConcurrentHashMap<>();
    /**
     * 字段缓存map key前缀 防止其他项目或jar使用相同字符串作为锁发生互斥
     */
    private static final String keyPrefix = "objectToMap-";

    private ObjectMappingMapUtil() {
    }



    /**
     * 对象属性转map
     *
     * @param sourceObject 要转化为map的对象
     * @param stopClass    要停止在对象父类的层级，不传或传递的非父类则默认Object 即只会转化继承结构中Object类以下的属性
     * @return 转化后的map
     */
    public static Map<String, Object> objectToMap(Object sourceObject, Class<?> stopClass) {
        HashMap<String, Object> resultMap = new HashMap<>();
        //根据对象内容 填充map属性
        populateMap(resultMap, sourceObject, stopClass,null);
        return resultMap;
    }


    /**
     * 对象属性转map
     *
     * @param sourceObject 要转化为map的对象
     * @param stopClass    要停止在对象父类的层级，不传或传递的非父类则默认Object 即只会转化继承结构中Object类以下的属性
     * @param mapIgnoreHandler 自定义属性忽略策略
     * @return 转化后的map
     */
    public static Map<String, Object> objectToMap(Object sourceObject, Class<?> stopClass, MapIgnoreHandler mapIgnoreHandler) {
        HashMap<String, Object> resultMap = new HashMap<>();
        //根据对象内容 填充map属性
        populateMap(resultMap, sourceObject, stopClass,mapIgnoreHandler);
        return resultMap;
    }

    /**
     * 根据对象 填充 map 的键值
     *
     * @param resultMap    要填充的map集合
     * @param sourceObject map填充的"数据源"
     * @param stopClass    限定 sourceObject 取的继承的属性层级
     */
    private static void populateMap(HashMap<String, Object> resultMap, Object sourceObject, Class<?> stopClass,MapIgnoreHandler mapIgnoreHandler) {
        if (sourceObject == null) {
            return;
        }
        Class<?> sourceObjectClass = sourceObject.getClass();
        // 结束类不是null 或者 结束类不是数据源类型的父类 或者结束类型是Void类型 设置默认结束类
        if (stopClass == null || !stopClass.isAssignableFrom(sourceObjectClass) || Void.class == stopClass) {
            stopClass = getDefaultStopClass(sourceObjectClass);
        }
        List<Field> fields = getAllDeclaredField(sourceObjectClass, stopClass);
        for (Field field : fields) {
            field.setAccessible(true);
            // 过滤不需要处理的属性字段
            if (filterField(sourceObjectClass, field)) {
                continue;
            }
            //是否需要捕捉对象全部属性填充到map中
            CatchAllProperty catchAllAnnotation = field.getAnnotation(CatchAllProperty.class);
            if (catchAllAnnotation != null && !isBaseType(field.getType())) {
                try {
                    // 递归调用 - 将字段对应的对象值填充到map集合中
                    populateMap(resultMap, field.get(sourceObject), catchAllAnnotation.stopClass(),mapIgnoreHandler);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                String standardKey = getMapKey(field);
                Object standardValue =  getMapValue(field, sourceObject);
                // 对用户自定义的忽略策略做处理
                if(mapIgnoreHandler==null||!mapIgnoreHandler.ignoreHandler(standardKey,standardValue)){
                    resultMap.put(standardKey,standardValue);
                }
            }
        }
    }

    /**
     * 获取默认的属性停止解析类
     *
     * @param sourceObjectClass 数据源类型
     * @return 传入类型默认的属性停止解析类
     */
    private static Class getDefaultStopClass(Class<?> sourceObjectClass) {
        if (sourceObjectClass.isEnum()) {
            return Enum.class;
        }
        return Object.class;
    }

    /**
     * 根据一些规则过滤不需加入到map中的字段
     *
     * @param sourceObjectClass 字段来源类
     * @param field             是否需要过滤的字段
     * @return true 表示需要过滤该字段
     */
    private static boolean filterField(Class<?> sourceObjectClass, Field field) {
        //添加了忽略注解，属性忽略转为map
        IgnoreMapMapping ignoreAnnotation = field.getAnnotation(IgnoreMapMapping.class);
        if (ignoreAnnotation != null && ignoreAnnotation.value()) {
            return true;
        }
        //如果字段来源于枚举类，字段本身对应类型也是枚举或者字段名包含$VALUES(该字段是编辑器编译枚举类自动生成) 过滤掉
        return sourceObjectClass.isEnum() && (field.getType().isEnum() || field.getName().contains("$VALUES"));
    }

    /**
     * 获取map映射的属性对应的值
     *
     * @param field        对象字段
     * @param sourceObject 保存值的对象
     * @return 获取map映射的属性对应的值
     */
    private static Object getMapValue(Field field, Object sourceObject) {
        Object mapValue = null;
        try {
            mapValue = field.get(sourceObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 检查 该字段映射的值 是否是字段本身对应的值
        CatchSingleProperty annotation = field.getAnnotation(CatchSingleProperty.class);
        if (mapValue != null && annotation != null && !"".equals(annotation.value()) && !isBaseType(mapValue.getClass())) {
            // 根据字段名称获取，和所属类的类型获取 Field 对象
            Field valueField = getClassFieldByName(mapValue.getClass(), annotation.value(), null);
            // 递归调用 - 该字段可能仍然不是希望获取的值(该字段可能仍然标记有 CatchProperty注解)
            if (valueField != null) {
                valueField.setAccessible(true);
                mapValue = getMapValue(valueField, mapValue);
            }
        }
        // 检查属性是否是日期类型,是否需要做日期格式化
        DateMapping dateMappingAnnotation = field.getAnnotation(DateMapping.class);
        if(dateMappingAnnotation!=null&&mapValue instanceof Date){
            mapValue = getDateString((Date) mapValue,dateMappingAnnotation.value());
        }
        return mapValue;
    }

    /**
     * 将日期转化为字符串表达
     * @param date 要格式化的日期
     * @param formate 格式化模板
     * @return 日期转化后的字符串表达
     */
    private static String getDateString(Date date,String formate){
        try {
            return  new SimpleDateFormat(formate).format(date);
        }catch (Exception e){
            // dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 判断类型是否是不可拆分的几种常见基本类型(不常见的未包含其中)
     * 包含: 基本类型，基本类型包装类型,容器类型(集合，数组)，String,Date类型，
     *
     * @param clazz 要判断的类型
     * @return 不是以上几种基本类型
     */
    private static boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive() || isBaseReferenceType(clazz) || isContainerType(clazz) || String.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz);
    }


    /**
     * 判断class是否基本引用类型
     *
     * @param clazz 要判断的class对象
     * @return 是否基本引用类型
     */
    private static boolean isBaseReferenceType(Class clazz) {
        return Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz);
    }

    /**
     * 判断class是否容器类型 集合 数组
     *
     * @param clazz 要判断的class对象
     * @return 判断class是否容器类型
     */
    private static boolean isContainerType(Class clazz) {
        return Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz) || clazz.isArray();
    }


    /**
     * 根据字段名到指定类中获取Field 字段对象
     *
     * @param aClass    指定类
     * @param fieldName 类中的字段名
     * @param stopClass 规定类层级查找上限
     * @return 指定类中指定字段的Field对象
     */
    private static Field getClassFieldByName(Class<?> aClass, String fieldName, Class<?> stopClass) {
        // 结束类不是null 或者 结束类不是数据源类型的父类 或者结束类型是Void类型 设置默认结束类
        if (stopClass == null || !stopClass.isAssignableFrom(aClass) || Void.class == stopClass) {
            stopClass = getDefaultStopClass(aClass);
        }
        List<Field> fields = getAllDeclaredField(aClass, stopClass);
        for (Field field : fields) {
            if (Objects.equals(fieldName, field.getName())) {
                return field;
            }
        }
        return null;
    }


    /**
     * 根据属性字段 获取map中的key
     *
     * @param field 转化为map的字段
     * @return 该属性要转化为map的key值
     */
    private static String getMapKey(Field field) {
        // 查看 该字段是否映射到其他字段
        Field mapKeyMappingField = getMapKeyMappingField(field);

        // 检查 是否设置了别名
        MapKeyMapping annotation = mapKeyMappingField.getAnnotation(MapKeyMapping.class);
        //获取map key 默认为属性名
        String key = mapKeyMappingField.getName();
        if (annotation != null && !"".equals(annotation.value())) {
            key = annotation.value();
        }
        return key;
    }

    /**
     * 检查给定字段是否映射到其他字段 -- 即字段上 CatchSingleProperty 注解 映射的其他字段
     *
     * @param field 给定的字段
     * @return 给定字段映射到的其他字段 不存在 返回原始字段
     */
    private static Field getMapKeyMappingField(Field field) {
        // 检查 该字段映射的值 是否是字段本身对应的值
        CatchSingleProperty annotation = field.getAnnotation(CatchSingleProperty.class);
        if (annotation != null && !"".equals(annotation.value()) && !isBaseType(field.getType())) {
            // 根据字段名称获取，和所属类的类型获取 Field 对象
            Field valueField = getClassFieldByName(field.getType(), annotation.value(), null);
            // 递归调用 - 该字段可能仍然不是希望获取的值(该字段可能仍然标记有 CatchProperty注解)
            if (valueField != null) {
                valueField.setAccessible(true);
                return getMapKeyMappingField(valueField);
            }
        }
        return field;
    }

    /**
     * 递归获取指定类的所有成员变量，允许设置停止递归的父类
     *
     * @param clazz          要获取所有字段的类
     * @param stopSuperClass 类的停止父类，即获取到该父类后，不在向上获取,默认Object
     * @return 指定类的所有成员变量
     */
    private static List<Field> getAllDeclaredField(Class clazz, Class stopSuperClass) {
        Class tmpClazz;
        if (clazz != null) {
            tmpClazz = clazz;
        } else {
            return new ArrayList<>();
        }
        //到缓存中查找对应的字段信息
        String fieldCatchKey = getFieldCatchKey(clazz, stopSuperClass);
        List<Field> fieldList = objFieldsCatch.get(fieldCatchKey);
        if (fieldList != null) {
            return fieldList;
        }
        // 加锁 保证同一个要获取字段的类和同一个停止类情况下 只执行一次获取该类所有字段操作 对于不同类可并行执行
        synchronized (getFieldCatchKey(clazz, stopSuperClass)) {
            //双重检查
            fieldList = objFieldsCatch.get(fieldCatchKey);
            if (fieldList != null) {
                return fieldList;
            }
            fieldList = new ArrayList<>();
            while (tmpClazz != null && tmpClazz != stopSuperClass) {
                fieldList.addAll(Arrays.asList(tmpClazz.getDeclaredFields()));
                //得到父类,然后赋给自己
                tmpClazz = tmpClazz.getSuperclass();
            }
            objFieldsCatch.put(fieldCatchKey, fieldList);
        }
        return fieldList;
    }

    /**
     * 获取缓存的字段的字符串key
     *
     * @param objClass  要获取所有字段对象的类型
     * @param stopClass 类的停止父类，即获取到该父类后，不在向上获取
     * @return 返回存入到字符串常量池中key的引用
     */
    private static String getFieldCatchKey(Class objClass, Class stopClass) {
        return (keyPrefix + objClass.getName() + "-" + stopClass.getName()).intern();
    }

    //-----------------------------------------------反向解析-----------------------------------

    /**
     * 将map 转为对象
     * --对于map转为枚举对象 自动推断转化
     *
     * @param sourceMap   map类型的数据源
     * @param targetClazz 要转化为的对象类型
     * @param <T>         要转化为的对象泛型
     * @return 将map 转化后的对象
     */
    public static <T> T mapToObject(Map<String, Object> sourceMap, Class<T> targetClazz, Class<?> stopClass) {
        if (targetClazz == null || sourceMap == null) {
            return null;
        }
        // 枚举类型特殊处理
        if (Enum.class.isAssignableFrom(targetClazz)) {
            return mapToEnum(sourceMap, targetClazz);
        }

        // 结束类不是null 或者 结束类不是数据源类型的父类 或者结束类型是Void类型 设置默认结束类
        if (stopClass == null || !stopClass.isAssignableFrom(targetClazz) || Void.class == stopClass) {
            stopClass = getDefaultStopClass(targetClazz);
        }

        // 普通类型直接反射创建对象
        T t = null;
        List<Field> allDeclaredField = getAllDeclaredField(targetClazz, stopClass);
        try {
            t = targetClazz.newInstance();
            for (Field field : allDeclaredField) {
                field.setAccessible(true);
                // 过滤不需要处理的属性字段
                if (filterField(targetClazz, field)) {
                    continue;
                }
                //加了 属性抓取注解的 需构造新的对象
                CatchSingleProperty catchSingleAnnotation = field.getAnnotation(CatchSingleProperty.class);
                CatchAllProperty catchAllAnnotation = field.getAnnotation(CatchAllProperty.class);
                if ((catchAllAnnotation != null || catchSingleAnnotation != null) && !isBaseType(field.getType())) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.isEnum()) {
                        Enum matchEnumByProperty = matchEnumByProperty(sourceMap, field);
                        field.set(t, matchEnumByProperty);
                        continue;
                    }
                    Class stopClass1 = null;
                    if (catchAllAnnotation != null) {
                        stopClass1 = catchAllAnnotation.stopClass();
                    }
                    Object catchAllValue = mapToObject(sourceMap, fieldType, stopClass1);
                    field.set(t, catchAllValue);
                    continue;
                }

                // 普通属性字段 直接从map中取值 赋值
                String mapKey = getMapKey(field);
                Object mapValue = sourceMap.get(mapKey);
                // 转换获取到的value 值类型 (可能会转换)
                mapValue = valueTypeChange(field,mapValue);
                //类型一致 赋值
                if(mapValue!=null&&field.getType() == mapValue.getClass()){
                    field.set(t, mapValue);
                }
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     *  可能存在的类型转换 -- 当前仅存在 字符串和日期转化
     * @param field 类字段
     * @param mapValue 从map中获取的值
     * @return 需要类型转化后的值，否则原值返回
     */
    private static Object valueTypeChange(Field field, Object mapValue) {
        //当前只有日期-字符串类型存在转化，其他类型暂时不需转化
        if(mapValue!=null && Date.class.isAssignableFrom(field.getType())){
            DateMapping dateMapping = field.getAnnotation(DateMapping.class);
            return  stringToDate(String.valueOf(mapValue),dateMapping.value());
        }
        return mapValue;
    }

    /**
     *  string转date
     * @param dateString 日期字符串表示
     * @param formate 日期格式化模板
     * @return 字符串转化后的日期
     */
    private static Date stringToDate(String dateString,String formate){
        try {
            return new SimpleDateFormat(formate).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * map 直接转枚举类型 - 属性全部匹配才可转
     *
     * @param sourceMap 数据源map
     * @param enumClass 枚举类型
     * @param <T>       泛型
     * @return map转化后的类型
     */
    private static <T> T mapToEnum(Map<String, Object> sourceMap, Class<T> enumClass) {
        try {
            //获取该枚举所有的枚举对象
            Method method = enumClass.getMethod("values");
            Enum[] values = (Enum[]) method.invoke(null);
            for (Enum value : values) {
                // 推断应该返回哪个枚举对象 - 两种类型 一种枚举除一般属性外所有额外属性均要匹配map 即CatchAllProperty注解模式
                if (fieldValueAllMatch(value, sourceMap)) {
                    return (T) value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据属性值 推断映射的是哪一个枚举对象 - 枚举字段必定包含CatchSingleProperty注解或者CatchAllProperty注解
     *
     * @param matchMap  可能保存有枚举内部值的 map
     * @param enumField 要转换的对象的枚举成员变量字段对象
     * @return 推断到的枚举类型
     */
    private static Enum matchEnumByProperty(Map<String, Object> matchMap, Field enumField) {
        if (matchMap == null || matchMap.isEmpty() || enumField == null) {
            return null;
        }
        Class<?> enumClass = enumField.getType();
        if (!Enum.class.isAssignableFrom(enumClass)) {
            return null;
        }
        CatchSingleProperty catchSingleAnnotation = enumField.getAnnotation(CatchSingleProperty.class);
        CatchAllProperty catchAllAnnotation = enumField.getAnnotation(CatchAllProperty.class);
        //枚举字段必定包含CatchSingleProperty注解或者CatchAllProperty注解
        if (catchSingleAnnotation == null && catchAllAnnotation == null) {
            return null;
        }
        try {
            //获取该枚举所有的枚举对象
            Method method = enumClass.getMethod("values");
            Enum[] values = (Enum[]) method.invoke(null);
            for (Enum value : values) {
                // 推断应该返回哪个枚举对象 - 两种类型 一种枚举除一般属性外所有额外属性均要匹配map 即CatchAllProperty注解模式
                if (catchSingleAnnotation == null && fieldValueAllMatch(value, matchMap)) {
                    return value;
                }
                //  另一种单个属性匹配即可 即CatchSingleProperty注解模式
                if (catchSingleAnnotation != null) {
                    Field classField = getClassFieldByName(enumClass, catchSingleAnnotation.value(), null);
                    if (classField != null && fieldValueSingleMatch(value, matchMap, classField)) {
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 单个字段是否匹配给定枚举
     *
     * @param value      要校验的枚举对象
     * @param matchMap   map数据源
     * @param judgeField 要匹配的字段
     * @return 匹配结果
     */
    private static boolean fieldValueSingleMatch(Enum value, Map<String, Object> matchMap, Field judgeField) {
        judgeField.setAccessible(true);
        String mapKey = getMapKey(judgeField);
        if (mapKey != null) {
            Object mapValue = matchMap.get(mapKey);
            // 获取当前枚举对象 对应属性值
            Object fieldValue = null;
            try {
                fieldValue = judgeField.get(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return Objects.deepEquals(mapValue, fieldValue);
        }
        return false;
    }

    /**
     *  所有字段是否匹配给定枚举
     * @param value  要校验的枚举对象
     * @param matchMap map数据源
     * @return 所有字段是否匹配给定枚举
     */
    private static boolean fieldValueAllMatch(Enum value, Map<String, Object> matchMap) {
        Class enumClass = value.getClass();
        // 获取枚举所有字段
        List<Field> allDeclaredField = getAllDeclaredField(enumClass,Enum.class);
        for (Field field : allDeclaredField) {
            field.setAccessible(true);
            // 可以被过滤的注解，不做属性匹配处理
            if (filterField(value.getClass(), field)) {
                continue;
            }
            // 判断字段是否匹配
            if (!fieldValueSingleMatch(value, matchMap, field)) {
                return false;
            }
        }
        return true;
    }

}

