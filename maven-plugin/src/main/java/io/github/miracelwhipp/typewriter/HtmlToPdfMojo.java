package io.github.miracelwhipp.typewriter;

import com.google.common.base.Strings;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import io.github.miracelwhipp.typewriter.font.FontDescription;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import io.github.miracelwhipp.typewriter.spi.util.ModifiedFiles;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Mojo(name = "pdf", defaultPhase = LifecyclePhase.COMPILE)
public class HtmlToPdfMojo extends AbstractTypewriterMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            String htmlFileName = getHtmlFileName(sourceFile);

            File htmlFile = new File(htmlDirectory, htmlFileName);

            if (!htmlFile.isFile()) {

                getLog().info("skipping typewriter pdf from html - source " + htmlFile + " not found");

                return;
            }

            String targetFilename = FilenameUtils.removeExtension(htmlFile.getName()) + "." + FileExtensions.PDF;
            File targetFile = new File(pdfDirectory, targetFilename);

            ModifiedFiles.apply(htmlFile, targetFile, (source, target) -> {

                try (OutputStream outputStream = new FileOutputStream(target.toFile())) {

                    PdfRendererBuilder builder = new PdfRendererBuilder();

                    FontDescription.getSystemFonts().forEach(fontDescription -> {

                        getLog().debug("registering " + fontDescription.getLocation() + " for font family " + fontDescription.getFamily());

                        builder.useFont(fontDescription.getLocation(), fontDescription.getFamily());
                    });

                    List<Monitor> monitors = SensorRegistry.INSTANCE.getSensors(source.toFile());

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
                            .withFile(source.toFile())
                            .toStream(outputStream)
                            .run();

                    project.getArtifact().setFile(targetFile);
//                    projectHelper.attachArtifact(project, "pdf", targetFile);

                } catch (IOException e) {

                    throw e;

                } catch (Exception e) {

                    throw new IOException(e.getMessage(), e);
                }
            });

        } catch (IOException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private String getHtmlFileName(String sourceFile) {

        String htmlFileName = sourceFile;

        String extension = FilenameUtils.getExtension(htmlFileName);

        if (FileExtensions.HTML_EXTENSIONS.contains(extension.toLowerCase())) {

            return htmlFileName;
        }

        htmlFileName = FilenameUtils.removeExtension(sourceFile);

        extension = FilenameUtils.getExtension(htmlFileName);

        if (FileExtensions.HTML_EXTENSIONS.contains(extension.toLowerCase())) {

            return htmlFileName;
        }

        if (!Strings.isNullOrEmpty(extension)) {

            htmlFileName = FilenameUtils.removeExtension(htmlFileName);
        }

        return htmlFileName + "." + FileExtensions.HTML;
    }

}
