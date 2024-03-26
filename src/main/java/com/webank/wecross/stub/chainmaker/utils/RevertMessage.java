package com.webank.wecross.stub.chainmaker.utils;

import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevertMessage {

    private static final Logger logger = LoggerFactory.getLogger(RevertMessage.class);

    public static final String RevertMethod = "08c379a0";
    public static final String RevertMethodWithHexPrefix = "0x08c379a0";

    public static final String SMRevertMethod = "c703cb12";
    public static final String SMRevertMethodWithHexPrefix = "0xc703cb12";

    // Error(String)
    public static final Function revertFunction =
            new Function(
                    "Error",
                    Collections.<Type>emptyList(),
                    Collections.singletonList(new TypeReference<Utf8String>() {}));

    public static boolean isOutputStartWithRevertMethod(String output) {
        return output.startsWith(RevertMethodWithHexPrefix)
                || output.startsWith(SMRevertMethodWithHexPrefix)
                || (output.startsWith(RevertMethod) || output.startsWith(SMRevertMethod));
    }

    public static boolean hasRevertMessage(Integer status, String output) {
        if (StringUtils.isEmpty(output)) {
            return false;
        }
        try {
            return status != 0 && isOutputStartWithRevertMethod(output);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param message
     * @return
     */
    public static String getErrorMessage(String message) {

        // FIXME: need to be implemented
        return "";
    }

    /**
     * try to resolve revert message, supports recursive operations
     *
     * @param status
     * @param output
     * @return
     */
    public static Tuple2<Boolean, String> tryResolveRevertMessage(Integer status, String output) {
        if (!hasRevertMessage(status, output)) {
            return new Tuple2<>(false, null);
        }

        try {
            String rawOutput =
                    Numeric.containsHexPrefix(output)
                            ? output.substring(RevertMethodWithHexPrefix.length())
                            : output.substring(RevertMethod.length());
            List<Type> result =
                    FunctionReturnDecoder.decode(rawOutput, revertFunction.getOutputParameters());
            if (result.get(0) instanceof Utf8String) {
                String message = ((Utf8String) result.get(0)).getValue();
                if (logger.isDebugEnabled()) {
                    logger.debug(" ABI: {} , RevertMessage: {}", output, message);
                }
                return new Tuple2<>(true, message);
            }
        } catch (Exception e) {
            logger.warn(" ABI: {}, e: {}", output, e);
        }

        return new Tuple2<>(false, null);
    }
}
