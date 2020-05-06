package io.github.miracelwhipp.typewriter.spi.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

@FunctionalInterface
public interface ForEachFile extends FileVisitor<Path> {

    static void foreach(Path directory, ForEachFile action) throws IOException {

        Files.walkFileTree(directory, action);
    }

    void visit(Path file, BasicFileAttributes attributes) throws IOException;

    @Override
    default FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    @Override
    default FileVisitResult visitFile(
            Path file, BasicFileAttributes attributes) throws IOException {

        Objects.requireNonNull(file);
        Objects.requireNonNull(attributes);

        visit(file, attributes);

        return FileVisitResult.CONTINUE;
    }


    @Override
    default FileVisitResult visitFileFailed(
            Path file, IOException exc) throws IOException {

        Objects.requireNonNull(file);
        throw exc;
    }

    @Override
    default FileVisitResult postVisitDirectory(
            Path dir, IOException exc) throws IOException {

        Objects.requireNonNull(dir);

        if (exc != null) {
            throw exc;
        }

        return FileVisitResult.CONTINUE;
    }
}
