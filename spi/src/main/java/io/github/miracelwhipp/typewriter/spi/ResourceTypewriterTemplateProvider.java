package io.github.miracelwhipp.typewriter.spi;

import java.nio.charset.Charset;

public abstract class ResourceTypewriterTemplateProvider extends ResourceLoader implements TypewriterTemplateProvider {


    protected ResourceTypewriterTemplateProvider(String name, Charset charset) {
        super(name, charset);
    }

    public ResourceTypewriterTemplateProvider(String name) {
        super(name);
    }

    @Override
    public String templateName() {
        return getName();
    }

    @Override
    public String templateContent() {

        return load();
    }

}
