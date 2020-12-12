package com.ocadotechnology.gembus.test;

import org.jeasy.random.EasyRandomParameters;

class MyEasyRandomParameters extends EasyRandomParameters {

    private int objectPoolSize;

    @Override
    public int getObjectPoolSize() {
        return this.objectPoolSize;
    }

    @Override
    public void setObjectPoolSize(int objectPoolSize) {
        this.objectPoolSize = objectPoolSize;
    }
}