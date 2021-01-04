package io.github.miracelwhipp.typewriter.conversion;

import io.github.miracelwhipp.typewriter.WrappedCheckedException;
import io.github.miracelwhipp.typewriter.conversion.freemarker.FreemarkerConfiguration;
import io.github.miracelwhipp.typewriter.conversion.freemarker.FreemarkerConversion;
import io.github.miracelwhipp.typewriter.conversion.markdown.MarkdownToHtmlConversion;
import io.github.miracelwhipp.typewriter.conversion.markdown.MarkdownToPdfConversion;
import io.github.miracelwhipp.typewriter.conversion.markdown.MarkdownToXhtmlConversion;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

public class ConversionSystem {

    private final List<Conversion> conversions;
    private final Map<String, BiConsumer<InputStream, OutputStream>> conversionsByConversionPath = new HashMap<>();
    private final System system;

    public ConversionSystem(List<Conversion> conversions, System system) {
        this.conversions = conversions;
        this.system = system;
    }

    public void convert(ConversionDescription conversions, File source, File targetDirectory) {

        if (!source.exists()) {

            return;
        }


        if (source.isDirectory()) {

            File newTarget = new File(targetDirectory, source.getName());

            try {

                Files.list(source.toPath()).forEach(entry -> convert(conversions, entry.toFile(), newTarget));

            } catch (IOException e) {

                throw new WrappedCheckedException(e);
            }

            return;
        }


        ConversionDescription usedConversions = computeConversions(conversions, source);

        File target = new File(targetDirectory, source.getName().split("\\.")[0] + "." + usedConversions.getFinalExtension());

        BiConsumer<InputStream, OutputStream> conversion = conversionsByConversionPath.computeIfAbsent(usedConversions.getConversionKey(), key -> {

            List<String> conversionChain = usedConversions.getConversions();

            if (conversionChain.size() <= 1) {

                return (x, y) -> {
                    try {

                        IOUtil.copy(x, y);

                    } catch (IOException e) {

                        throw new WrappedCheckedException(e);
                    }
                };
            }

            ConversionContext context = new ConversionContext(system, source, target);

            BiConsumer<InputStream, OutputStream> result = null;

            for (int index = 1; index < conversionChain.size(); index++) {

                String elementSource = conversionChain.get(index - 1);
                String elementTarget = conversionChain.get(index);

                Conversion fileConverter = this.conversions.stream().filter(converter ->
                        converter.sourceExtension().matcher(elementSource).matches() &&
                                converter.targetExtension().matcher(elementTarget).matches()
                ).findFirst().orElseThrow(IllegalStateException::new);

                if (result == null) {

                    result = (x, y) -> fileConverter.convert(x, y, context);

                } else {

                    BiConsumer<InputStream, OutputStream> innerResult = result;

                    result = (inputStream, outputStream) -> {

                        try (ByteArrayOutputStream innerTarget = new ByteArrayOutputStream()) {

                            innerResult.accept(inputStream, innerTarget);

                            try (ByteArrayInputStream innerSource = new ByteArrayInputStream(innerTarget.toByteArray())) {

                                fileConverter.convert(innerSource, outputStream, context);
                            }

                        } catch (IOException e) {

                            throw new WrappedCheckedException(e);
                        }
                    };
                }
            }

            return result;
        });

        try {

            FileUtils.forceMkdir(targetDirectory);

            try (
                    FileInputStream sourceStream = new FileInputStream(source);
                    FileOutputStream targetStream = new FileOutputStream(target);
            ) {

                conversion.accept(sourceStream, targetStream);
            }

        } catch (IOException e) {

            throw new WrappedCheckedException(e);
        }

    }

    public static ConversionSystem defaultConversions(boolean debug, FreemarkerConfiguration freemarkerConfiguration, System system) {

        //TODO: collect custom conversions??

        MarkdownToHtmlConversion markdownToHtml = new MarkdownToHtmlConversion(debug);
        MarkdownToXhtmlConversion markdownToXhtml = new MarkdownToXhtmlConversion(debug);

        FreemarkerConversion freemarkerConversion = new FreemarkerConversion(freemarkerConfiguration);

        HtmlToPdfConversion htmlToPdfConversion = new HtmlToPdfConversion();

        MarkdownToPdfConversion markdownToPdfConversion = new MarkdownToPdfConversion();

        return new ConversionSystem(Arrays.asList(
                markdownToHtml,
                markdownToXhtml,
                htmlToPdfConversion,
                freemarkerConversion,
                markdownToPdfConversion
        ), system);
    }

    private ConversionDescription computeConversions(ConversionDescription conversions, File source) {

        String[] split = source.getName().split("\\.");

        if (split.length <= 1) {

            return conversions;
        }

        List<String> prefix = Arrays.asList(Arrays.copyOfRange(split, 1, split.length));

        Collections.reverse(prefix);

        return ConversionDescription.of(prefix).append(conversions);
    }

}
