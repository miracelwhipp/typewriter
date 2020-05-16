package io.github.miracelwhipp.typewriter.library;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.ElementConverter;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StyleMonitor implements Monitor {

    private final List<String> styles = new ArrayList<>();

    @Override
    public Object newSensor() {
        return new TemplateDirectiveModel() {

            @Override
            public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

                try (StringWriter writer = new StringWriter()) {

                    body.render(writer);

                    styles.add(writer.toString());
                }
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

                styles.forEach(style -> {

                    Element styleElement = document.createElement("style");

                    styleElement.setTextContent(style);

                    node.appendChild(styleElement);
                });

                return node;
            }
        };
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "style";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new StyleMonitor();
        }
    }
}
