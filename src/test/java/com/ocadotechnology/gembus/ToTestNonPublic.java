package com.ocadotechnology.gembus;

import com.ocadotechnology.gembus.test.CustomArranger;

import static com.ocadotechnology.gembus.test.Arranger.someInteger;

public class ToTestNonPublic {
    public static final String ARRANGER_TEXT = "Arranger was used";
    public String someText;
    public Integer someNumber;
}

class ToTestNonPublicArranger extends CustomArranger<ToTestNonPublic> {

    @Override
    protected ToTestNonPublic instance() {
        ToTestNonPublic toTestNonPublic = new ToTestNonPublic();
        toTestNonPublic.someText = ToTestNonPublic.ARRANGER_TEXT;
        toTestNonPublic.someNumber = someInteger();
        return toTestNonPublic;
    }
}
