package io.github.miracelwhipp.typewriter.conversion;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public interface Conversion {

    Pattern sourceExtension();

    Pattern targetExtension();

    void convert(InputStream source, OutputStream target, ConversionContext context);
}
