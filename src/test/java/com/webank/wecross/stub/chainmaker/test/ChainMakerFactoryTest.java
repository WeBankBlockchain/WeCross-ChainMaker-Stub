package com.webank.wecross.stub.chainmaker.test;

import com.webank.wecross.stub.chainmaker.ChainMakerBaseFactory;
import org.junit.Test;

public class ChainMakerFactoryTest {

    private final ChainMakerBaseFactory chainMakerBaseFactory = new ChainMakerBaseFactory();

    @Test
    public void newDriverTest() {
        chainMakerBaseFactory.newDriver();
    }
}
