package io.github.miracelwhipp.typewriter.conversion.markdown;

import com.openhtmltopdf.layout.SharedContext;
import com.openhtmltopdf.resource.XMLResource;
import com.openhtmltopdf.util.ThreadCtx;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.github.miracelwhipp.typewriter.ElementConverterDomMutator;
import io.github.miracelwhipp.typewriter.WrappedCheckedException;
import io.github.miracelwhipp.typewriter.conversion.Conversion;
import io.github.miracelwhipp.typewriter.conversion.ConversionContext;
import io.github.miracelwhipp.typewriter.spi.CssProvider;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.codehaus.plexus.util.IOUtil;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class MarkdownConversion implements Conversion {

    private static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n<html>\n<head>\n";
    private static final String HTML_CLOSE_HEAD = "</head>\n<body>\n";
    private static final String HTML_FOOTER = "\n</body></html>";

    protected final boolean debug;
    private String defaultCss;

    public MarkdownConversion(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Pattern sourceExtension() {
        return Pattern.compile("md");
    }

    @Override
    public void convert(InputStream source, OutputStream target, ConversionContext context) {

        try (InputStream original = source) {

            MutableDataSet options = new MutableDataSet();

            //TODO: check available options
            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    MacroExtension.create()
            ));

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            String markdown = new String(IOUtil.toByteArray(original), context.getSystem().getSourceEncoding());

            Node document = parser.parse(markdown);

            StringBuilder html = new StringBuilder();

            html.append(HTML_HEADER);

            String defaultStyle = collectCss();

            html.append(defaultStyle);

            html.append(HTML_CLOSE_HEAD);

            html.append(renderer.render(document));

            html.append(HTML_FOOTER);

            byte[] htmlBytes = html.toString().getBytes(context.getSystem().getSourceEncoding());

            if (debug) {

                File targetFile = new File(context.getTargetFile().getParentFile(), context.getConvertedFile().getName() + ".html");

                Files.write(targetFile.toPath(), htmlBytes);
            }

            byte[] transformed = applyMonitors(context, new ByteArrayInputStream(htmlBytes));

            IOUtil.copy(transformed, target);

        } catch (IOException | TransformerException e) {

            throw new WrappedCheckedException(e);
        }
    }

    private byte[] applyMonitors(ConversionContext context, InputStream source) throws TransformerException, IOException {

        try (
                InputStream inputStream = source;
                ByteArrayOutputStream intermediate = new ByteArrayOutputStream()
        ) {

//            List<Monitor> monitors = SensorRegistry.INSTANCE.getSensors(source.toFile());
            List<Monitor> monitors = context.allOf(Monitor.class).collect(Collectors.toList());

            if (monitors.isEmpty()) {

                return IOUtil.toByteArray(source);
            }


            SharedContext sharedContext = new SharedContext();
            sharedContext._preferredDocumentBuilderFactoryImplementationClass = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

            ThreadCtx.get().setSharedContext(sharedContext);

            XMLResource resource = XMLResource.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            Document xmlDocument = resource.getDocument();


            monitors.forEach(sensor -> new ElementConverterDomMutator(sensor.newEvaluator()).mutateDocument(xmlDocument));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            setTransformerOutputProperties(transformer);

            StreamResult result = new StreamResult(intermediate);
            DOMSource domSource = new DOMSource(xmlDocument);
            transformer.transform(domSource, result);

            return intermediate.toByteArray();
        }
    }

    protected abstract void setTransformerOutputProperties(Transformer transformer);

    private String collectCss() {

        if (defaultCss != null) {

            return defaultCss;
        }

        ServiceLoader<CssProvider> loader = ServiceLoader.load(CssProvider.class);

        StringBuilder css = new StringBuilder();

        loader.forEach(cssProvider -> {

            css.append("<style>\n");

            css.append(cssProvider.content());

            css.append("</style>\n");
        });

        return defaultCss = css.toString();
    }
}
