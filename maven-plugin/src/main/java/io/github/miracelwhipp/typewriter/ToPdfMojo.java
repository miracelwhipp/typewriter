package io.github.miracelwhipp.typewriter;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "pdf", defaultPhase = LifecyclePhase.COMPILE)
public class ToPdfMojo extends SingleFileConversionMojo {

    @Parameter(defaultValue = DEFAULT_SOURCE_DIRECTORY + "/main.md.ftl", property = "typewriter.source.file")
    protected File sourceFile;

    public ToPdfMojo() {
        super(FileExtensions.PDF);
    }

    @Override
    protected File getSourceFile() {
        return sourceFile;
    }
}
