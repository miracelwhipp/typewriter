package io.github.miracelwhipp.typewriter;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.github.miracelwhipp.typewriter.spi.CssProvider;
import io.github.miracelwhipp.typewriter.spi.util.ModifiedFiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    MacroExtension.create()
            ));

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

                        html.append(HTML_CLOSE_HEAD);

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
    private static final String HTML_CLOSE_HEAD = "</head>\n<body>\n";
    private static final String HTML_FOOTER = "\n</body></html>";
    private static final String HTML_LINK_PREFIX = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
    private static final String HTML_LINK_SUFFIX = "\"/>\n";

}
