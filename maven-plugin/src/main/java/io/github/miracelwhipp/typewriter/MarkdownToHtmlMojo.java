package io.github.miracelwhipp.typewriter;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.github.miracelwhipp.typewriter.spi.util.ModifiedFiles;
import io.github.miracelwhipp.typewriter.spi.CssProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

@Mojo(name = "html", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MarkdownToHtmlMojo extends AbstractTypewriterMojo {

    private String defaultCss;

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            if (!markdownDirectory.isDirectory()) {

                getLog().info("skipping typewriter html from markdown - no sources found");

                return;
            }


            MutableDataSet options = new MutableDataSet();

            //TODO: check available options
            //uncomment to set optional extensions
            //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

            // uncomment to convert soft-breaks to hard breaks
            //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            Path markdownPath = markdownDirectory.toPath();
            Path targetPath = htmlDirectory.toPath();

            ModifiedFiles.foreach(
                    markdownPath,
                    targetPath,
                    FileExtensions.MARKDOWN,
                    FileExtensions.HTML,
                    (sourceFile, targetFile) -> {

                        String markdown = new String(
                                Files.readAllBytes(sourceFile), getSourceCharset());

                        Node document = parser.parse(markdown);


                        StringBuilder html = new StringBuilder();

                        html.append(HTML_HEADER);

                        String defaultStyle = collectCss();

                        html.append(defaultStyle);

                        File defaultCssFileName = new File(htmlDirectory, defaultCssFile);
                        getLog().debug("default css file is " + defaultCssFileName.getAbsolutePath());

                        if (defaultCssFileName.isFile()) {

                            Path relativePath = targetFile.getParent().relativize(defaultCssFileName.toPath());

                            appendStyleSheetLink(html, relativePath.toString());
                        }

                        File localDefault = new File(targetFile.getParent().toFile(), defaultCssFile);
                        getLog().debug("local default css file is " + localDefault.getAbsolutePath());

                        if (localDefault.isFile() && !localDefault.equals(defaultCssFileName)) {

                            appendStyleSheetLink(html, defaultCssFile);
                        }

                        File namedCssFile = new File(targetFile.getParent().toFile(),
                                FilenameUtils.removeExtension(sourceFile.toFile().getName()) + "." + FileExtensions.CSS);
                        getLog().debug("named css file is " + namedCssFile.getAbsolutePath());

                        if (namedCssFile.isFile()) {

                            appendStyleSheetLink(html, namedCssFile.getName());
                        }

                        html.append(HTML_OPEN_TITLE).append(FilenameUtils.removeExtension(sourceFile.toFile().getName())).append(HTML_CLOSE_HEAD);

                        html.append(renderer.render(document));

                        html.append(HTML_FOOTER);

                        Files.write(targetFile, html.toString().getBytes(getSourceCharset()));
                    });

        } catch (IOException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

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

    @NotNull
    private StringBuilder appendStyleSheetLink(StringBuilder html, String relativePath) {

        relativePath = relativePath.replaceAll("\\\\", "/");

        return html.append(HTML_LINK_PREFIX).append(relativePath).append(HTML_LINK_SUFFIX);
    }

    private static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n<html>\n<head>\n";
    private static final String HTML_OPEN_TITLE = "<title>";
    private static final String HTML_CLOSE_HEAD = "</title>\n</head>\n<body>\n";
    private static final String HTML_FOOTER = "\n</body></html>";
    private static final String HTML_LINK_PREFIX = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
    private static final String HTML_LINK_SUFFIX = "\"/>\n";

}
