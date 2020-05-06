package io.github.miracelwhipp.typewriter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class FileExtensions {

    public static final String HTML = "html";
    public static final String XHTML = "xhtml";
    public static final String HTM = "htm";
    public static final String CSS = "css";
    public static final String MARKDOWN = "md";
    public static final String PDF = "pdf";
    public static final String FREEMARKER = "ftl";

    public static final Set<String> HTML_EXTENSIONS = new HashSet<>(Arrays.asList(
            HTML,
            XHTML,
            HTM
    ));

    public static final Set<String> PDF_SOURCE_TYPES = new HashSet<>(Arrays.asList(
            HTML,
            XHTML,
            HTM,
            MARKDOWN
    ));

    public static final Set<String> PREPROCESSED_FILE_TYPES = new HashSet<>(Arrays.asList(
            FREEMARKER
    ));

    private FileExtensions() {

    }
}
