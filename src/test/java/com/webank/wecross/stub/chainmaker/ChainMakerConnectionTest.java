package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import org.junit.Test;

import java.math.BigInteger;

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
                    //Assert.assertNotNull(blockNumber);
                });
    }
}
