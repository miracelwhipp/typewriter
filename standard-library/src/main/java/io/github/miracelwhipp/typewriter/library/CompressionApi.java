package io.github.miracelwhipp.typewriter.library;

import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.util.ModifiedFiles;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

public class CompressionApi {

    private final Path tempDirectory;
    private final Path preprocessingTarget;

    public CompressionApi(Path tempDirectory, Path preprocessingTarget) {
        this.tempDirectory = tempDirectory;
        this.preprocessingTarget = preprocessingTarget;
    }

    public String unzip(String uri, String selector) throws IOException {

        URL url = new URL(uri);

        String fileName = new File(url.getPath()).getName();

        Path outputDirectory = tempDirectory.resolve(fileName);

        FileUtils.forceMkdir(outputDirectory.toFile());

        Path targetFile = outputDirectory.resolve(selector);

        if (!targetFile.toFile().isDirectory()) {

            boolean found = false;

            try (ZipArchiveInputStream source = new ZipArchiveInputStream(new BufferedInputStream(url.openStream()))) {

                for (
                        ZipArchiveEntry entry = source.getNextZipEntry();
                        entry != null;
                        entry = source.getNextZipEntry()
                ) {

                    if (entry.isDirectory()) {

                        continue;
                    }

                    if (entry.getName().equals(selector)) {

                        found = true;
                    }

                    File unpackedFile = outputDirectory.resolve(entry.getName()).toFile();

                    FileUtils.forceMkdir(unpackedFile.getParentFile());

                    try (OutputStream target = new FileOutputStream(unpackedFile)) {

                        IOUtils.copy(source, target);
                    }
                }
            }

            if (!found) {

                throw new IllegalArgumentException("selector " + selector + " not found in " + outputDirectory.toString());
            }
        }

        return preprocessingTarget.getParent().toAbsolutePath().relativize(targetFile.toAbsolutePath()).toString().replaceAll("\\\\", "/");
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "compression";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new CompressionApi(
                    new File(customConfiguration.getOrDefault("tempDirectory", "target/temp")).toPath(),
                    evaluationParameters.getTargetFile()
            );
        }
    }
}
