package io.github.miracelwhipp.typewriter.spi;

import org.w3c.dom.Element;

import java.util.List;

public interface ElementConverter {

    List<String> elementNamesToConvert();

    Element convert(Element node);
}
