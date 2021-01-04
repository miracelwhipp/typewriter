package io.github.miracelwhipp.typewriter.library;

public class SectionDescriptor {

    private final String title;
    private final String identifier;

    public SectionDescriptor(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public String getIdentifier() {
        return identifier;
    }
}
