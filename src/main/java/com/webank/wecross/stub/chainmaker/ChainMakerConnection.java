package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.chainmaker.client.AbstractClientWrapper;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.protocal.TransactionParams;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ResultOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;

public class ChainMakerConnection implements Connection {
  private static final Logger logger = LoggerFactory.getLogger(ChainMakerConnection.class);
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

  private List<ResourceInfo> resourceInfoList = new ArrayList<>();
  private List<ResourceInfo> resourcesCache = new ArrayList<>();
  private ConnectionEventHandler eventHandler;
  private final Map<String, String> properties = new HashMap<>();
  private final AbstractClientWrapper clientWrapper;

  public ChainMakerConnection(
      AbstractClientWrapper clientWrapper, ScheduledExecutorService scheduledExecutorService) {
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    this.clientWrapper = clientWrapper;
    scheduledExecutorService.scheduleAtFixedRate(
        () -> {
          if (Objects.nonNull(eventHandler)) {
            noteOnResourcesChange();
          }
        },
        10000,
        30000,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void asyncSend(Request request, Callback callback) {
    // request type
    int type = request.getType();
    if (type == ChainMakerRequestType.CALL) {
      // constantCall constantCallWithXa
      handleAsyncCallRequest(request, callback);
    } else if (type == ChainMakerRequestType.SEND_TRANSACTION) {
      // sendTransaction sendTransactionWithXa
      handleAsyncTransactionRequest(request, callback);
    } else if (type == ChainMakerRequestType.GET_BLOCK_NUMBER) {
      handleAsyncGetBlockNumberRequest(callback);
    } else if (type == ChainMakerRequestType.GET_BLOCK_BY_NUMBER) {
      handleAsyncGetBlockRequest(request, callback);
    } else if (type == ChainMakerRequestType.GET_TRANSACTION_PROOF) {
      handleAsyncGetTransactionProof(request, callback);
    } else if (type == ChainMakerRequestType.GET_TRANSACTION) {
      handleAsyncGetTransaction(request, callback);
    } else {
      logger.warn(" unrecognized request type, type: {}", request.getType());
      Response response = new Response();
      response.setErrorCode(ChainMakerStatusCode.UnrecognizedRequestType);
      response.setErrorMessage(
          ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.UnrecognizedRequestType)
              + " ,type: "
              + request.getType());
      callback.onResponse(response);
    }
  }

  private void handleAsyncGetTransaction(Request request, Callback callback) {
    Response response = new Response();
    try {
      String txId = new String(request.getData(), StandardCharsets.UTF_8);
      ChainmakerTransaction.TransactionInfo transactionInfo = clientWrapper.getTxByTxId(txId);
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncGetTransaction: {}", JsonFormat.printer().print(transactionInfo));
      }
      response.setErrorCode(ChainMakerStatusCode.Success);
      response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
      response.setData(objectMapper.writeValueAsBytes(transactionInfo));
    } catch (Exception e) {
      logger.warn("handleAsyncGetTransaction Exception, e: ", e);
      response.setErrorCode(ChainMakerStatusCode.UnclassifiedError);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncGetTransactionProof(Request request, Callback callback) {
    // TODO no proof ？

  }

  private void handleAsyncGetBlockRequest(Request request, Callback callback) {
    Response response = new Response();
    try {
      BigInteger blockHeight = new BigInteger(request.getData());
      ChainmakerBlock.BlockInfo blockInfo = clientWrapper.getBlockByHeight(blockHeight.longValue());
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncGetBlockRequest: {}", JsonFormat.printer().print(blockInfo));
      }
      response.setErrorCode(ChainMakerStatusCode.Success);
      response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
      response.setData(objectMapper.writeValueAsBytes(blockInfo));
    } catch (Exception e) {
      logger.warn("handleAsyncGetBlockRequest Exception, e: ", e);
      response.setErrorCode(ChainMakerStatusCode.UnclassifiedError);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncGetBlockNumberRequest(Callback callback) {
    Response response = new Response();
    try {
      BigInteger currentBlockHeight = BigInteger.valueOf(clientWrapper.getCurrentBlockHeight());
      if (logger.isDebugEnabled()) {
        logger.debug("currentBlockHeight: {}", currentBlockHeight);
      }
      response.setErrorCode(ChainMakerStatusCode.Success);
      response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
      response.setData(currentBlockHeight.toByteArray());
    } catch (Exception e) {
      logger.warn("handleGetBlockNumberRequest Exception, e: ", e);
      response.setErrorCode(ChainMakerStatusCode.HandleGetBlockNumberFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncCallRequest(Request request, Callback callback) {
    Response response = new Response();
    try {
      TransactionParams cmRequest =
          objectMapper.readValue(request.getData(), TransactionParams.class);
      String proxyContractName = cmRequest.getContractName();
      String proxyContractMethod = cmRequest.getContractMethod();
      Map<String, byte[]> proxyContractMethodParams = cmRequest.getContractMethodParams();
      ResultOuterClass.TxResponse txResponse =
          clientWrapper.queryContract(
              proxyContractName, proxyContractMethod, proxyContractMethodParams);
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncCallRequest: {}", JsonFormat.printer().print(txResponse));
      }
      response.setErrorCode(ChainMakerStatusCode.Success);
      response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
      response.setData(objectMapper.writeValueAsBytes(txResponse));
    } catch (Exception e) {
      logger.warn("handleAsyncCallRequest Exception:", e);
      response.setErrorCode(ChainMakerStatusCode.HandleCallRequestFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncTransactionRequest(Request request, Callback callback) {
    Response response = new Response();
    try {
      TransactionParams cmRequest =
          objectMapper.readValue(request.getData(), TransactionParams.class);
      String proxyContractName = cmRequest.getContractName();
      String proxyContractMethod = cmRequest.getContractMethod();
      Map<String, byte[]> proxyContractMethodParams = cmRequest.getContractMethodParams();

      ResultOuterClass.TxResponse txResponse =
          clientWrapper.invokeContract(
              proxyContractName, proxyContractMethod, proxyContractMethodParams);
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncTransactionRequest: {}", JsonFormat.printer().print(txResponse));
      }
      response.setErrorCode(ChainMakerStatusCode.Success);
      response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
      response.setData(objectMapper.writeValueAsBytes(txResponse));
    } catch (Exception e) {
      logger.warn("handleAsyncTransactionRequest Exception:", e);
      response.setErrorCode(ChainMakerStatusCode.HandleCallRequestFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void noteOnResourcesChange() {
    synchronized (this) {
      List<ResourceInfo> resources = getResources();
      if (!resources.equals(resourcesCache) && !resources.isEmpty()) {
        eventHandler.onResourcesChange(resources);
        resourcesCache = resources;
        if (logger.isDebugEnabled()) {
          logger.debug("resources notify, resources: {}", resources);
        }
      }
    }
  }

  public List<ResourceInfo> getResources() {
    List<ResourceInfo> resources =
        new ArrayList<ResourceInfo>() {
          {
            addAll(resourceInfoList);
          }
        };

    // from proxy contract
    String[] paths = listPaths();
    if (Objects.nonNull(paths)) {
      for (String path : paths) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setStubType(getProperty(ChainMakerConstant.CHAIN_MAKER_PROPERTY_STUB_TYPE));
        String[] pathSplit = path.split("\\.");
        if (pathSplit.length != 3) {
          continue;
        }
        resourceInfo.setName(pathSplit[2]);
        Map<Object, Object> resourceProperties = new HashMap<>();
        resourceProperties.put(
            ChainMakerConstant.CHAIN_MAKER_PROPERTY_CHAIN_ID,
            getProperty(ChainMakerConstant.CHAIN_MAKER_PROPERTY_CHAIN_ID));
        resourceProperties.put(
            ChainMakerConstant.CHAIN_MAKER_PROPERTY_AUTH_TYPE,
            getProperty(ChainMakerConstant.CHAIN_MAKER_PROPERTY_AUTH_TYPE));
        resourceInfo.setProperties(resourceProperties);
        resources.add(resourceInfo);
      }
    }
    return resources;
  }

  public String[] listPaths() {
    String proxyContactName = getProperty(ChainMakerConstant.CHAIN_MAKER_PROXY_NAME);
    Function function =
        new Function(
            ChainMakerConstant.PROXY_METHOD_GET_PATHS,
            Collections.emptyList(),
            Arrays.asList(new TypeReference<Array<Utf8String>>() {}));
    Map<String, byte[]> params = new HashMap<>();
    String methodDataStr = FunctionEncoder.encode(function);
    String method = methodDataStr.substring(0, 10);
    params.put(ChainMakerConstant.CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM, methodDataStr.getBytes());
    try {
      ResultOuterClass.TxResponse responseInfo =
          clientWrapper.queryContract(proxyContactName, method, params);
      if (Objects.equals(
          responseInfo.getCode().getNumber(), ResultOuterClass.TxStatusCode.SUCCESS.getNumber())) {
        logger.warn("listPaths failed, status {}", responseInfo.getCode().getNumber());
        return null;
      }
      // TODO 解析返回结果
      ByteString result = responseInfo.getContractResult().getResult();
      String[] paths = new String[] {};
      Set<String> set = new LinkedHashSet<>();
      set.add("a.b." + ChainMakerConstant.CHAIN_MAKER_PROXY_NAME);
      set.add("a.b." + ChainMakerConstant.CHAIN_MAKER_HUB_NAME);
      if (Objects.nonNull(paths) && paths.length != 0) {
        for (int i = paths.length - 1; i >= 0; i--) {
          set.add(paths[i]);
        }
      } else {
        logger.debug("No path found and add system resources");
      }
      return set.toArray(new String[0]);
    } catch (Exception e) {
      logger.warn("listPaths failed,", e);
      return null;
    }
  }

  public boolean hasProxyDeployed() {
    return getProperties().containsKey(ChainMakerConstant.CHAIN_MAKER_PROXY_NAME);
  }

  public boolean hasHubDeployed() {
    return getProperties().containsKey(ChainMakerConstant.CHAIN_MAKER_HUB_NAME);
  }

  public List<ResourceInfo> getResourceInfoList() {
    return resourceInfoList;
  }

  public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
    this.resourceInfoList = resourceInfoList;
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  public void addProperty(String key, String value) {
    this.properties.put(key, value);
  }

  public String getProperty(String key) {
    return this.properties.get(key);
  }
}
