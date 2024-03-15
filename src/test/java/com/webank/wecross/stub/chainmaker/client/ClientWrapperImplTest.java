package com.webank.wecross.stub.chainmaker.client;

import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class ClientWrapperImplTest {

    @Test
    public void clientWrapperImplTest() {
        try {
            ChainMakerStubConfigParser chainMakerStubConfigParser =
                    new ChainMakerStubConfigParser("./", "stub.toml");
            ChainMakerStubConfig chainMakerStubConfig =
                    chainMakerStubConfigParser.loadConfig();
            ClientWrapperImpl clientWrapperInstance =
                    ClientWrapperFactory.createClientWrapperInstance(chainMakerStubConfig);
        } catch (Exception e) {
            fail();
        }
    }
}
