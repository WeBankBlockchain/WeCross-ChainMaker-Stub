package com.webank.wecross.stub.chainmaker.config;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class ChainMakerStubConfigParserTest {
    @Test
    public void stubConfigParserTest() throws IOException {
        ChainMakerStubConfigParser chainMakerStubConfigParser = new ChainMakerStubConfigParser("./", "stub.toml");
        ChainMakerStubConfig chainMakerStubConfig = chainMakerStubConfigParser.loadConfig();

        ChainMakerStubConfig.Common common = chainMakerStubConfig.getCommon();
        assertTrue(Objects.nonNull(common));
        assertEquals(common.getName(), "chainmaker");
        assertEquals(common.getType(), "PK");

        ChainMakerStubConfig.Chain chain = chainMakerStubConfig.getChain();
        assertTrue(Objects.nonNull(chain));
        assertEquals(chain.getChainId(), "pkchain01");
        assertEquals(chain.getSignKeyPath(), "sign.key");
        assertEquals(chain.getAuthType(), "public");
        ChainMakerStubConfig.Chain.Crypto crypto = chain.getCrypto();
        assertTrue(Objects.nonNull(crypto));
        assertEquals(crypto.getHash(), "SHA256");
        List<ChainMakerStubConfig.Chain.Node> nodes = chain.getNodes();
        assertFalse(nodes.isEmpty());
        ChainMakerStubConfig.Chain.RpcClient rpcClient = chain.getRpcClient();
        assertTrue(Objects.nonNull(rpcClient));
        assertEquals(rpcClient.getMaxReceiveMessageSize(), 100);

        List<ChainMakerStubConfig.Resource> resources = chainMakerStubConfig.getResources();
        assertTrue(Objects.nonNull(resources) && resources.size() == 2);
        assertEquals(resources.get(0).getName(), "WeCrossProxy");
        assertEquals(resources.get(0).getType(), "CM_CONTRACT");
        assertEquals(resources.get(0).getValue(), "1");
        assertEquals(resources.get(1).getName(), "WeCrossHub");
        assertEquals(resources.get(1).getType(), "CM_CONTRACT");
        assertEquals(resources.get(1).getValue(), "2");

    }
}
