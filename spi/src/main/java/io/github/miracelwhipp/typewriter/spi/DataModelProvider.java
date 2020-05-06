package io.github.miracelwhipp.typewriter.spi;

import java.util.Map;

public interface DataModelProvider {

    String name();

    Object getDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters);
}
