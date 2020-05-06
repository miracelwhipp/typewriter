package io.github.miracelwhipp.typewriter.spi;

import java.util.Map;

public abstract class SingletonDataModelProvider implements DataModelProvider {

    private Object instance;

    protected abstract Object makeDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters);

    @Override
    public Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {

        if (instance != null) {

            return instance;
        }

        return instance = makeDataModel(customConfiguration, evaluationParameters);
    }
}
