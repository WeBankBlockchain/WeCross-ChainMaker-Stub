package com.webank.wecross.stub.chainmaker.client;

import com.google.protobuf.ByteString;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.chainmaker.pb.accesscontrol.MemberOuterClass;
import org.chainmaker.pb.common.Request;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.ChainClientConfig;
import org.chainmaker.sdk.config.CryptoConfig;
import org.chainmaker.sdk.config.NodeConfig;
import org.chainmaker.sdk.config.RpcClientConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ClientUtility {

  private static final Logger logger = LoggerFactory.getLogger(ClientUtility.class);

  private static final ChainManager chainManager;

  static {
    chainManager = ChainManager.getInstance();
  }

  public static ChainClient initClient(ChainMakerStubConfig stubConfig) throws Exception {
    SdkConfig sdkConfig = new SdkConfig();
    ChainClientConfig chainClientConfig = buildChainClientConfig(stubConfig.getChain());
    sdkConfig.setChainClient(chainClientConfig);
    return chainManager.createChainClient(sdkConfig);
  }

  public static Request.Payload createPayload(
      String chainId, String contractName, String method, Map<String, byte[]> params) {

    String txId = Utils.generateTxId();

    Request.Payload.Builder payloadBuilder =
        Request.Payload.newBuilder()
            .setChainId(chainId)
            .setTxType(Request.TxType.INVOKE_CONTRACT)
            .setTxId(txId)
            .setTimestamp(Utils.getCurrentTimeSeconds())
            .setContractName(contractName)
            .setMethod(method)
            .setSequence(0);

    if (params != null && !params.isEmpty()) {
      params.forEach(
          (key, value) ->
              payloadBuilder.addParameters(
                  Request.KeyValuePair.newBuilder()
                      .setKey(key)
                      .setValue(ByteString.copyFrom(value))
                      .build()));
    }

    return payloadBuilder.build();
  }

  public static Request.TxRequest createTxRequest(
      Request.Payload payload, String orgId, User user, String hashAlgorithm)
      throws ChainMakerCryptoSuiteException {
    Request.EndorsementEntry.Builder endorsementEntryBuilder;

    MemberOuterClass.Member sender =
        MemberOuterClass.Member.newBuilder()
            .setOrgId(orgId)
            .setMemberInfo(ByteString.copyFrom(user.getPukBytes()))
            .setMemberType(MemberOuterClass.MemberType.PUBLIC_KEY)
            .build();
    byte[] signature =
        user.getCryptoSuite().rsaSign(user.getPrivateKey(), payload.toByteArray(), hashAlgorithm);
    endorsementEntryBuilder =
        Request.EndorsementEntry.newBuilder()
            .setSigner(sender)
            .setSignature(ByteString.copyFrom(signature));

    Request.TxRequest.Builder txRequestBuilder =
        Request.TxRequest.newBuilder().setPayload(payload).setSender(endorsementEntryBuilder);
    return txRequestBuilder.build();
  }

  private static ChainClientConfig buildChainClientConfig(ChainMakerStubConfig.Chain chain)
      throws WeCrossException, IOException {
    String chainId = chain.getChainId();
    String signKeyPath = chain.getSignKeyPath();
    String authType = chain.getAuthType();
    String hash = chain.getCrypto().getHash();
    List<ChainMakerStubConfig.Chain.Node> nodes = chain.getNodes();
    int maxReceiveMessageSize = chain.getRpcClient().getMaxReceiveMessageSize();

    CryptoConfig cryptoConfig = new CryptoConfig();
    cryptoConfig.setHash(hash);

    NodeConfig[] nodeConfigs =
        nodes.stream()
            .map(
                s -> {
                  NodeConfig nodeConfig = new NodeConfig();
                  nodeConfig.setNodeAddr(s.getNodeAddr());
                  return nodeConfig;
                })
            .toArray(NodeConfig[]::new);

    RpcClientConfig rpcClientConfig = new RpcClientConfig();
    rpcClientConfig.setMaxReceiveMessageSize(maxReceiveMessageSize);

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource signKeyResource = resolver.getResource(signKeyPath);
    if (!signKeyResource.exists() || !signKeyResource.isFile()) {
      throw new WeCrossException(
          WeCrossException.ErrorCode.DIR_NOT_EXISTS,
          signKeyPath + " does not exist, please check.");
    }

    ChainClientConfig chainClientConfig = new ChainClientConfig();
    chainClientConfig.setChainId(chainId);
    chainClientConfig.setUserSignKeyFilePath(signKeyResource.getFile().getPath());
    chainClientConfig.setAuthType(authType);
    chainClientConfig.setCrypto(cryptoConfig);
    chainClientConfig.setNodes(nodeConfigs);
    chainClientConfig.setRpcClient(rpcClientConfig);

    return chainClientConfig;
  }
}
