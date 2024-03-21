package com.webank.wecross.stub.chainmaker.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.chainmaker.common.ChainMakerToml;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainMakerStubConfigParser extends AbstractChainMakerConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(ChainMakerStubConfigParser.class);

    public ChainMakerStubConfigParser(String configPath, String configName) {
        super(configPath + File.separator + configName);
    }

    public ChainMakerStubConfig loadConfig() throws IOException {
        ChainMakerToml chainMakerToml = new ChainMakerToml(getConfigPath());
        Toml toml = chainMakerToml.getToml();
        return toml.to(ChainMakerStubConfig.class);
    }
}
