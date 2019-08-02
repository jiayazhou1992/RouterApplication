package com.kongapi.routerprocessor;

public class MyClass {
    private String packeage;

    public String getPackeage() {
        return packeage;
    }

    public void setPackeage(String packeage) {
        if (this.packeage != null) return;
        this.packeage = packeage;
    }
}
