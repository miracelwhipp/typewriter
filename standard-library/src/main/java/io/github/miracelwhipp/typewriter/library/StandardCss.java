package io.github.miracelwhipp.typewriter.library;

import io.github.miracelwhipp.typewriter.spi.ResourceCssProvider;

public class StandardCss extends ResourceCssProvider {

    public StandardCss() {
        super("typewriter.css");
    }
}
