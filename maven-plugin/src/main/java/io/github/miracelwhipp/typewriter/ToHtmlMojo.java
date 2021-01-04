package io.github.miracelwhipp.typewriter;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "html", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ToHtmlMojo extends SingleFileConversionMojo {

    @Parameter(defaultValue = DEFAULT_SOURCE_DIRECTORY + "/main.md.ftl", property = "typewriter.source.file")
    protected File sourceFile;


    public ToHtmlMojo() {
        super(FileExtensions.HTML);
    }

    @Override
    protected File getSourceFile() {
        return sourceFile;
    }
}
