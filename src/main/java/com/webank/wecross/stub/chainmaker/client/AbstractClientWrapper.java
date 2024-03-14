package com.webank.wecross.stub.chainmaker.client;


import org.chainmaker.sdk.ChainClient;

public abstract class AbstractClientWrapper implements ClientWrapper {

    private final ChainClient client;

    public AbstractClientWrapper(ChainClient client) {
        this.client = client;
    }

}
