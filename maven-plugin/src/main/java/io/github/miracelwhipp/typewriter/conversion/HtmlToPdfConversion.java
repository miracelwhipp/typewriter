package io.github.miracelwhipp.typewriter.conversion;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import io.github.miracelwhipp.typewriter.ElementConverterDomMutator;
import io.github.miracelwhipp.typewriter.WrappedCheckedException;
import io.github.miracelwhipp.typewriter.font.FontDescription;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.codehaus.plexus.util.IOUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlToPdfConversion implements Conversion {

    @Override
    public Pattern sourceExtension() {

        return Pattern.compile("htm|html|xhtml");
    }

    @Override
    public Pattern targetExtension() {

        return Pattern.compile("pdf");
    }

    @Override
    public void convert(InputStream source, OutputStream target, ConversionContext context) {

        if (false) {
            // TODO: move to markdown to html conversion

//            try (
//                    InputStream inputStream = new FileInputStream(source.toFile());
//                    OutputStream outputStream = new FileOutputStream(new File(source.toFile().getParentFile(), target.toFile().getName() + ".html"))
//            ) {
//
//                SharedContext sharedContext = new SharedContext();
//                sharedContext._preferredDocumentBuilderFactoryImplementationClass = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";
//
//                ThreadCtx.get().setSharedContext(sharedContext);
//
//                XMLResource resource = XMLResource.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//
//                Document document = resource.getDocument();
//
//                List<Monitor> monitors = SensorRegistry.INSTANCE.getSensors(source.toFile());
//
//                monitors.forEach(sensor -> {
//
//                    new ElementConverterDomMutator(sensor.newEvaluator()).mutateDocument(document);
//                });
//
//                Transformer transformer = TransformerFactory.newInstance().newTransformer();
//                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//
//                StreamResult result = new StreamResult(outputStream);
//                DOMSource domSource = new DOMSource(document);
//                transformer.transform(domSource, result);
//
//            } catch (TransformerException e) {
//
//                throw new WrappedCheckedException(e);
//            }
        }

        try (OutputStream outputStream = target) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            FontDescription.getSystemFonts().forEach(fontDescription -> {

                context.getSystem().getLog().debug("registering " + fontDescription.getLocation() + " for font family " + fontDescription.getFamily());

                builder.useFont(fontDescription.getLocation(), fontDescription.getFamily());
            });

//            List<Monitor> monitors = SensorRegistry.INSTANCE.getSensors(source.toFile());
            List<Monitor> monitors = context.allOf(Monitor.class).collect(Collectors.toList());

            monitors.forEach(sensor -> {

                builder.addDOMMutator(new ElementConverterDomMutator(sensor.newEvaluator()));
            });

            builder.useFastMode()
                    .useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory())
                    .useUnicodeBidiReorderer(new ICUBidiReorderer())
                    .defaultTextDirection(BaseRendererBuilder.TextDirection.LTR)
                    .useSVGDrawer(new BatikSVGDrawer())
                    .useMathMLDrawer(new MathMLDrawer())
                    .addDOMMutator(LaTeXDOMMutator.INSTANCE)
                    .withHtmlContent(new String(IOUtil.toByteArray(source), context.getSystem().getSourceEncoding()), context.getConvertedFile().getParent())
                    .toStream(outputStream)
                    .run();

        } catch (Exception e) {

            throw new WrappedCheckedException(e);

        }

    }
}
