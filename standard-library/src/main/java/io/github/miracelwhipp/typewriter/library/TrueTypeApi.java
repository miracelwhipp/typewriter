package io.github.miracelwhipp.typewriter.library;

import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.mabb.fontverter.FVFont;
import org.mabb.fontverter.FontVerter;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.DataFormatException;

public class TrueTypeApi {

    private final File fontDirectory;
    private final Path targetFile;

    public TrueTypeApi(File fontDirectory, Path targetFile) {
        this.fontDirectory = fontDirectory;
        this.targetFile = targetFile;
    }

    public String fromWoff(String url) throws IOException, DataFormatException {

        URL targetUrl = new URL(url);

        final String fileName = FilenameUtils.getName(targetUrl.getPath());
        final File localFile = new File(fontDirectory, FilenameUtils.removeExtension(fileName) + ".ttf");

        Path relativePath = targetFile.getParent().relativize(localFile.toPath());

        if (localFile.isFile()) {

            return buildResultString(relativePath);
        }

        FileUtils.forceMkdir(fontDirectory);

        try (
                InputStream source = targetUrl.openStream();
                OutputStream target = new FileOutputStream(localFile);
                ByteArrayOutputStream memory = new ByteArrayOutputStream()
        ) {
            IOUtils.copy(source, memory);

            final FVFont font = FontVerter.convertFont(memory.toByteArray(), FontVerter.FontFormat.TTF);

            IOUtils.write(font.getData(), target);


            return buildResultString(relativePath);
        }
    }

    @NotNull
    private String buildResultString(Path relativePath) {
        return "url(" + relativePath.toString().replaceAll("\\\\", "/") + ") format(truetype)";
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "trueType";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new TrueTypeApi(
                    new File(customConfiguration.getOrDefault("tempDirectory", "target/temp")),
                    evaluationParameters.getTargetFile()
            );
        }
    }

}
