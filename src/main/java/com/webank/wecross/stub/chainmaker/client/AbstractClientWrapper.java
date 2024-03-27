package com.webank.wecross.stub.chainmaker.client;

import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.RpcServiceClient;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.execption.ExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientWrapper implements ClientWrapper {

  private final ChainClient client;

  private final long rpcCallTimeout;
  private final long syncResultTimeout;
  private final Logger logger = LoggerFactory.getLogger(AbstractClientWrapper.class);

  public AbstractClientWrapper(ChainClient client, ChainMakerStubConfig.Chain.RpcClient rpcClient) {
    this.client = client;
    this.rpcCallTimeout = rpcClient.getCallTimeout();
    this.syncResultTimeout = rpcClient.getSyncResultTimeout();
  }

  @Override
  public ChainClient getNativeClient() {
    return client;
  }

  @Override
  public ResultOuterClass.TxResponse queryContract(
          String contractCallName, String method, Map<String, byte[]> params)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.queryContract(contractCallName, method, null, params, rpcCallTimeout);
  }

  @Override
  public ResultOuterClass.TxResponse invokeContract(
          String contractCallName, String method, Map<String, byte[]> params)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.invokeContract(
            contractCallName, method, null, params, rpcCallTimeout, syncResultTimeout);
  }

  @Override
  public ResultOuterClass.TxResponse sendContractRequest(
          String contractCallName, String method, Map<String, byte[]> params, User user)
      throws ChainMakerCryptoSuiteException, ChainClientException {
    Request.Payload payload = client.invokeContractPayload(contractCallName, method, "", params);
    return client.sendContractRequest(payload, null, rpcCallTimeout, syncResultTimeout, user);
  }

  @Override
  public ResultOuterClass.TxResponse sendTxRequest(Request.TxRequest signedRequest)
      throws Exception {
    ResultOuterClass.TxResponse txResponse;
    RpcServiceClient rpcServiceClient = null;
    try {
      rpcServiceClient = client.getConnectionPool().borrowObject();
      if (rpcServiceClient == null) {
        logger.error("all connections no Idle or Ready");
        throw new ChainClientException(
            "all connections no Idle or Ready, please reSet connection count",
            ExceptionType.NOTNORMALCONNECT);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      txResponse =
          rpcServiceClient
              .getRpcNodeFutureStub()
              .sendRequest(signedRequest)
              .get(rpcCallTimeout, TimeUnit.MILLISECONDS);
      client.getConnectionPool().returnObject(rpcServiceClient);
    } catch (TimeoutException e) {
      client.getConnectionPool().invalidateObject(rpcServiceClient);
      throw new ChainClientException(
          "connect timeout error : " + e.getMessage(), ExceptionType.TIMEOUT);
    } catch (InterruptedException e) {
      client.getConnectionPool().invalidateObject(rpcServiceClient);
      throw new ChainClientException(
          "connect interrupted, error : " + e.getMessage(), ExceptionType.INTERRUPTED);
    } catch (ExecutionException e) {
      client.getConnectionPool().invalidateObject(rpcServiceClient);
      throw new ChainClientException(
          "connect execution error : " + e.getMessage(), ExceptionType.EXECUTION);
    }

    return txResponse;
  }

  @Override
  public ChainmakerBlock.BlockInfo getBlockByHeight(long blockHeight)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.getBlockByHeight(blockHeight, true, rpcCallTimeout);
  }

  @Override
  public ChainmakerBlock.BlockHeader getBlockHeaderByHeight(long blockHeight)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.getBlockHeaderByHeight(blockHeight, rpcCallTimeout);
  }

  @Override
  public long getCurrentBlockHeight() throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.getCurrentBlockHeight(rpcCallTimeout);
  }

  @Override
  public ChainmakerTransaction.TransactionInfo getTxByTxId(String txId)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.getTxByTxId(txId, rpcCallTimeout);
  }

  @Override
  public byte[] getMerklePathByTxId(String txId)
      throws ChainClientException, ChainMakerCryptoSuiteException {
    return client.getMerklePathByTxId(txId, rpcCallTimeout);
  }
}
