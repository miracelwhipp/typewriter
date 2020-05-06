package io.github.miracelwhipp.typewriter.freemarker;

import io.github.miracelwhipp.typewriter.spi.DataModelProvider;

import java.util.*;

public class DataModelProviders {

    private final Set<String> builtInDataModels;
    private List<DataModelProvider> dataModelProviders = null;

    public DataModelProviders(Set<String> builtInDataModels) {
        this.builtInDataModels = builtInDataModels;
    }

    public List<DataModelProvider> getDataModelProviders() {

        if (dataModelProviders != null) {

            return dataModelProviders;
        }

        ServiceLoader<DataModelProvider> loader = ServiceLoader.load(DataModelProvider.class);

        Map<String, Class<? extends DataModelProvider>> providers = new HashMap<>();

        ArrayList<DataModelProvider> result = new ArrayList<>();

        loader.forEach(dataModelProvider -> {

            if (builtInDataModels.contains(dataModelProvider.name())) {

                throw new IllegalStateException("data model name clashes with builtin data model " + dataModelProvider.name() + " class: " + dataModelProvider.getClass().getCanonicalName());
            }

            Class<? extends DataModelProvider> oldClass =
                    providers.put(dataModelProvider.name(), dataModelProvider.getClass());

            if (oldClass != null) {

                throw new IllegalStateException("double declaration in data model for key " + dataModelProvider.name() + " " + oldClass.getCanonicalName() + " and " + dataModelProvider.getClass().getCanonicalName());
            }

            result.add(dataModelProvider);
        });

        return dataModelProviders = result;
    }
}
