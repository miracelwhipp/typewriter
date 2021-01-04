package io.github.miracelwhipp.typewriter.conversion.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public final class Configurations {

    private Configurations() {
    }

    public static Configuration makeConfiguration() {

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_29);

        configuration.setIncompatibleImprovements(Configuration.VERSION_2_3_29);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);

        return configuration;
    }
}
