package io.github.miracelwhipp.typewriter.library;

import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.ElementConverter;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BibliographyMonitor implements Monitor {

    private final List<String> sources = new ArrayList<>();

    @Override
    public Object newSensor() {
        return new BibliographySensor();
    }

    @Override
    public ElementConverter newEvaluator() {
        return new BibliographyEvaluator();
    }

    public class BibliographySensor {

        public String source(String source) {

            sources.add(source);

            return "<sup class=\"typewriter-source-reference\">[" + sources.size() + "]</sup>";
        }
    }

    public class BibliographyEvaluator implements ElementConverter {

        @Override
        public List<String> elementNamesToConvert() {
            return Collections.singletonList("bibliography");
        }

        @Override
        public Element convert(Element node) {

            Document document = node.getOwnerDocument();
            Element list = document.createElement("ol");
            list.setAttribute("class", "typewriter-bibliography");

            sources.forEach(source -> {

                Element listIndex = document.createElement("li");
                listIndex.setTextContent(source);

                list.appendChild(listIndex);
            });


            return list;
        }
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "bibliography";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new BibliographyMonitor();
        }
    }

}
