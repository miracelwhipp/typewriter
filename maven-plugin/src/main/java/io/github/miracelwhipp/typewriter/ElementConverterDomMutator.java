package io.github.miracelwhipp.typewriter;

import com.openhtmltopdf.extend.FSDOMMutator;
import io.github.miracelwhipp.typewriter.spi.ElementConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class ElementConverterDomMutator implements FSDOMMutator {

    private final ElementConverter converter;

    public ElementConverterDomMutator(ElementConverter converter) {
        this.converter = converter;
    }

    @Override
    public void mutateDocument(Document document) {

        List<String> elementNames = converter.elementNamesToConvert();

        elementNames.forEach(elementName -> {

            NodeList elements = document.getElementsByTagName(elementName);

            for (int index = 0; index < elements.getLength(); index++) {

                Element element = (Element) elements.item(index);

                Element converted = converter.convert(element);

                if (converted == element) {

                    continue;
                }

                if (converted == null) {

                    element.getParentNode().removeChild(element);

                } else {

                    element.getParentNode().replaceChild(converted, element);
                }
            }
        });
    }
}