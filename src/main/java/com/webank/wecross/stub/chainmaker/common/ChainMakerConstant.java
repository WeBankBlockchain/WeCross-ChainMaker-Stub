package com.webank.wecross.stub.chainmaker.common;

import com.webank.wecross.stub.StubConstant;

public interface ChainMakerConstant {
  String GM_STUB_SUFFIX = "GM";

  String STUB_TOML_NAME = "stub.toml";

  String CHAIN_MAKER_PROXY_NAME = StubConstant.PROXY_NAME;
  String CHAIN_MAKER_HUB_NAME = StubConstant.HUB_NAME;
  String CHAIN_MAKER_ECDSA_EVM_STUB_TYPE = "CHAIN_MAKER";
  String CHAIN_MAKER_GM_EVM_STUB_TYPE = "CHAIN_MAKER_GM";

  String CHAIN_MAKER_PROPERTY_ABI_SUFFIX = "ABI";
  String CHAIN_MAKER_PROPERTY_STUB_TYPE = "CHAIN_MAKER_PROPERTY_STUB_TYPE";
  String CHAIN_MAKER_PROPERTY_CHAIN_ID = "CHAIN_MAKER_PROPERTY_CHAIN_ID";
  String CHAIN_MAKER_PROPERTY_AUTH_TYPE = "CHAIN_MAKER_PROPERTY_AUTH_TYPE";

  String CHAIN_MAKER_CONTRACT_ARGS_EVM_PARAM = "data";

  String PROXY_METHOD_GET_PATHS = "getPaths";
}
