package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainMakerBaseFactory implements StubFactory {
  private final Logger logger = LoggerFactory.getLogger(ChainMakerBaseFactory.class);

  @Override
  public void init(WeCrossContext weCrossContext) {}

  @Override
  public Driver newDriver() {
    return null;
  }

  @Override
  public Connection newConnection(String path) {
    try {
      ChainMakerConnection connection =
          ChainMakerConnectionFactory.build(path, ChainMakerConstant.STUB_TOML_NAME);

      // check proxy contract
      if (!connection.hasProxyDeployed()) {
        String errorMsg = "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
        // String help =
        // "Please deploy WeCrossProxy contract by: "
        // + ProxyContractDeployment.getUsage(path);
        // System.out.println(errorMsg + "\n" + help);
        throw new Exception(errorMsg);
      }

      // check hub contract
      if (!connection.hasHubDeployed()) {
        String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
        // String help =
        // "Please deploy WeCrossHub contract by: "
        // + HubContractDeployment.getUsage(path);
        // System.out.println(errorMsg + "\n" + help);
        throw new Exception(errorMsg);
      }

      return connection;
    } catch (Exception e) {
      logger.error(" newConnection, e: ", e);
      return null;
    }
  }

  @Override
  public Account newAccount(Map<String, Object> map) {
    return null;
  }

  @Override
  public void generateAccount(String s, String[] strings) {}

  @Override
  public void generateConnection(String s, String[] strings) {}
}
