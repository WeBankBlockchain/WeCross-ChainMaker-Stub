package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.chainmaker.account.ChainMakerPublicAccount;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStubException;
import com.webank.wecross.stub.chainmaker.protocal.TransactionParams;
import com.webank.wecross.stub.chainmaker.utils.FunctionUtility;
import com.webank.wecross.stub.chainmaker.utils.RevertMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.utils.Utils;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.abi.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.utils.Hex;
import org.fisco.bcos.sdk.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainMakerDriver implements Driver {

  private static final Logger logger = LoggerFactory.getLogger(ChainMakerDriver.class);

  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

  private final ABICodecJsonWrapper codecJsonWrapper;

  private final ABICodec abiCodec;

  private final FunctionEncoder functionEncoder;

  private final ABIDefinitionFactory abiDefinitionFactory;

  public ChainMakerDriver(CryptoSuite cryptoSuite) {
    this.codecJsonWrapper = new ABICodecJsonWrapper(true);
    this.abiCodec = new ABICodec(cryptoSuite, true);
    this.functionEncoder = new FunctionEncoder(cryptoSuite);
    this.abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
  }

  @Override
  public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
    return null;
  }

  @Override
  public List<ResourceInfo> getResources(Connection connection) {
    if (connection instanceof ChainMakerConnection) {
      return ((ChainMakerConnection) connection).getResources();
    }
    logger.error("Not ChainMaker connection, connection name: {}", connection.getClass().getName());
    return new ArrayList<>();
  }

  @Override
  public void asyncCall(
      TransactionContext context,
      TransactionRequest request,
      boolean byProxy,
      Connection connection,
      Callback callback) {
    if (byProxy) {
      asyncCallByProxy(context, request, connection, callback);
    } else {
      asyncCallNative(context, request, connection, callback);
    }
  }

  @Override
  public void asyncSendTransaction(
      TransactionContext context,
      TransactionRequest request,
      boolean byProxy,
      Connection connection,
      Callback callback) {
    if (byProxy) {
      asyncSendTransactionByProxy(context, request, connection, callback);
    } else {
      asyncSendTransactionNative(context, request, connection, callback);
    }
  }

  @Override
  public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
    Request request = Request.newRequest(ChainMakerRequestType.GET_BLOCK_NUMBER, "");
    connection.asyncSend(
        request,
        response -> {
          if (response.getErrorCode() != ChainMakerStatusCode.Success) {
            logger.warn(
                " errorCode: {},  errorMessage: {}",
                response.getErrorCode(),
                response.getErrorMessage());
            callback.onResponse(new Exception(response.getErrorMessage()), -1);
          } else {
            BigInteger blockNumber = new BigInteger(response.getData());
            logger.debug(" blockNumber: {}", blockNumber);
            callback.onResponse(null, blockNumber.longValue());
          }
        });
  }

  @Override
  public void asyncGetBlock(
      long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback) {
    // 构建Request
    byte[] blockNumberBytes = BigInteger.valueOf(blockNumber).toByteArray();
    Request request = Request.newRequest(ChainMakerRequestType.GET_BLOCK_NUMBER, blockNumberBytes);
    connection.asyncSend(
        request,
        response -> {
          // 判断请求是否成功
          if (response.getErrorCode() != ChainMakerStatusCode.Success) {
            logger.warn(
                " asyncGetBlock, errorCode: {},  errorMessage: {}",
                response.getErrorCode(),
                response.getErrorMessage());

            callback.onResponse(new Exception(response.getErrorMessage()), null);
          } else {
            try {
              ChainmakerBlock.BlockInfo blockInfo =
                  ObjectMapperFactory.getObjectMapper()
                      .readValue(response.getData(), ChainmakerBlock.BlockInfo.class);
              Block block = BlockUtility.convertToBlock(response.getData(), onlyHeader);
              block.setRawBytes(response.getData());
              if (block.getTransactionsHashes().isEmpty()) {
                List<ChainmakerTransaction.Transaction> txsList = blockInfo.getBlock().getTxsList();
                for (ChainmakerTransaction.Transaction chainMakerTx : txsList) {
                  parseChainMakerTransaction(chainMakerTx, block, connection);
                }
              }
              //TODO: block header validation

              callback.onResponse(null, block);
            } catch (Exception e) {
              logger.warn(" blockNumber: {}, e: ", blockNumber, e);
              callback.onResponse(e, null);
            }
          }
        });
  }

  private void parseChainMakerTransaction(
      ChainmakerTransaction.Transaction chainmakerTransaction, Block block, Connection connection)
      throws Exception {
    org.chainmaker.pb.common.Request.Payload payload = chainmakerTransaction.getPayload();
    String methodId;
    String input;
    String xaTransactionID = "0";
    String path;
    String resource;
    Transaction transaction = new Transaction();
    String txId = payload.getTxId();
    transaction.setTxBytes(chainmakerTransaction.toByteArray());
    String memberInfo =
        chainmakerTransaction.getSender().getSigner().getMemberInfo().toStringUtf8();
    String userAddr = CryptoUtils.getEVMAddressFromPKPEM(memberInfo, "SHA256", "EC");
    transaction.setAccountIdentity(userAddr);
    transaction.setTransactionByProxy(true);
    transaction.getTransactionResponse().setHash(txId);
    transaction.getTransactionResponse().setBlockNumber(block.getBlockHeader().getNumber());
    if (block.getBlockHeader() != null) {
      transaction.getTransactionResponse().setTimestamp(block.getBlockHeader().getTimestamp());
    }
    List<org.chainmaker.pb.common.Request.KeyValuePair> parametersList =
        payload.getParametersList();
    org.chainmaker.pb.common.Request.KeyValuePair keyValuePair = parametersList.get(0);
    String proxyInput = keyValuePair.getValue().toStringUtf8();
    if (proxyInput.startsWith(functionEncoder.buildMethodId(FunctionUtility.ProxySendTXMethod))) {
      Tuple3<String, String, byte[]> proxyResult =
          FunctionUtility.getSendTransactionProxyWithoutTxIdFunctionInput(proxyInput);
      resource = proxyResult.getValue2();
      input = Numeric.toHexString(proxyResult.getValue3());
      methodId = input.substring(0, FunctionUtility.MethodIDWithHexPrefixLength);
      if (logger.isDebugEnabled()) {
        logger.debug("  resource: {}, methodId: {}", resource, methodId);
      }
    } else if (proxyInput.startsWith(
        functionEncoder.buildMethodId(FunctionUtility.ProxySendTransactionTXMethod))) {
      Tuple6<String, String, BigInteger, String, String, byte[]> proxyInputResult =
          FunctionUtility.getSendTransactionProxyFunctionInput(proxyInput);
      xaTransactionID = proxyInputResult.getValue2();
      path = proxyInputResult.getValue4();
      resource = Path.decode(path).getResource();
      String methodSig = proxyInputResult.getValue5();
      methodId = functionEncoder.buildMethodId(methodSig);

      if (logger.isDebugEnabled()) {
        logger.debug(
            "xaTransactionID: {}, path: {}, methodSig: {}, methodId: {}",
            xaTransactionID,
            path,
            methodSig,
            methodId);
      }
    } else {
      // transaction not send by proxy
      transaction.setTransactionByProxy(false);
      block.getTransactionsWithDetail().add(transaction);
      return;
    }
    transaction
        .getTransactionRequest()
        .getOptions()
        .put(StubConstant.XA_TRANSACTION_ID, xaTransactionID);
    transaction.setResource(resource);
    // query ABI
    String finalMethodId = methodId;
    // TODO: 获取abi
    String abi = "";
    try {
      ABIDefinition function =
          abiDefinitionFactory.loadABI(abi).getMethodIDToFunctions().get(finalMethodId);
      if (Objects.isNull(function)) {
        logger.warn("Maybe abi is upgraded, Load function failed, methodId: {}", finalMethodId);
      } else {
        transaction.getTransactionRequest().setMethod(function.getName());
      }
    } catch (Exception e) {
      logger.error(
          "Maybe Query abi failed, transactionHash: {},resource:{} MethodId: {},e:",
          txId,
          resource,
          finalMethodId,
          e);
    }
    block.getTransactionsWithDetail().add(transaction);
  }

  @Override
  public void asyncGetTransaction(
      String transactionHash,
      long blockNumber,
      BlockManager blockManager,
      boolean isVerified,
      Connection connection,
      GetTransactionCallback callback) {}

  @Override
  public void asyncCustomCommand(
      String command,
      Path path,
      Object[] args,
      Account account,
      BlockManager blockManager,
      Connection connection,
      CustomCommandCallback callback) {}

  @Override
  public byte[] accountSign(Account account, byte[] message) {
    return new byte[0];
  }

  @Override
  public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
    return false;
  }

  void asyncCallByProxy(
      TransactionContext context,
      TransactionRequest request,
      Connection connection,
      Callback callback) {
    TransactionResponse transactionResponse = new TransactionResponse();
    Map<String, String> properties = connection.getProperties();

    // TODO: check properties
    try {
      String contractAddress = properties.get(ChainMakerConstant.CHAIN_MAKER_PROXY_NAME);
      Path path = context.getPath();
      String name = path.getResource();

      // TODO: get Abi by name
      String abi = ""; // resources abi

      if (abi == null) {
        throw new ChainMakerStubException(
            ChainMakerStatusCode.ABINotExist, "resource ABI not exist: " + name);
      }
      // encode
      String[] args = request.getArgs();
      String method = request.getMethod();

      ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
      ABIDefinition abiDefinition =
          contractABIDefinition.getFunctions().get(method).stream()
              .filter(function -> function.getInputs().size() == (args == null ? 0 : args.length))
              .findFirst()
              .orElseThrow(
                  () ->
                      new ChainMakerStubException(
                          ChainMakerStatusCode.MethodNotExist, "method not exist: " + method));

      byte[] encodedArgs =
          Hex.decode(
              abiCodec.encodeMethodFromString(
                  abi, method, args != null ? Arrays.asList(args) : new ArrayList<>()));
      String transactionID = (String) request.getOptions().get(StubConstant.XA_TRANSACTION_ID);

      Function function;
      String proxyMethod;

      if (Objects.isNull(transactionID) || transactionID.isEmpty() || "0".equals(transactionID)) {

        function =
            FunctionUtility.newConstantCallProxyFunction(
                functionEncoder,
                path.getResource(),
                abiDefinition.getMethodSignatureAsString(),
                encodedArgs);
        proxyMethod = FunctionUtility.ProxyCallMethod;
      } else {
        function =
            FunctionUtility.newConstantCallProxyFunction(
                transactionID,
                path.toString(),
                abiDefinition.getMethodSignatureAsString(),
                encodedArgs);
        proxyMethod = FunctionUtility.ProxyCallWithTransactionIdMethod;
      }

      if (logger.isDebugEnabled()) {
        logger.debug(
            " name:{}, address: {}, method: {}, args: {}",
            ChainMakerConstant.CHAIN_MAKER_PROXY_NAME,
            contractAddress,
            request.getMethod(),
            request.getArgs());
      }

      Map<String, byte[]> params = new HashMap<>();
      params.put(
          ChainMakerConstant.CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM,
          Hex.decode(functionEncoder.encode(function)));

      TransactionParams transaction =
          new TransactionParams(
              request,
              ChainMakerConstant.CHAIN_MAKER_PROXY_NAME,
              functionEncoder.buildMethodId(proxyMethod),
              params);

      Request req =
          Request.newRequest(
              ChainMakerRequestType.CALL, objectMapper.writeValueAsBytes(transaction));
      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != ChainMakerStatusCode.Success) {
                throw new ChainMakerStubException(
                    response.getErrorCode(), response.getErrorMessage());
              }
              ResultOuterClass.TxResponse txResponse =
                  objectMapper.readValue(response.getData(), ResultOuterClass.TxResponse.class);
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "call result, code: {}, msg: {}",
                    txResponse.getCode(),
                    txResponse.getMessage());
              }
              if (txResponse.getCode().getNumber()
                  == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {
                // if success, try to decode results
                transactionResponse.setErrorCode(ChainMakerStatusCode.Success);
                transactionResponse.setMessage(
                    ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
                ABIObject outputObject = ABIObjectFactory.createOutputObject(abiDefinition);
                byte[] proxyBytesOutput =
                    FunctionUtility.decodeProxyBytesOutput(
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                transactionResponse.setResult(
                    codecJsonWrapper
                        .decode(outputObject, Hex.toHexString(proxyBytesOutput))
                        .toArray(new String[0]));
              } else {
                // if error, try to decode revert msg
                transactionResponse.setErrorCode(ChainMakerStatusCode.CallNotSuccessStatus);

                Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertMessage.tryResolveRevertMessage(
                        txResponse.getCode().getNumber(),
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                if (Boolean.TRUE.equals(booleanStringTuple2.getValue1())) {
                  transactionResponse.setMessage(booleanStringTuple2.getValue2());
                } else {
                  transactionResponse.setMessage(txResponse.getMessage());
                }
              }
              callback.onTransactionResponse(null, transactionResponse);
            } catch (ChainMakerStubException e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(e.getErrorCode(), e.getMessage()), null);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()),
                  null);
            }
          });

    } catch (ChainMakerStubException e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(e.getErrorCode(), e.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }

  void asyncCallNative(
      TransactionContext context,
      TransactionRequest request,
      Connection connection,
      Callback callback) {
    TransactionResponse transactionResponse = new TransactionResponse();
    // Map<String, String> properties = connection.getProperties();

    // TODO: check properties
    try {
      Path path = context.getPath();
      String name = path.getResource();

      // TODO: get Abi by name
      String abi = ""; // resources abi

      if (abi == null) {
        throw new ChainMakerStubException(
            ChainMakerStatusCode.ABINotExist, "resource ABI not exist: " + name);
      }

      // encode
      String[] args = request.getArgs();
      String method = request.getMethod();

      ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
      ABIDefinition abiDefinition =
          contractABIDefinition.getFunctions().get(method).stream()
              .filter(function -> function.getInputs().size() == (args == null ? 0 : args.length))
              .findFirst()
              .orElseThrow(
                  () ->
                      new ChainMakerStubException(
                          ChainMakerStatusCode.MethodNotExist, "method not exist: " + method));

      byte[] encodedArgs =
          Hex.decode(
              abiCodec.encodeMethodFromString(
                  abi, method, args != null ? Arrays.asList(args) : new ArrayList<>()));

      if (logger.isDebugEnabled()) {
        logger.debug(
            " name:{}, address: {}, method: {}, args: {}",
            name,
            Utils.calcContractName(name),
            request.getMethod(),
            request.getArgs());
      }

      Map<String, byte[]> params = new HashMap<>();
      params.put(ChainMakerConstant.CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM, encodedArgs);

      TransactionParams transaction =
          new TransactionParams(
              request,
              name,
              functionEncoder.buildMethodId(abiDefinition.getMethodSignatureAsString()),
              params);
      Request req =
          Request.newRequest(
              ChainMakerRequestType.CALL, objectMapper.writeValueAsBytes(transaction));

      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != ChainMakerStatusCode.Success) {
                throw new ChainMakerStubException(
                    response.getErrorCode(), response.getErrorMessage());
              }
              ResultOuterClass.TxResponse txResponse =
                  objectMapper.readValue(response.getData(), ResultOuterClass.TxResponse.class);
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "call result, code: {}, msg: {}",
                    txResponse.getCode(),
                    txResponse.getMessage());
              }
              if (txResponse.getCode().getNumber()
                  == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {
                // if success, try to decode results
                transactionResponse.setErrorCode(ChainMakerStatusCode.Success);
                transactionResponse.setMessage(
                    ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
                ABIObject outputObject = ABIObjectFactory.createOutputObject(abiDefinition);
                transactionResponse.setResult(
                    codecJsonWrapper
                        .decode(
                            outputObject,
                            Hex.toHexString(
                                txResponse.getContractResult().getResult().toByteArray()))
                        .toArray(new String[0]));
              } else {
                // if error, try to decode revert msg
                transactionResponse.setErrorCode(ChainMakerStatusCode.CallNotSuccessStatus);

                Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertMessage.tryResolveRevertMessage(
                        txResponse.getCode().getNumber(),
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                if (Boolean.TRUE.equals(booleanStringTuple2.getValue1())) {
                  transactionResponse.setMessage(booleanStringTuple2.getValue2());
                } else {
                  transactionResponse.setMessage(txResponse.getMessage());
                }
              }
              callback.onTransactionResponse(null, transactionResponse);
            } catch (ChainMakerStubException e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(e.getErrorCode(), e.getMessage()), null);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()),
                  null);
            }
          });
    } catch (ChainMakerStubException e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(e.getErrorCode(), e.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }

  void asyncSendTransactionByProxy(
      TransactionContext context,
      TransactionRequest request,
      Connection connection,
      Callback callback) {
    TransactionResponse transactionResponse = new TransactionResponse();
    Map<String, String> properties = connection.getProperties();
    try {
      // TODO: check properties
      String contractAddress = properties.get(ChainMakerConstant.CHAIN_MAKER_PROXY_NAME);
      Path path = context.getPath();
      String name = path.getResource();

      ChainMakerPublicAccount account = (ChainMakerPublicAccount) context.getAccount();
      User user = account.getUser();
      // TODO: get abi by name
      String abi = "";
      if (abi == null) {
        throw new ChainMakerStubException(
            ChainMakerStatusCode.ABINotExist, "resource:" + name + " not exist");
      }

      // encode
      String[] args = request.getArgs();
      String method = request.getMethod();

      ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
      ABIDefinition abiDefinition =
          contractABIDefinition.getFunctions().get(method).stream()
              .filter(function -> function.getInputs().size() == (args == null ? 0 : args.length))
              .findFirst()
              .orElseThrow(
                  () ->
                      new ChainMakerStubException(
                          ChainMakerStatusCode.MethodNotExist, "method not exist: " + method));

      byte[] encodedArgs =
          Hex.decode(
              abiCodec.encodeMethodFromString(
                  abi, method, args != null ? Arrays.asList(args) : new ArrayList<>()));

      String uniqueID = (String) request.getOptions().get(StubConstant.TRANSACTION_UNIQUE_ID);
      String uid =
          Objects.nonNull(uniqueID) ? uniqueID : UUID.randomUUID().toString().replaceAll("-", "");

      String transactionID = (String) request.getOptions().get(StubConstant.XA_TRANSACTION_ID);

      Long transactionSeq = (Long) request.getOptions().get(StubConstant.XA_TRANSACTION_SEQ);
      Long seq = Objects.isNull(transactionSeq) ? 0 : transactionSeq;

      Function function;
      String proxyMethod;

      if (Objects.isNull(transactionID) || transactionID.isEmpty() || "0".equals(transactionID)) {
        proxyMethod = FunctionUtility.ProxySendTXMethod;
        function =
            FunctionUtility.newSendTransactionProxyFunction(
                functionEncoder,
                uid,
                name,
                abiDefinition.getMethodSignatureAsString(),
                encodedArgs);
      } else {
        proxyMethod = FunctionUtility.ProxySendTransactionTXMethod;
        function =
            FunctionUtility.newSendTransactionProxyFunction(
                uid,
                transactionID,
                seq,
                name,
                abiDefinition.getMethodSignatureAsString(),
                encodedArgs);
      }

      String proxyParams = functionEncoder.encode(function);

      Map<String, byte[]> params = new HashMap<>();
      params.put(ChainMakerConstant.CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM, Hex.decode(proxyParams));

      // FIXME: client chain id
      org.chainmaker.pb.common.Request.Payload payload =
          ClientUtility.createPayload(
              "", contractAddress, functionEncoder.buildMethodId(proxyMethod), params);
      // FIXME: client user orgId, hash alg
      org.chainmaker.pb.common.Request.TxRequest txRequest =
          ClientUtility.createTxRequest(payload, "", user, "");
      TransactionParams transactionParams =
          new TransactionParams(request, objectMapper.writeValueAsBytes(txRequest));
      Request req =
          Request.newRequest(
              ChainMakerRequestType.SEND_TRANSACTION,
              objectMapper.writeValueAsBytes(transactionParams));
      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != ChainMakerStatusCode.Success) {
                throw new ChainMakerStubException(
                    response.getErrorCode(), response.getErrorMessage());
              }
              ResultOuterClass.TxResponse txResponse =
                  objectMapper.readValue(response.getData(), ResultOuterClass.TxResponse.class);
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "send transaction result, code: {}, msg: {}",
                    txResponse.getCode(),
                    txResponse.getMessage());
              }
              if (txResponse.getCode().getNumber()
                  == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {
                transactionResponse.setErrorCode(ChainMakerStatusCode.Success);
                transactionResponse.setMessage(
                    ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
                ABIObject outputObject = ABIObjectFactory.createOutputObject(abiDefinition);
                byte[] proxyBytesOutput =
                    FunctionUtility.decodeProxyBytesOutput(
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                transactionResponse.setResult(
                    codecJsonWrapper
                        .decode(outputObject, Hex.toHexString(proxyBytesOutput))
                        .toArray(new String[0]));

              } else {
                transactionResponse.setErrorCode(
                    ChainMakerStatusCode.SendTransactionNotSuccessStatus);
                Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertMessage.tryResolveRevertMessage(
                        txResponse.getCode().getNumber(),
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                if (Boolean.TRUE.equals(booleanStringTuple2.getValue1())) {
                  transactionResponse.setMessage(booleanStringTuple2.getValue2());
                } else {
                  transactionResponse.setMessage(txResponse.getMessage());
                }
              }
              callback.onTransactionResponse(null, transactionResponse);
            } catch (ChainMakerStubException e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(e.getErrorCode(), e.getMessage()), null);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()),
                  null);
            }
          });
    } catch (ChainMakerStubException e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(e.getErrorCode(), e.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }

  void asyncSendTransactionNative(
      TransactionContext context,
      TransactionRequest request,
      Connection connection,
      Callback callback) {
    TransactionResponse transactionResponse = new TransactionResponse();
    // TODO: check properties
    try {
      Path path = context.getPath();
      String name = path.getResource();

      // TODO: get Abi by name
      String abi = ""; // resources abi

      if (abi == null) {
        throw new ChainMakerStubException(
            ChainMakerStatusCode.ABINotExist, "resource ABI not exist: " + name);
      }
      ChainMakerPublicAccount account = (ChainMakerPublicAccount) context.getAccount();
      User user = account.getUser();
      // encode
      String[] args = request.getArgs();
      String method = request.getMethod();

      ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
      ABIDefinition abiDefinition =
          contractABIDefinition.getFunctions().get(method).stream()
              .filter(function -> function.getInputs().size() == (args == null ? 0 : args.length))
              .findFirst()
              .orElseThrow(
                  () ->
                      new ChainMakerStubException(
                          ChainMakerStatusCode.MethodNotExist, "method not exist: " + method));

      byte[] encodedArgs =
          Hex.decode(
              abiCodec.encodeMethodFromString(
                  abi, method, args != null ? Arrays.asList(args) : new ArrayList<>()));

      if (logger.isDebugEnabled()) {
        logger.debug(
            " name:{}, address: {}, method: {}, args: {}",
            name,
            Utils.calcContractName(name),
            request.getMethod(),
            request.getArgs());
      }

      Map<String, byte[]> params = new HashMap<>();
      params.put(ChainMakerConstant.CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM, encodedArgs);
      // FIXME: client chain id
      org.chainmaker.pb.common.Request.Payload payload =
          ClientUtility.createPayload(
              "",
              Utils.calcContractName(name),
              functionEncoder.buildMethodId(abiDefinition.getMethodSignatureAsString()),
              params);
      // FIXME: client user orgId, hash alg
      org.chainmaker.pb.common.Request.TxRequest txRequest =
          ClientUtility.createTxRequest(payload, "", user, "");
      TransactionParams transactionParams =
          new TransactionParams(request, objectMapper.writeValueAsBytes(txRequest));
      Request req =
          Request.newRequest(
              ChainMakerRequestType.SEND_TRANSACTION,
              objectMapper.writeValueAsBytes(transactionParams));
      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != ChainMakerStatusCode.Success) {
                throw new ChainMakerStubException(
                    response.getErrorCode(), response.getErrorMessage());
              }
              ResultOuterClass.TxResponse txResponse =
                  objectMapper.readValue(response.getData(), ResultOuterClass.TxResponse.class);
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "call result, code: {}, msg: {}",
                    txResponse.getCode(),
                    txResponse.getMessage());
              }
              if (txResponse.getCode().getNumber()
                  == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {
                // if success, try to decode results
                transactionResponse.setErrorCode(ChainMakerStatusCode.Success);
                transactionResponse.setMessage(
                    ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
                ABIObject outputObject = ABIObjectFactory.createOutputObject(abiDefinition);
                transactionResponse.setResult(
                    codecJsonWrapper
                        .decode(
                            outputObject,
                            Hex.toHexString(
                                txResponse.getContractResult().getResult().toByteArray()))
                        .toArray(new String[0]));
              } else {
                // if error, try to decode revert msg
                transactionResponse.setErrorCode(ChainMakerStatusCode.CallNotSuccessStatus);

                Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertMessage.tryResolveRevertMessage(
                        txResponse.getCode().getNumber(),
                        Hex.toHexString(txResponse.getContractResult().getResult().toByteArray()));
                if (Boolean.TRUE.equals(booleanStringTuple2.getValue1())) {
                  transactionResponse.setMessage(booleanStringTuple2.getValue2());
                } else {
                  transactionResponse.setMessage(txResponse.getMessage());
                }
              }
              callback.onTransactionResponse(null, transactionResponse);
            } catch (ChainMakerStubException e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(e.getErrorCode(), e.getMessage()), null);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()),
                  null);
            }
          });
    } catch (ChainMakerStubException e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(e.getErrorCode(), e.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(ChainMakerStatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }
}
