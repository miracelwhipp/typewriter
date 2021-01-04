package io.github.miracelwhipp.typewriter.conversion.freemarker;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
import java.util.Map;

public interface FreemarkerConfiguration {

    File freemarkerIncludeDirectory();

    PlexusConfiguration dataModel();

    Map<String, String> customConfiguration();

}
