package com.webank.wecross.stub.chainmaker.protocal;

import com.webank.wecross.stub.TransactionRequest;
import java.util.Map;

public class TransactionParams {
    private TransactionRequest transactionRequest;
    private String contractName;
    private String contractMethod;
    private Map<String, byte[]> contractMethodParams;

    public TransactionParams(
            TransactionRequest transactionRequest,
            String contractName,
            String contractMethod,
            Map<String, byte[]> contractMethodParams) {
        this.transactionRequest = transactionRequest;
        this.contractName = contractName;
        this.contractMethod = contractMethod;
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

    public String getContractMethod() {
        return contractMethod;
    }

    public void setContractMethod(String contractMethod) {
        this.contractMethod = contractMethod;
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
                + ", proxyContractName='"
                + contractName
                + '\''
                + ", proxyContractMethod='"
                + contractMethod
                + '\''
                + ", proxyContractMethodParams="
                + contractMethodParams
                + '}';
    }
}
