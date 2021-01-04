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
import java.util.Stack;

public class DocumentStructureMonitor implements Monitor {


    private final Tree<SectionDescriptor> root = new Tree<>(new SectionDescriptor("<root>", "section"));

    private final Stack<Tree<SectionDescriptor>> sections = new Stack<Tree<SectionDescriptor>>() {
        {
            push(root);
        }
    };

    @Override
    public Object newSensor() {
        return new DocumentStructureSensor();
    }

    @Override
    public ElementConverter newEvaluator() {
        return new DocumentStructureEvaluator();
    }

    public class DocumentStructureSensor {

        public void enterSection(String title) {


            if (sections.empty()) {

                sections.push(new Tree<>(new SectionDescriptor(title, "section")));
                return;
            }

            Tree<SectionDescriptor> currentSection = sections.peek();

            Tree<SectionDescriptor> newSection = new Tree<>(new SectionDescriptor(title, currentSection.getData().getIdentifier() + "." + (currentSection.getChildren().size() + 1)));

            currentSection.getChildren().add(newSection);
            sections.push(newSection);
        }

        public void leaveSection() {

            if (sections.empty()) {

                return;
            }

            sections.pop();
        }

        public int sectionDepth() {

            return sections.size();
        }

        public String currentId() {

            return sections.peek().getData().getIdentifier();
        }

    }


    public class DocumentStructureEvaluator implements ElementConverter {

        @Override
        public List<String> elementNamesToConvert() {
            return Collections.singletonList("table-of-contents");
        }

        @Override
        public Element convert(Element node) {

            boolean ordered = Boolean.parseBoolean(node.getAttribute("ordered"));

            Document document = node.getOwnerDocument();

            Element container = document.createElement("div");
            container.setAttribute("class", "typewriter-table-of-contents");

            Element list = document.createElement(ordered ? "ol" : "ul");
            container.appendChild(list);

            root.getChildren().forEach(child -> {

                list.appendChild(convert(ordered, document, child));
            });

            return container;
        }

        private Element convert(boolean ordered, Document document, Tree<SectionDescriptor> tree) {

            Element listIndex = document.createElement("li");
            Element link = document.createElement("a");

            link.setAttribute("href", "#" + tree.getData().getIdentifier());
            link.setTextContent(tree.getData().getTitle());
            listIndex.appendChild(link);

            Element subList = document.createElement(ordered ? "ol" : "ul");
            listIndex.appendChild(subList);

            tree.getChildren().forEach(child -> {

                subList.appendChild(convert(ordered, document, child));
            });

            return listIndex;
        }
    }

    public static class Provider implements DataModelProvider {

        @Override
        public String name() {
            return "structure";
        }

        @Override
        public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

            return new DocumentStructureMonitor();
        }
    }
}
