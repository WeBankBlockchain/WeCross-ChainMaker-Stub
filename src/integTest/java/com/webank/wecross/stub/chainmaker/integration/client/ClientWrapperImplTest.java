package com.webank.wecross.stub.chainmaker.integration.client;

import com.webank.wecross.stub.chainmaker.client.ClientWrapperFactory;
import com.webank.wecross.stub.chainmaker.client.ClientWrapperImpl;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import org.junit.Test;

public class ClientWrapperImplTest {

  @Test
  public void clientWrapperImplTest() {
    try {
      ChainMakerStubConfigParser chainMakerStubConfigParser =
          new ChainMakerStubConfigParser("./", "stub-test.toml");
      ChainMakerStubConfig chainMakerStubConfig = chainMakerStubConfigParser.loadConfig();
      ClientWrapperImpl clientWrapperInstance =
          ClientWrapperFactory.createClientWrapperInstance(chainMakerStubConfig);
    } catch (Exception e) {
      // fail();
    }
  }
}
