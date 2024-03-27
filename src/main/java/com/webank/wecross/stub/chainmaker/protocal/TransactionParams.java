package com.webank.wecross.stub.chainmaker.protocal;

import com.webank.wecross.stub.TransactionRequest;
import java.util.Map;

public class TransactionParams {
  private TransactionRequest transactionRequest;
  private String contractCallName;
  private String contractMethodId;
  private Map<String, byte[]> contractMethodParams;
  private byte[] signData;

  public TransactionParams(
      TransactionRequest transactionRequest,
      String contractCallName,
      String contractMethodId,
      Map<String, byte[]> contractMethodParams) {
    this.transactionRequest = transactionRequest;
    this.contractCallName = contractCallName;
    this.contractMethodId = contractMethodId;
    this.contractMethodParams = contractMethodParams;
  }

  public TransactionParams(TransactionRequest transactionRequest, byte[] signData) {
    this.transactionRequest = transactionRequest;
    this.signData = signData;
  }

  public TransactionRequest getTransactionRequest() {
    return transactionRequest;
  }

  public void setTransactionRequest(TransactionRequest transactionRequest) {
    this.transactionRequest = transactionRequest;
  }

  public String getContractCallName() {
    return contractCallName;
  }

  public void setContractCallName(String contractCallName) {
    this.contractCallName = contractCallName;
  }

  public String getContractMethodId() {
    return contractMethodId;
  }

  public void setContractMethodId(String contractMethodId) {
    this.contractMethodId = contractMethodId;
  }

  public Map<String, byte[]> getContractMethodParams() {
    return contractMethodParams;
  }

  public void setContractMethodParams(Map<String, byte[]> contractMethodParams) {
    this.contractMethodParams = contractMethodParams;
  }

  public byte[] getSignData() {
    return signData;
  }

  @Override
  public String toString() {
    return "TransactionParams{"
        + "transactionRequest="
        + transactionRequest
        + ", contractName='"
        + contractCallName
        + '\''
        + ", contractMethod='"
        + contractMethodId
        + '\''
        + ", contractMethodParams="
        + contractMethodParams
        + '}';
  }
}
