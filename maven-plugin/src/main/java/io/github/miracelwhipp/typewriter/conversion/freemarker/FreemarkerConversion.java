package io.github.miracelwhipp.typewriter.conversion.freemarker;

import freemarker.cache.ByteArrayTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.miracelwhipp.typewriter.conversion.Conversion;
import io.github.miracelwhipp.typewriter.conversion.ConversionContext;
import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class FreemarkerConversion implements Conversion {

    public static final String CURRENT_TEMPLATE_MARKER = "<current>";

    private final FreemarkerConfiguration freemarkerConfiguration;
    public static final Pattern FTL = Pattern.compile("ftl");
    public static final Pattern ALL = Pattern.compile(".*");

    public FreemarkerConversion(FreemarkerConfiguration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }


    @Override
    public Pattern sourceExtension() {
        return FTL;
    }

    @Override
    public Pattern targetExtension() {
        return ALL;
    }

    @Override
    public void convert(InputStream source, OutputStream target, ConversionContext context) {

        try {
            Configuration configuration = Configurations.makeConfiguration();

            ByteArrayTemplateLoader inputStreamLoader = new ByteArrayTemplateLoader();
            inputStreamLoader.putTemplate(CURRENT_TEMPLATE_MARKER, IOUtils.toByteArray(source));

            TemplateLoader templateLoader = TemplateLoaders.withLog(context.getSystem().getLog())
                    .addSourceDirectory(context.getConvertedFile().getParentFile())
                    .addSourceDirectory(freemarkerConfiguration.freemarkerIncludeDirectory())
                    .addTemplateLoader(inputStreamLoader)
                    .build();

            configuration.setTemplateLoader(templateLoader);

            Locale currentLocale = context.getSystem().getLocale();
            configuration.setLocale(currentLocale);

            configuration.setDefaultEncoding(context.getSystem().getSourceEncoding().name());

            MavenProject project = context.getSystem().getProject();

            // TODO: file system api, http api, woff2 to ttf api
            DataModel dataModel = DataModel.builtIn(project, freemarkerConfiguration.dataModel());

            DataModelProviders dataModelProviders = new DataModelProviders(dataModel.getData().keySet());

            Map<String, Object> input = new HashMap<>(dataModel.getData());

            project.getProperties().forEach((key, value) -> {
                input.put(key.toString(), value.toString());
            });

            evaluateTemplate(context, configuration, dataModelProviders, input, target);

        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
    }

    private void evaluateTemplate(
            ConversionContext context,
            Configuration configuration,
            DataModelProviders providers,
            Map<String, Object> input,
            OutputStream target
    ) throws IOException {

        context.getSystem().getLog().debug("freemarker preprocessing in chain " + context.getConvertedFile() + " to " + context.getTargetFile());

        EvaluationParameters evaluationParameters = new EvaluationParameters(context.getConvertedFile().toPath(), context.getTargetFile().toPath());

        List<DataModelProvider> dataModelProviders = providers.getDataModelProviders();

        for (DataModelProvider provider : dataModelProviders) {

            Object model = provider.getDataModel(freemarkerConfiguration.customConfiguration(), evaluationParameters);

            context.getSystem().getLog().info("adding model " + model.getClass().getCanonicalName() + " from provider " + provider.getClass().getCanonicalName());

            if (!(model instanceof Monitor)) {

                input.put(provider.name(), model);

            } else {

                Monitor monitor = (Monitor) model;

                context.add(monitor);

                input.put(provider.name(), monitor.newSensor());
            }
        }

        Template template = configuration.getTemplate(CURRENT_TEMPLATE_MARKER);

        try (Writer writer = new OutputStreamWriter(target)) {

            template.process(input, writer);

        } catch (TemplateException e) {

            throw new IOException(e.getMessage(), e);
        }
    }


}
