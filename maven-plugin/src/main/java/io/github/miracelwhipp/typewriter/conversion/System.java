package io.github.miracelwhipp.typewriter.conversion;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.nio.charset.Charset;
import java.util.Locale;

public interface System {

    Log getLog();

    Locale getLocale();

    Charset getSourceEncoding();

    MavenProject getProject();
}
