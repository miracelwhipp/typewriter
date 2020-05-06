package io.github.miracelwhipp.typewriter.spi;

import java.nio.charset.Charset;

public abstract class ResourceCssProvider extends ResourceLoader implements CssProvider {

    public ResourceCssProvider(String name, Charset charset) {
        super(name, charset);
    }

    public ResourceCssProvider(String name) {
        super(name);
    }

    @Override
    public String content() {
        return load();
    }
}
