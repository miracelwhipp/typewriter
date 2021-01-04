package io.github.miracelwhipp.typewriter.conversion.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import io.github.miracelwhipp.typewriter.WrappedCheckedException;
import io.github.miracelwhipp.typewriter.spi.TypewriterTemplateProvider;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class TemplateLoaders {

    private TemplateLoaders() {
    }

    public static TemplateLoaderFactory withLog(Log log) {

        TemplateLoaderFactory result = new TemplateLoaderFactory(log);

        StringTemplateLoader additionalTemplates = new StringTemplateLoader();

        ServiceLoader<TypewriterTemplateProvider> loader = ServiceLoader.load(TypewriterTemplateProvider.class);

        Map<String, Class<? extends TypewriterTemplateProvider>> providers = new HashMap<>();

        loader.forEach(provider -> {

            String templateContent = provider.templateContent();

            if (templateContent == null) {

                log.warn("template content provider " + provider.getClass().getCanonicalName() + " provided null");
                return;
            }


            Class<? extends TypewriterTemplateProvider> old = providers.put(provider.templateName(), provider.getClass());

            if (old != null) {

                throw new IllegalStateException("trying to register different template providers for name " + provider.templateName() + " " + old.getCanonicalName() + " vs. " + provider.getClass().getCanonicalName());
            }

            additionalTemplates.putTemplate(provider.templateName(), templateContent);
        });


        result.addTemplateLoader(additionalTemplates);


        return result;
    }

    public static class TemplateLoaderFactory {

        private final Log log;
        private final List<TemplateLoader> templateLoaders = new ArrayList<>();

        public TemplateLoaderFactory(Log log) {
            this.log = log;
        }

        public TemplateLoaderFactory addSourceDirectory(File sourceDirectory) throws IOException {

            if (!sourceDirectory.isDirectory()) {

                return this;
            }

            templateLoaders.add(new FileTemplateLoader(sourceDirectory));

            return this;
        }

        public TemplateLoader build() {

            return new MultiTemplateLoader(templateLoaders.toArray(new TemplateLoader[0]));
        }



        public TemplateLoaderFactory addTemplateLoader(TemplateLoader loader) {

            templateLoaders.add(loader);

            return this;
        }
    }

    public static TemplateLoader build(Log log, File... sourceDirectories) throws IOException {

        TemplateLoaderFactory factory = withLog(log);

        List<TemplateLoader> templateLoaders = new ArrayList<>(2);

        Arrays.stream(sourceDirectories).forEach(sourceDirectory -> {

            try {

                factory.addSourceDirectory(sourceDirectory);

            } catch (IOException e) {

                throw new WrappedCheckedException(e);
            }
        });

        StringTemplateLoader additionalTemplates = new StringTemplateLoader();
        templateLoaders.add(additionalTemplates);

        ServiceLoader<TypewriterTemplateProvider> loader = ServiceLoader.load(TypewriterTemplateProvider.class);

        Map<String, Class<? extends TypewriterTemplateProvider>> providers = new HashMap<>();

        loader.forEach(provider -> {

            String templateContent = provider.templateContent();

            if (templateContent == null) {

                log.warn("template content provider " + provider.getClass().getCanonicalName() + " provided null");
                return;
            }


            Class<? extends TypewriterTemplateProvider> old = providers.put(provider.templateName(), provider.getClass());

            if (old != null) {

                throw new IllegalStateException("trying to register different template providers for name " + provider.templateName() + " " + old.getCanonicalName() + " vs. " + provider.getClass().getCanonicalName());
            }

            additionalTemplates.putTemplate(provider.templateName(), templateContent);
        });

        return new MultiTemplateLoader(templateLoaders.toArray(new TemplateLoader[0]));

    }
}
