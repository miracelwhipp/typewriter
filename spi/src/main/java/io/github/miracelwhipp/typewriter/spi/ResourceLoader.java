package io.github.miracelwhipp.typewriter.spi;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    private final String name;
    private final Charset charset;

    public ResourceLoader(String name, Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    public ResourceLoader(String name) {
        this(name, StandardCharsets.UTF_8);
    }

    public String getName() {
        return name;
    }

    public Charset getCharset() {
        return charset;
    }

    public InputStream open() {

        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);

        if (stream == null) {

            stream = getClass().getClassLoader().getResourceAsStream("/" + name);
        }

        return stream;
    }

    public String load() {

        try (InputStream source = open()) {

            return IOUtils.toString(source, charset);

        } catch (IOException e) {

            return null;
        }
    }
}
