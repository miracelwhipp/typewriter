package io.github.miracelwhipp.typewriter.spi.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ModifiedFiles {

    private ModifiedFiles() {
    }

    @FunctionalInterface
    public interface FileTargetConsumer {

        void apply(Path sourceFile, Path targetFile) throws IOException;
    }

    public static void apply(File sourceFile, File targetFile, FileTargetConsumer action) throws IOException {

        if (targetFile.exists() && targetFile.lastModified() > sourceFile.lastModified()) {

            return;
        }

        FileUtils.forceMkdir(targetFile.getParentFile());

        action.apply(sourceFile.toPath(), targetFile.toPath());
    }

    public static void foreach(
            Path sourceDirectory,
            Path targetDirectory,
            Set<String> sourceExtensions,
            FileTargetConsumer action
    ) throws IOException {

        foreach(
                sourceDirectory,
                targetDirectory,
                file -> {
                    String extension = FilenameUtils.getExtension(file.getFileName().toString());
                    return sourceExtensions.contains(extension);
                },
                computeTargetExtension(x -> x),
                action
        );

    }

    public static void foreach(
            Path sourceDirectory,
            Path targetDirectory,
            String sourceExtension,
            String targetExtension,
            FileTargetConsumer action
    ) throws IOException {

        foreach(
                sourceDirectory,
                targetDirectory,
                file -> FilenameUtils.isExtension(file.getFileName().toString(), sourceExtension),
                computeTargetExtension(x -> targetExtension),
                action
        );
    }

    public static Function<Path, String> computeTargetExtension(
            Function<String, String> computation) {

        return file -> {
            String localFileName = file.getFileName().toString();
            String targetExtension = computation.apply(FilenameUtils.getExtension(localFileName));
            return FilenameUtils.removeExtension(localFileName) + "." + targetExtension;
        };
    }

    public static void foreach(
            Path sourceDirectory,
            Path targetDirectory,
            Predicate<Path> filter,
            Function<Path, String> computeTargetFileName,
            FileTargetConsumer action
    ) throws IOException {

        ForEachFile.foreach(sourceDirectory, (file, attributes) -> {

            if (!filter.test(file)) {

                return;
            }

            File targetFile = buildTargetFile(sourceDirectory, targetDirectory, file, computeTargetFileName);

            apply(file.toFile(), targetFile, action);
        });
    }

    public static File buildTargetFile(
            Path sourceDirectory,
            Path targetDirectory,
            Path file
    ) {
        return buildTargetFile(sourceDirectory, targetDirectory, file, path -> path.toFile().getName());
    }

    public static File buildTargetFile(
            Path sourceDirectory,
            Path targetDirectory,
            Path file, Function<Path, String> computeTargetFileName
    ) {
        Path relativePath = sourceDirectory.relativize(file);

        File targetSubDirectory = targetDirectory.resolve(relativePath).getParent().toFile();

        String name = computeTargetFileName.apply(file);

        return new File(targetSubDirectory, name);
    }
}
