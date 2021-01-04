package io.github.miracelwhipp.typewriter;

import com.google.common.base.Strings;
import io.github.miracelwhipp.typewriter.conversion.System;
import io.github.miracelwhipp.typewriter.conversion.freemarker.FreemarkerConfiguration;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractTypewriterMojo extends AbstractMojo implements FreemarkerConfiguration, System {


    public static final String DEFAULT_SOURCE_DIRECTORY = "${project.basedir}/src/main/typewriter";

    private static final String DEFAULT_SOURCE_ENCODING = "${project.build.sourceEncoding}";

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/main/freemarker-include", property = "typewriter.freemarker.include.directory")
    private File freemarkerIncludeDirectory;

    @Parameter
    private PlexusConfiguration dataModel;

    @Parameter
    private Map<String, String> customConfiguration = new HashMap<>();

    @Parameter(property = "typewriter.locale")
    private String locale;

    @Parameter(defaultValue = DEFAULT_SOURCE_ENCODING, property = "typewriter.source.encoding")
    private String sourceEncoding;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "false", property = "typewriter.debug")
    protected boolean debug;

    @Override
    public File freemarkerIncludeDirectory() {
        return freemarkerIncludeDirectory;
    }

    @Override
    public PlexusConfiguration dataModel() {
        return dataModel;
    }

    @Override
    public Map<String, String> customConfiguration() {
        return customConfiguration;
    }

    @Override
    public Locale getLocale() {

        if (Strings.isNullOrEmpty(locale)) {

            return Locale.getDefault();
        }

        return Locale.forLanguageTag(locale);
    }

    @Override
    public Charset getSourceEncoding() {

        if (StringUtils.isBlank(sourceEncoding) || sourceEncoding.equals(DEFAULT_SOURCE_ENCODING)) {

            return StandardCharsets.UTF_8;
        }

        return Charset.forName(sourceEncoding);
    }

    @Override
    public MavenProject getProject() {
        return project;
    }
}
