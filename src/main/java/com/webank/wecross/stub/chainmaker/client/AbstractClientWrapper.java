package com.webank.wecross.stub.chainmaker.client;

import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import java.util.Map;

public abstract class AbstractClientWrapper implements ClientWrapper {

    private final ChainClient client;

    private final long rpcCallTimeout;
    private final long syncResultTimeout;

    public AbstractClientWrapper(ChainClient client, ChainMakerStubConfig.Chain.RpcClient rpcClient) {
        this.client = client;
        this.rpcCallTimeout = rpcClient.getCallTimeout();
        this.syncResultTimeout = rpcClient.getSyncResultTimeout();
    }

    @Override
    public ResultOuterClass.TxResponse queryContract(String contractName, String method, Map<String, byte[]> params) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.queryContract(contractName, method, null, params, rpcCallTimeout);
    }

    @Override
    public ResultOuterClass.TxResponse invokeContract(String contractName, String method, Map<String, byte[]> params) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.invokeContract(contractName, method, null, params, rpcCallTimeout, syncResultTimeout);
    }

    @Override
    public ChainmakerBlock.BlockInfo getBlockByHeight(long blockHeight) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.getBlockByHeight(blockHeight, true, rpcCallTimeout);
    }

    @Override
    public ChainmakerBlock.BlockHeader getBlockHeaderByHeight(long blockHeight) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.getBlockHeaderByHeight(blockHeight, rpcCallTimeout);
    }

    @Override
    public long getCurrentBlockHeight() throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.getCurrentBlockHeight(rpcCallTimeout);
    }

    @Override
    public ChainmakerTransaction.TransactionInfo getTxByTxId(String txId) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.getTxByTxId(txId, rpcCallTimeout);
    }

    @Override
    public byte[] getMerklePathByTxId(String txId) throws ChainClientException, ChainMakerCryptoSuiteException {
        return client.getMerklePathByTxId(txId, rpcCallTimeout);
    }
}
