package io.github.miracelwhipp.typewriter.conversion.markdown;

import io.github.miracelwhipp.typewriter.WrappedCheckedException;
import io.github.miracelwhipp.typewriter.conversion.Conversion;
import io.github.miracelwhipp.typewriter.conversion.ConversionContext;
import io.github.miracelwhipp.typewriter.conversion.HtmlToPdfConversion;

import java.io.*;
import java.util.regex.Pattern;

public class MarkdownToPdfConversion implements Conversion {

    private final MarkdownToXhtmlConversion step1 = new MarkdownToXhtmlConversion(false);
    private final HtmlToPdfConversion step2 = new HtmlToPdfConversion();

    @Override
    public Pattern sourceExtension() {
        return Pattern.compile("md");
    }

    @Override
    public Pattern targetExtension() {

        return Pattern.compile("pdf");
    }

    @Override
    public void convert(InputStream source, OutputStream target, ConversionContext context) {

        try (ByteArrayOutputStream innerTarget = new ByteArrayOutputStream()) {

            step1.convert(source, innerTarget, context);

            step2.convert(new ByteArrayInputStream(innerTarget.toByteArray()), target, context);

        } catch (IOException e) {

            throw new WrappedCheckedException(e);
        }
    }
}
