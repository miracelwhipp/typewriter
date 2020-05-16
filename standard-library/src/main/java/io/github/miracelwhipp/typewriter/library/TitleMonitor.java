package io.github.miracelwhipp.typewriter.library;

import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.ElementConverter;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TitleMonitor implements Monitor {

    private String title;

    @Override
    public Object newSensor() {

        return new Object() {

            public void title(String newTitle) {

                title = newTitle;
            }
        };
    }

    @Override
    public ElementConverter newEvaluator() {

        return new ElementConverter() {

            @Override
            public List<String> elementNamesToConvert() {

                return Collections.singletonList("head");
            }

            @Override
            public Element convert(Element node) {

                Document document = node.getOwnerDocument();

                Element titleElement = document.createElement("title");
                titleElement.setTextContent(title);

                node.appendChild(titleElement);

                return node;
            }
        };
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "title";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new TitleMonitor();
        }
    }
}
