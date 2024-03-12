package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import java.util.Map;

public class ChainMakerBaseFactory implements StubFactory {
    @Override
    public void init(WeCrossContext weCrossContext) {}

    @Override
    public Driver newDriver() {
        return null;
    }

    @Override
    public Connection newConnection(String s) {
        return null;
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
