package io.github.miracelwhipp.typewriter;

import io.github.miracelwhipp.typewriter.conversion.ConversionDescription;
import io.github.miracelwhipp.typewriter.conversion.ConversionSystem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "convert", defaultPhase = LifecyclePhase.COMPILE)
public class ConversionMojo extends AbstractTypewriterMojo {

    @Parameter
    private List<String> conversions = new ArrayList<>();

    @Parameter(defaultValue = DEFAULT_SOURCE_DIRECTORY, property = "typewriter.source.directory")
    private File conversionSource;

    @Parameter(defaultValue = "${project.build.directory}/typewriter", property = "typewriter.target.directory")
    private File conversionTargetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ConversionSystem conversionSystem = ConversionSystem.defaultConversions(debug, this, this);

        conversionSystem.convert(ConversionDescription.of(conversions), conversionSource, conversionTargetDirectory);
    }
}
