/*
 * Copyright © 2020 Ocado (marian.jureczko@ocado.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocadotechnology.gembus;

import com.ocadotechnology.gembus.test.CustomArranger;

import static com.ocadotechnology.gembus.test.Arranger.someInteger;

public class ToTestNonPublic {
    public static final String ARRANGER_TEXT = "Arranger was used";
    public String someText;
    public Integer someNumber;

    record NonPublicRecord(String name) {
    }
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
