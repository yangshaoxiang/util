package com.ysx.util.demo;

import com.ysx.util.annotation.IgnoreMapMapping;
import com.ysx.util.annotation.MapKeyMapping;

public class Address {
    private String sheng;
    @IgnoreMapMapping
    private String shi;

    @MapKeyMapping("address-xian")
    private String xian;

    public String getSheng() {
        return sheng;
    }

    public void setSheng(String sheng) {
        this.sheng = sheng;
    }

    public String getShi() {
        return shi;
    }

    public void setShi(String shi) {
        this.shi = shi;
    }

    public String getXian() {
        return xian;
    }

    public void setXian(String xian) {
        this.xian = xian;
    }

    @Override
    public String toString() {
        return "Address{" +
                "sheng='" + sheng + '\'' +
                ", shi='" + shi + '\'' +
                ", xian='" + xian + '\'' +
                '}';
    }
}
