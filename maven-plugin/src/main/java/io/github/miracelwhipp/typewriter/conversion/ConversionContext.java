package io.github.miracelwhipp.typewriter.conversion;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ConversionContext implements Iterable<Object> {

    private final List<Object> elements = new ArrayList<>();
    @NotNull
    private final System system;
    @NotNull
    private final File convertedFile;
    @NotNull
    private final File targetFile;


    public ConversionContext(@NotNull System system, @NotNull File convertedFile, @NotNull File targetFile) {
        this.system = system;
        this.convertedFile = convertedFile;
        this.targetFile = targetFile;
    }

    public void add(@NotNull Object object) {

        elements.add(object);
    }

    @NotNull
    public Stream<Object> stream() {

        return elements.stream();
    }

    @NotNull
    public <Type> Stream<Type> allOf(Class<Type> clazz) {

        return stream().filter(clazz::isInstance).map(clazz::cast);
    }

    @NotNull
    @Override
    public Iterator<Object> iterator() {

        return elements.iterator();
    }

    @NotNull
    public System getSystem() {
        return system;
    }

    @NotNull
    public File getConvertedFile() {
        return convertedFile;
    }

    public @NotNull File getTargetFile() {
        return targetFile;
    }
}
