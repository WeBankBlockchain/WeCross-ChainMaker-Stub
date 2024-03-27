package com.webank.wecross.stub.chainmaker.client;

import java.util.Map;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

public interface ClientWrapper {

  ChainClient getNativeClient();

  ResultOuterClass.TxResponse invokeContract(
      String contractCallName, String method, Map<String, byte[]> params)
      throws ChainClientException, ChainMakerCryptoSuiteException;

  ResultOuterClass.TxResponse sendContractRequest(
      String contractCallName, String method, Map<String, byte[]> params, User user)
      throws ChainMakerCryptoSuiteException, ChainClientException;

  ResultOuterClass.TxResponse sendTxRequest(Request.TxRequest signedRequest) throws Exception;

  ResultOuterClass.TxResponse queryContract(
      String contractCallName, String method, Map<String, byte[]> params)
      throws ChainClientException, ChainMakerCryptoSuiteException;

  ChainmakerBlock.BlockInfo getBlockByHeight(long blockHeight)
      throws ChainClientException, ChainMakerCryptoSuiteException;

  ChainmakerBlock.BlockHeader getBlockHeaderByHeight(long blockHeight)
      throws ChainClientException, ChainMakerCryptoSuiteException;

  long getCurrentBlockHeight() throws ChainClientException, ChainMakerCryptoSuiteException;

  ChainmakerTransaction.TransactionInfo getTxByTxId(String txId)
      throws ChainClientException, ChainMakerCryptoSuiteException;

  byte[] getMerklePathByTxId(String txId)
      throws ChainClientException, ChainMakerCryptoSuiteException;
}
