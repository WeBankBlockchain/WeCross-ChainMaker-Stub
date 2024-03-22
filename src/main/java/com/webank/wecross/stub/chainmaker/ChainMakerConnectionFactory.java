package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.chainmaker.client.AbstractClientWrapper;
import com.webank.wecross.stub.chainmaker.client.ClientWrapperFactory;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfig;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChainMakerConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerConnectionFactory.class);

    public static ChainMakerConnection build(String stubConfigPath, String configName)
            throws Exception {
        ScheduledExecutorService executorService =
                new ScheduledThreadPoolExecutor(
                        4, new CustomizableThreadFactory("tmpChainMakerConn-"));
        return build(stubConfigPath, configName, executorService);
    }

    public static ChainMakerConnection build(
            String stubConfigPath, String configName, ScheduledExecutorService executorService)
            throws Exception {
        logger.info("stubConfigPath: {} ", stubConfigPath);
        ChainMakerStubConfigParser chainMakerStubConfigParser =
                new ChainMakerStubConfigParser(stubConfigPath, configName);
        ChainMakerStubConfig chainMakerStubConfig = chainMakerStubConfigParser.loadConfig();
        AbstractClientWrapper clientWrapper =
                ClientWrapperFactory.createClientWrapperInstance(chainMakerStubConfig);
        build(chainMakerStubConfig, clientWrapper, executorService);
        return build(chainMakerStubConfig, clientWrapper, executorService);
    }

    public static ChainMakerConnection build(
            ChainMakerStubConfig chainMakerStubConfig, AbstractClientWrapper clientWrapper) {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(
                        4, new CustomizableThreadFactory("tmpChainMakerConn-"));
        return build(chainMakerStubConfig, clientWrapper, scheduledExecutorService);
    }

    public static ChainMakerConnection build(
            ChainMakerStubConfig chainMakerStubConfig,
            AbstractClientWrapper clientWrapper,
            ScheduledExecutorService executorService) {
        logger.info("chainMakerStubConfig: {}", chainMakerStubConfig);
        ChainMakerConnection connection = new ChainMakerConnection(clientWrapper, executorService);
        connection.setResourceInfoList(chainMakerStubConfig.convertToResourceInfos());

        connection.addProperty(
                ChainMakerConstant.CHAIN_MAKER_PROPERTY_CHAIN_ID,
                chainMakerStubConfig.getChain().getChainId());
        connection.addProperty(
                ChainMakerConstant.CHAIN_MAKER_PROPERTY_AUTH_TYPE,
                chainMakerStubConfig.getChain().getAuthType());
        connection.addProperty(
                ChainMakerConstant.CHAIN_MAKER_PROPERTY_STUB_TYPE,
                chainMakerStubConfig.getCommon().getType());
        // from config build resources
        List<ChainMakerStubConfig.Resource> resources = chainMakerStubConfig.getResources();
        if (!resources.isEmpty()) {
            for (ChainMakerStubConfig.Resource resource : resources) {
                if (Objects.equals(resource.getName(), ChainMakerConstant.CHAIN_MAKER_PROXY_NAME)) {
                    connection.addProperty(
                            ChainMakerConstant.CHAIN_MAKER_PROXY_NAME, resource.getValue());
                }
                if (Objects.equals(resource.getName(), ChainMakerConstant.CHAIN_MAKER_HUB_NAME)) {
                    connection.addProperty(
                            ChainMakerConstant.CHAIN_MAKER_HUB_NAME, resource.getValue());
                }
                // TODO 将资源path注册到proxy合约
            }
        }
        return connection;
    }
}
