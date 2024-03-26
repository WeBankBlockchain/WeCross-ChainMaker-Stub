package com.webank.wecross.stub.chainmaker.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.Utils;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.abi.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.utils.Hex;
import org.fisco.bcos.sdk.utils.Numeric;

/**
 * Function object used across blockchain chain. Wecross requires that a cross-chain contract
 * interface must conform to the following format:
 *
 * <p>function funcName(string[] params) public returns(string[])
 *
 * <p>or
 *
 * <p>function funcName() public returns(string[])
 */
@SuppressWarnings("rawtypes")
public class FunctionUtility {

    public static final int MethodIDLength = 8;
    public static final int MethodIDWithHexPrefixLength = MethodIDLength + 2;

    public static final String ProxySendTXMethod = "sendTransaction(string,string,bytes)";
    public static final String ProxySendTXMethodName = "sendTransaction";

    public static final String ProxySendTransactionTXMethod =
            "sendTransactionWithXa(string,string,uint256,string,string,bytes)";
    public static final String ProxySendTransactionTXMethodName = "sendTransactionWithXa";

    public static final String ProxyCallWithTransactionIdMethod =
            "constantCallWithXa(string,string,string,bytes)";
    public static final String ProxyCallWithTransactionIdMethodName = "constantCallWithXa";

    public static final String ProxyCallMethod = "constantCall(string,bytes)";
    public static final String ProxyCallMethodName = "constantCall";

    public static final List<TypeReference<?>> abiTypeReferenceOutputs =
            Collections.singletonList(new TypeReference<DynamicArray<Utf8String>>() {});

    /**
     * Get the function object used to encode and decode the abi parameters
     *
     * @param funcName
     * @param params
     * @return Function
     */
    public static Function newDefaultFunction(String funcName, String[] params) {

        if (Objects.isNull(params)) {
            // public func() returns(string[])
            return new Function(funcName, Arrays.<Type>asList(), abiTypeReferenceOutputs);
        }

        // public func(string[]) returns(string[])
        return new Function(
                funcName,
                Arrays.asList(
                        (0 == params.length)
                                ? new DynamicArray<>(Utf8String.class, Collections.emptyList())
                                : new DynamicArray<>(
                                        Utf8String.class,
                                        Utils.typeMap(Arrays.asList(params), Utf8String.class))),
                abiTypeReferenceOutputs);
    }

    /**
     * WeCrossProxy constantCallWithXa function <br>
     * </>function constantCallWithXa(string memory _XATransactionID, string memory _path, string
     * memory _func, bytes memory _args) public returns (bytes memory)
     *
     * @param id
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction(
            String id, String path, String methodSignature, byte[] abi) {
        return new Function(
                ProxyCallWithTransactionIdMethodName,
                Arrays.asList(
                        new Utf8String(id),
                        new Utf8String(path),
                        new Utf8String(methodSignature),
                        new DynamicBytes(abi)),
                Collections.emptyList());
    }

    /**
     * WeCrossProxy constantCall function constantCall(string memory _name, bytes memory
     * _argsWithMethodId) public returns (bytes memory)
     *
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction(
            FunctionEncoder functionEncoder, String name, String methodSignature, byte[] abi)
            throws IOException {
        String methodId = functionEncoder.buildMethodId(methodSignature);
        ByteArrayOutputStream params = new ByteArrayOutputStream();
        params.write(Hex.decode(methodId));
        if (abi != null && abi.length != 0) {
            params.write(abi);
        }
        return new Function(
                ProxyCallMethodName,
                Arrays.<Type>asList(new Utf8String(name), new DynamicBytes(params.toByteArray())),
                Collections.emptyList());
    }

    /**
     * WeCrossProxy sendTransactionWithXa function sendTransactionWithXa(string memory _uid, string
     * memory _XATransactionID, uint256 _XATransactionSeq, string memory _path, string memory _func,
     * bytes memory _args) public returns (bytes memory) {
     *
     * @param uid
     * @param tid
     * @param seq
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String uid, String tid, long seq, String path, String methodSignature, byte[] abi) {
        return new Function(
                ProxySendTransactionTXMethodName,
                Arrays.asList(
                        new Utf8String(uid),
                        new Utf8String(tid),
                        new Uint256(seq),
                        new Utf8String(path),
                        new Utf8String(methodSignature),
                        new DynamicBytes(abi)),
                Collections.emptyList());
    }

    /**
     * WeCrossProxy sendTransaction function sendTransaction(string memory _uid, string memory
     * _name, bytes memory _argsWithMethodId) public returns (bytes memory)
     *
     * @param uid
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            FunctionEncoder functionEncoder,
            String uid,
            String name,
            String methodSignature,
            byte[] abi)
            throws IOException {
        String methodId = functionEncoder.buildMethodId(methodSignature);
        ByteArrayOutputStream params = new ByteArrayOutputStream();
        params.write(Hex.decode(methodId));
        if (abi != null && abi.length != 0) {
            params.write(abi);
        }
        return new Function(
                ProxySendTXMethodName,
                Arrays.asList(
                        new Utf8String(uid),
                        new Utf8String(name),
                        new DynamicBytes(params.toByteArray())),
                Collections.emptyList());
    }

    /**
     * decode WeCrossProxy constantCall input
     *
     * @param input
     * @return
     */
    public static Tuple4<String, String, String, byte[]> getConstantCallProxyFunctionInput(
            String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);
        final Function function =
                new Function(
                        ProxyCallWithTransactionIdMethodName,
                        Collections.emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple4<>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (byte[]) results.get(3).getValue());
    }

    /**
     * decode WeCrossProxy constantCall input
     *
     * @param input
     * @return
     */
    public static Tuple2<String, byte[]> getConstantCallFunctionInput(String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);
        final Function function =
                new Function(
                        ProxyCallMethodName,
                        Collections.emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple2<>((String) results.get(0).getValue(), (byte[]) results.get(1).getValue());
    }

    /**
     * decode WeCrossProxy sendTransaction input
     *
     * @param input
     * @return
     */
    public static Tuple6<String, String, BigInteger, String, String, byte[]>
            getSendTransactionProxyFunctionInput(String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);

        final Function function =
                new Function(
                        ProxySendTransactionTXMethodName,
                        Collections.emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple6<>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (String) results.get(4).getValue(),
                (byte[]) results.get(5).getValue());
    }

    public static byte[] decodeProxyBytesOutput(String output) {
        List<TypeReference<?>> outputParameters =
                Arrays.asList(new TypeReference<DynamicBytes>() {});
        List<Type> results = FunctionReturnDecoder.decode(output, Utils.convert(outputParameters));

        return (byte[]) results.get(0).getValue();
    }

    /**
     * decode WeCrossProxy sendTransaction input
     *
     * @param input
     * @return
     */
    public static Tuple3<String, String, byte[]> getSendTransactionProxyWithoutTxIdFunctionInput(
            String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);

        final Function function =
                new Function(
                        ProxySendTXMethodName,
                        Collections.emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple3<>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (byte[]) results.get(2).getValue());
    }

    public static List<String> convertToStringList(List<Type> typeList) {
        List<String> stringList = new ArrayList<>();
        if (!typeList.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Utf8String> utf8StringList = ((DynamicArray) typeList.get(0)).getValue();
            for (Utf8String utf8String : utf8StringList) {
                stringList.add(utf8String.getValue());
            }
        }
        return stringList;
    }

    /**
     * @param input
     * @return
     */
    public static String[] decodeDefaultInput(String input) {
        if (Objects.isNull(input) || input.length() < MethodIDLength) {
            return null;
        }

        // function funcName() public returns(string[])
        if (input.length() == MethodIDLength) {
            return new String[0];
        }

        return decodeDefaultOutput(input.substring(MethodIDLength));
    }

    /**
     * decode abi encode data
     *
     * @param output
     * @return
     */
    public static String[] decodeDefaultOutput(String output) {
        if (Objects.isNull(output) || output.length() < MethodIDLength) {
            return null;
        }

        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output, Utils.convert(FunctionUtility.abiTypeReferenceOutputs));
        List<String> outputArgs = FunctionUtility.convertToStringList(outputTypes);
        return outputArgs.toArray(new String[0]);
    }

    public static String decodeOutputAsString(String output) {
        if (Objects.isNull(output) || output.length() < MethodIDLength) {
            return null;
        }

        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output,
                        Utils.convert(
                                Collections.singletonList(new TypeReference<Utf8String>() {})));
        if (Objects.isNull(outputTypes) || outputTypes.isEmpty()) {
            return null;
        }

        return (String) outputTypes.get(0).getValue();
    }
}
