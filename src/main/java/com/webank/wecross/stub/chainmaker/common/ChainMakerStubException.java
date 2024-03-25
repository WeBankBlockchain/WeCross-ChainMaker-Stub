package com.webank.wecross.stub.chainmaker.common;

import com.webank.wecross.exception.WeCrossException;

public class ChainMakerStubException extends WeCrossException {
    public ChainMakerStubException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public ChainMakerStubException(Integer errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ChainMakerStubException(
            Integer errorCode, String message, Integer internalErrorCode, String internalMessage) {
        super(errorCode, message, internalErrorCode, internalMessage);
    }
}
