package com.webank.wecross.stub.chainmaker.integration;

import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.ChainMakerConnectionFactory;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import java.math.BigInteger;
import org.junit.Test;

public class ChainMakerConnectionTest {

  @Test
  public void handleGetBlockNumberTest() throws Exception {
    ChainMakerConnection connection = ChainMakerConnectionFactory.build("./", "stub-test.toml");
    Request request = new Request();
    request.setType(ChainMakerRequestType.GET_BLOCK_NUMBER);

    connection.asyncSend(
        request,
        response -> {
          BigInteger blockNumber = new BigInteger(response.getData());
          System.out.println("-----blockNumber:" + blockNumber);
          // Assert.assertNotNull(blockNumber);
        });
  }
}
