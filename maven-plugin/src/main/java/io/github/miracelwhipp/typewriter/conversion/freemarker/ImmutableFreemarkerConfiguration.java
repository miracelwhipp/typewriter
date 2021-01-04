package io.github.miracelwhipp.typewriter.conversion.freemarker;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
import java.util.Map;

public class ImmutableFreemarkerConfiguration implements FreemarkerConfiguration {

    private final Map<String, String> customConfiguration;
    private final PlexusConfiguration dataModel;
    private final File freemarkerIncludeDirectory;

    public ImmutableFreemarkerConfiguration(Map<String, String> customConfiguration, PlexusConfiguration dataModel, File freemarkerIncludeDirectory) {
        this.customConfiguration = customConfiguration;
        this.dataModel = dataModel;
        this.freemarkerIncludeDirectory = freemarkerIncludeDirectory;
    }

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
}
