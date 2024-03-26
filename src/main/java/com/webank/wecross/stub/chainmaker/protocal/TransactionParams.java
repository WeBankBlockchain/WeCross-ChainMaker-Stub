package com.webank.wecross.stub.chainmaker.protocal;

import com.webank.wecross.stub.TransactionRequest;
import java.util.Map;

public class TransactionParams {
  private TransactionRequest transactionRequest;
  private String contractName;
  private String contractMethodId;
  private Map<String, byte[]> contractMethodParams;

  public TransactionParams(
      TransactionRequest transactionRequest,
      String contractName,
      String contractMethodId,
      Map<String, byte[]> contractMethodParams) {
    this.transactionRequest = transactionRequest;
    this.contractName = contractName;
    this.contractMethodId = contractMethodId;
    this.contractMethodParams = contractMethodParams;
  }

  public TransactionRequest getTransactionRequest() {
    return transactionRequest;
  }

  public void setTransactionRequest(TransactionRequest transactionRequest) {
    this.transactionRequest = transactionRequest;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
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

  @Override
  public String toString() {
    return "TransactionParams{"
        + "transactionRequest="
        + transactionRequest
        + ", contractName='"
        + contractName
        + '\''
        + ", contractMethod='"
        + contractMethodId
        + '\''
        + ", contractMethodParams="
        + contractMethodParams
        + '}';
  }
}
