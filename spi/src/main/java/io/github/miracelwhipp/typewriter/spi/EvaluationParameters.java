package io.github.miracelwhipp.typewriter.spi;

import java.nio.file.Path;

public class EvaluationParameters {

    private final Path sourceFile;
    private final Path targetFile;

    public EvaluationParameters(Path sourceFile, Path targetFile) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    public Path getTargetFile() {
        return targetFile;
    }
}
