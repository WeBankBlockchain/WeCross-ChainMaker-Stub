package com.webank.wecross.stub.chainmaker;

import org.junit.Test;

public class ChainMakerFactoryTest {

    private final ChainMakerBaseFactory chainMakerBaseFactory = new ChainMakerBaseFactory();

    @Test
    public void newDriverTest() {
        chainMakerBaseFactory.newDriver();
    }
}
