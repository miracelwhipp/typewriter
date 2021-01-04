package io.github.miracelwhipp.typewriter;

import io.github.miracelwhipp.typewriter.conversion.ConversionDescription;
import io.github.miracelwhipp.typewriter.conversion.ConversionSystem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class SingleFileConversionMojo extends AbstractTypewriterMojo {

    @Parameter(defaultValue = "${project.build.directory}", property = "typewriter.target.directory")
    protected File pdfDirectory;

    private final String targetType;

    public SingleFileConversionMojo(String targetType) {
        this.targetType = targetType;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ConversionSystem conversionSystem = ConversionSystem.defaultConversions(debug, this, this);

        File sourceFile = determineSourceFile();

        conversionSystem.convert(ConversionDescription.of(targetType), sourceFile, pdfDirectory);

        if (!sourceFile.isDirectory()) {

            String plainName = sourceFile.getName().split("\\.")[0];

            File targetFile = new File(pdfDirectory, plainName + "." + targetType);

            getProject().getArtifact().setFile(targetFile);
        }
    }

    private File determineSourceFile() throws MojoFailureException {


        File configuredSourceFile = getSourceFile();

        if (configuredSourceFile.exists()) {

            return configuredSourceFile;
        }

        File sourceDirectory = configuredSourceFile.getParentFile();

        if (!sourceDirectory.isDirectory()) {

            throw new MojoFailureException("source file " + configuredSourceFile + " not found");
        }

        File[] files = sourceDirectory.listFiles();

        if (files.length != 1) {

            throw new MojoFailureException("source file " + configuredSourceFile + " not found");
        }

        return files[0];
    }

    protected abstract File getSourceFile();
}
