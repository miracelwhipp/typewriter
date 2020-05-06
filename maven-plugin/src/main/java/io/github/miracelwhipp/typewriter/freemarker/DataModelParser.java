package io.github.miracelwhipp.typewriter.freemarker;

import freemarker.core.CollectionAndSequence;
import freemarker.template.*;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class DataModelParser {

    private DataModelParser() {
    }

    public static TemplateModel parseTemplateModel(PlexusConfiguration dataModel) {

        if (dataModel.getValue() != null) {

            return new SimpleScalar(dataModel.getValue());
        }

        Map<String, TemplateModel> childModels = new HashMap<>();

        for (PlexusConfiguration child : dataModel.getChildren()) {

            childModels.put(child.getName(), parseTemplateModel(child));
        }

        return new NodeDataModel(childModels);
    }

    public static class NodeDataModel implements TemplateHashModelEx {

        private final Map<String, TemplateModel> childModels;

        public NodeDataModel(Map<String, TemplateModel> childModels) {
            this.childModels = childModels;
        }

        @Override
        public int size() throws TemplateModelException {
            return childModels.size();
        }

        @Override
        public TemplateCollectionModel keys() throws TemplateModelException {

            ArrayList<String> keys = new ArrayList<>(childModels.keySet());

            return new CollectionAndSequence(new TemplateSequenceModel() {
                @Override
                public TemplateModel get(int index) throws TemplateModelException {
                    return new SimpleScalar(keys.get(index));
                }

                @Override
                public int size() throws TemplateModelException {
                    return keys.size();
                }
            });
        }

        @Override
        public TemplateCollectionModel values() throws TemplateModelException {

            ArrayList<TemplateModel> values = new ArrayList<>(childModels.values());

            return new CollectionAndSequence(new TemplateSequenceModel() {
                @Override
                public TemplateModel get(int index) throws TemplateModelException {
                    return values.get(index);
                }

                @Override
                public int size() throws TemplateModelException {
                    return values.size();
                }
            });
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            return childModels.get(key);
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return childModels.isEmpty();
        }
    }
}
