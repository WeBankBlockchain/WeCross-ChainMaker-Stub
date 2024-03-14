package com.webank.wecross.stub.chainmaker.common;

import com.moandjiezana.toml.Toml;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

public class ChainMakerToml {
    private final String path;

    public ChainMakerToml(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Toml getToml() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(getPath());
        return new Toml().read(resource.getInputStream());
    }
}
