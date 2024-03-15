package com.webank.wecross.stub.chainmaker.client;

import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import org.chainmaker.sdk.ChainClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientWrapperFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientWrapperFactory.class);

    public static ClientWrapperImpl createClientWrapperInstance(ChainMakerStubConfig chainMakerStubConfig) throws Exception {
        logger.info("ChainMakerStubConfig: {}", chainMakerStubConfig);
        ChainClient client = ClientUtility.initClient(chainMakerStubConfig);
        return new ClientWrapperImpl(client);
    }

}
