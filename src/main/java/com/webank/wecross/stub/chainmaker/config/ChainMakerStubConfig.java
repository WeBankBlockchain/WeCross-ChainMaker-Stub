package com.webank.wecross.stub.chainmaker.config;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChainMakerStubConfig {
    private static final Logger logger = LoggerFactory.getLogger(Logger.class);

    private Common common;
    private Chain chain;
    private List<Resource> resources;

    public static class Common {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Common{" + "name='" + name + '\'' + ", type='" + type + '\'' + '}';
        }
    }

    public static class Chain {
        private String chainId;
        private String signKeyPath;
        private String authType;
        private Crypto crypto;
        private List<Node> nodes;
        private RpcClient rpcClient;

        public static class Crypto {
            private String hash;

            public String getHash() {
                return hash;
            }

            public void setHash(String hash) {
                this.hash = hash;
            }

            @Override
            public String toString() {
                return "Crypto{" + "hash='" + hash + '\'' + '}';
            }
        }

        public static class Node {
            private String nodeAddr;

            public String getNodeAddr() {
                return nodeAddr;
            }

            public void setNodeAddr(String nodeAddr) {
                this.nodeAddr = nodeAddr;
            }

            @Override
            public String toString() {
                return "Node{" + "nodeAddr='" + nodeAddr + '\'' + '}';
            }
        }

        public static class RpcClient {
            private long callTimeout = 10000;
            private long syncResultTimeout = 10000;
            private int maxReceiveMessageSize = 100;

            public long getCallTimeout() {
                return callTimeout;
            }

            public void setCallTimeout(long callTimeout) {
                this.callTimeout = callTimeout;
            }

            public long getSyncResultTimeout() {
                return syncResultTimeout;
            }

            public void setSyncResultTimeout(long syncResultTimeout) {
                this.syncResultTimeout = syncResultTimeout;
            }

            public int getMaxReceiveMessageSize() {
                return maxReceiveMessageSize;
            }

            public void setMaxReceiveMessageSize(int maxReceiveMessageSize) {
                this.maxReceiveMessageSize = maxReceiveMessageSize;
            }

            @Override
            public String toString() {
                return "RpcClient{" +
                        "callTimeout=" + callTimeout +
                        ", syncResultTimeout=" + syncResultTimeout +
                        ", maxReceiveMessageSize=" + maxReceiveMessageSize +
                        '}';
            }
        }

        public String getChainId() {
            return chainId;
        }

        public void setChainId(String chainId) {
            this.chainId = chainId;
        }

        public String getSignKeyPath() {
            return signKeyPath;
        }

        public void setSignKeyPath(String signKeyPath) {
            this.signKeyPath = signKeyPath;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public Crypto getCrypto() {
            return crypto;
        }

        public void setCrypto(Crypto crypto) {
            this.crypto = crypto;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }

        public RpcClient getRpcClient() {
            return rpcClient;
        }

        public void setRpcClient(RpcClient rpcClient) {
            this.rpcClient = rpcClient;
        }
    }

    public static class Resource {
        private String name;
        private String type;
        private String value;
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "Resource{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<ResourceInfo> convertToResourceInfos() {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        for (Resource resource : this.getResources()) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(resource.getName());
            resourceInfo.setStubType(this.getCommon().getType());
            resourceInfo.getProperties().put(resource.getName(), resource.getValue());
            resourceInfo
                    .getProperties()
                    .put(
                            ChainMakerConstant.CHAIN_MAKER_PROPERTY_CHAIN_ID,
                            this.getChain().getChainId());
            resourceInfo
                    .getProperties()
                    .put(
                            ChainMakerConstant.CHAIN_MAKER_PROPERTY_AUTH_TYPE,
                            this.getChain().getAuthType());
            resourceInfos.add(resourceInfo);
        }
        logger.info(" resource list: {}", resourceInfos);
        return resourceInfos;
    }
}
