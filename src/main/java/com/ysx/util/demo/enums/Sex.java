package com.ysx.util.demo.enums;

public enum Sex {
    MAN("男"),WOMEN("女");
    private String sexName;

    Sex(String sexName) {
        this.sexName = sexName;
    }

    public String getSexName() {
        return sexName;
    }

    public void setSexName(String sexName) {
        this.sexName = sexName;
    }
}
