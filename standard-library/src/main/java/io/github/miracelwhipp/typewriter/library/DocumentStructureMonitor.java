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


    private final Tree<String> root = new Tree<>("<root>");

    private final Stack<Tree<String>> sections = new Stack<Tree<String>>() {
        {
            push(root);
        }
    };

    private Tree<String> sectionTree;

    @Override
    public Object newSensor() {
        return new DocumentStructureSensor();
    }

    @Override
    public ElementConverter newEvaluator() {
        return new DocumentStructureEvaluator();
    }

    public Tree<String> getSectionTree() {
        return sectionTree;
    }


    public class DocumentStructureSensor {

        public void enterSection(String title) {

            Tree<String> newSection = new Tree<>(title);

            if (sections.empty()) {

                sections.push(newSection);
                return;
            }

            Tree<String> currentSection = sections.peek();
            currentSection.getChildren().add(newSection);
            sections.push(newSection);
        }

        public void leaveSection() {

            if (sections.empty()) {

                return;
            }

            sectionTree = sections.pop();
        }

        public int sectionDepth() {

            return sections.size();
        }
    }


    public class DocumentStructureEvaluator implements ElementConverter {

        @Override
        public List<String> elementNamesToConvert() {
            return Collections.singletonList("table-of-contents");
        }

        @Override
        public Element convert(Element node) {

//            Tree<String> sectionTree = getSectionTree();

            Document document = node.getOwnerDocument();

            Element container = document.createElement("div");
            container.setAttribute("class", "typewriter-table-of-contents");

//            if (sectionTree == null) {
//
//                return container;
//            }

            Element list = document.createElement("ol");
            container.appendChild(list);

//            if (sectionTree != root) {
//
//                throw new IllegalStateException("creating table of contents when document is not fully preprocessed");
//            }

            root.getChildren().forEach(child -> {

                list.appendChild(convert(document, child));
            });

            return container;
        }

        private Element convert(Document document, Tree<String> tree) {

            Element listIndex = document.createElement("li");
            listIndex.setTextContent(tree.getData());

            Element subList = document.createElement("ol");
            listIndex.appendChild(subList);

            tree.getChildren().forEach(child -> {

                subList.appendChild(convert(document, child));
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
