package io.github.miracelwhipp.typewriter;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

public abstract class AbstractTypewriterMojo extends AbstractMojo {


    private static final String DEFAULT_SOURCE_ENCODING = "${project.build.sourceEncoding}";

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/main/md", property = "typewriter.processed.markdown.directory")
    protected File markdownDirectory;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/main/html", property = "typewriter.processed.html.directory")
    protected File htmlDirectory;

    @Parameter(defaultValue = "${project.build.directory}/pdf", property = "typewriter.pdf.directory")
    protected File pdfDirectory;

    @Parameter(defaultValue = "main.md.ftl", property = "typewriter.source.file")
    protected String sourceFile;

    @Parameter(readonly = true, defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(defaultValue = DEFAULT_SOURCE_ENCODING, property = "typewriter.source.encoding")
    private String sourceEncoding;

    @Parameter(defaultValue = "default.css", property = "typewriter.default.css.file")
    protected String defaultCssFile;

    public String getSourceEncoding() {

        if (StringUtils.isBlank(sourceEncoding) || sourceEncoding.equals(DEFAULT_SOURCE_ENCODING)) {

            return "UTF-8";
        }

        return sourceEncoding;
    }

    public Charset getSourceCharset() {

        return Charset.forName(getSourceEncoding());
    }

    protected Path getFontPath() {

        if (SystemUtils.IS_OS_WINDOWS) {

            return new File("c:\\Windows\\Fonts").toPath();
        }

        return new File("/usr/share/fonts/truetype/").toPath();
    }
}
