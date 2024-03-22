package com.webank.wecross.stub.chainmaker.protocal;

import com.webank.wecross.stub.TransactionRequest;

import java.util.Map;

public class TransactionParams {
    private TransactionRequest transactionRequest;
    private String proxyContractName;
    private String proxyContractMethod;
    private Map<String, byte[]> proxyContractMethodParams;

    public TransactionParams() {}

    public TransactionParams(
            TransactionRequest transactionRequest,
            String proxyContractName,
            String proxyContractMethod,
            Map<String, byte[]> proxyContractMethodParams) {
        this.transactionRequest = transactionRequest;
        this.proxyContractName = proxyContractName;
        this.proxyContractMethod = proxyContractMethod;
        this.proxyContractMethodParams = proxyContractMethodParams;
    }

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public void setTransactionRequest(TransactionRequest transactionRequest) {
        this.transactionRequest = transactionRequest;
    }

    public String getProxyContractName() {
        return proxyContractName;
    }

    public void setProxyContractName(String proxyContractName) {
        this.proxyContractName = proxyContractName;
    }

    public String getProxyContractMethod() {
        return proxyContractMethod;
    }

    public void setProxyContractMethod(String proxyContractMethod) {
        this.proxyContractMethod = proxyContractMethod;
    }

    public Map<String, byte[]> getProxyContractMethodParams() {
        return proxyContractMethodParams;
    }

    public void setProxyContractMethodParams(Map<String, byte[]> proxyContractMethodParams) {
        this.proxyContractMethodParams = proxyContractMethodParams;
    }

    @Override
    public String toString() {
        return "TransactionParams{"
                + "transactionRequest="
                + transactionRequest
                + ", proxyContractName='"
                + proxyContractName
                + '\''
                + ", proxyContractMethod='"
                + proxyContractMethod
                + '\''
                + ", proxyContractMethodParams="
                + proxyContractMethodParams
                + '}';
    }
}
