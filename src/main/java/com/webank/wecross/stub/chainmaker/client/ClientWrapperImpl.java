package com.webank.wecross.stub.chainmaker.client;

import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import org.chainmaker.sdk.ChainClient;

public class ClientWrapperImpl extends AbstractClientWrapper {

  public ClientWrapperImpl(ChainClient client, ChainMakerStubConfig.Chain.RpcClient rpcClient) {
    super(client, rpcClient);
  }
}
