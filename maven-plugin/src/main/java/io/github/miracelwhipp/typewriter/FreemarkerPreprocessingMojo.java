package io.github.miracelwhipp.typewriter;

import com.google.common.base.Strings;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.miracelwhipp.typewriter.freemarker.Configurations;
import io.github.miracelwhipp.typewriter.freemarker.DataModel;
import io.github.miracelwhipp.typewriter.freemarker.DataModelProviders;
import io.github.miracelwhipp.typewriter.spi.Monitor;
import io.github.miracelwhipp.typewriter.spi.util.ForEachFile;
import io.github.miracelwhipp.typewriter.spi.util.ModifiedFiles;
import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Mojo(name = "preprocess", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class FreemarkerPreprocessingMojo extends AbstractTypewriterMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/typewriter", property = "typewriter.source.directory")
    private File typewriterSourceDirectory;

    @Parameter
    private PlexusConfiguration dataModel;

    @Parameter
    private Map<String, String> customConfiguration = new HashMap<>();

    @Parameter(property = "typewriter.locale")
    private String locale;

    private List<DataModelProvider> dataModelProviders;

    private Set<String> builtInDataModels = new HashSet<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            if (!typewriterSourceDirectory.isDirectory()) {

                getLog().info("skipping typewriter preprocessing - no sources found");

                return;
            }

            Configuration configuration = Configurations.makeConfiguration();

            TemplateLoader templateLoader = TemplateLoaders.build(getLog(), typewriterSourceDirectory, freemarkerIncludeDirectory);
            configuration.setTemplateLoader(templateLoader);

            Locale currentLocale = getLocale();
            configuration.setLocale(currentLocale);

            configuration.setDefaultEncoding(getSourceEncoding());


            // TODO: file system api, http api, woff2 to ttf api
            DataModel dataModel = DataModel.builtIn(project, this.dataModel);

            DataModelProviders dataModelProviders = new DataModelProviders(dataModel.getData().keySet());

            Map<String, Object> input = new HashMap<>(dataModel.getData());

            project.getProperties().forEach((key, value) -> {
                input.put(key.toString(), value.toString());
            });

            preprocessSourceFile(dataModelProviders, new File(typewriterSourceDirectory, this.sourceFile), configuration, input);

            Path sourceDirectory = typewriterSourceDirectory.toPath();

            ForEachFile.foreach(sourceDirectory, (file, attributes) -> {

                String extension = FilenameUtils.getExtension(file.toFile().getName());

                if (FileExtensions.FREEMARKER.equalsIgnoreCase(extension)) {

                    preprocessSourceFile(dataModelProviders, file.toFile(), configuration, input);
                    return;
                }

                if (FileExtensions.PDF_SOURCE_TYPES.contains(extension.toLowerCase())) {

                    return;
                }

                File targetFile = ModifiedFiles.buildTargetFile(sourceDirectory, htmlDirectory.toPath(), file);

                ModifiedFiles.apply(file.toFile(), targetFile, (source, target) -> {

                    getLog().debug("copying file " + sourceFile + " to " + targetFile);
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                });
            });

        } catch (IOException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Locale getLocale() {

        if (Strings.isNullOrEmpty(locale)) {

            return Locale.getDefault();
        }

        return Locale.forLanguageTag(locale);
    }

    private void preprocessSourceFile(DataModelProviders dataModelProviders, File sourceFile, Configuration configuration, Map<String, Object> input) throws IOException {

        Path sourceDirectory = typewriterSourceDirectory.toPath();

        String targetFileName = getTargetFileName(sourceFile);

        if (Strings.isNullOrEmpty(FilenameUtils.getExtension(targetFileName))) {

            getLog().debug("ignore pure freemarker template " + sourceFile);

            return;
        }

        boolean isMarkdown = FilenameUtils.isExtension(targetFileName, FileExtensions.MARKDOWN);

        File targetFile = ModifiedFiles.buildTargetFile(
                sourceDirectory,
                (isMarkdown ? markdownDirectory : htmlDirectory).toPath(),
                sourceFile.toPath(), file -> targetFileName
        );

        ModifiedFiles.apply(sourceFile, targetFile, (source, target) -> {

            evaluateTemplate(dataModelProviders, configuration, input, sourceDirectory, source, target);
        });
    }

    @NotNull
    private String getTargetFileName(File sourceFile) {

        String targetFileName = sourceFile.getName();

        if (FilenameUtils.isExtension(targetFileName, FileExtensions.FREEMARKER)) {

            targetFileName = FilenameUtils.removeExtension(targetFileName);
        }

        return targetFileName;
    }

    private void evaluateTemplate(
            DataModelProviders providers, Configuration configuration,
            Map<String, Object> input,
            Path sourceDirectory,
            Path sourceFile,
            Path targetFile
    ) throws IOException {

        getLog().debug("preprocessing file " + sourceFile + " to " + targetFile);

        EvaluationParameters evaluationParameters = new EvaluationParameters(sourceFile, targetFile);

        List<DataModelProvider> dataModelProviders = providers.getDataModelProviders();

        for (DataModelProvider provider : dataModelProviders) {

            Object model = provider.getDataModel(customConfiguration, evaluationParameters);

            getLog().info("adding model " + model.getClass().getCanonicalName() + " from provider " + provider.getClass().getCanonicalName());

            if (!(model instanceof Monitor)) {

                input.put(provider.name(), model);

            } else {

                File registryKey = targetFile.toFile();

                if (FileExtensions.MARKDOWN.equalsIgnoreCase(FilenameUtils.getExtension(targetFile.toString()))) {

                    String htmlFilename = FilenameUtils.removeExtension(htmlDirectory.toPath().resolve(markdownDirectory.toPath().relativize(targetFile)).toString()) + "." + FileExtensions.HTML;
                    registryKey = new File(htmlFilename);
                }

                Monitor monitor = (Monitor) model;

                SensorRegistry.INSTANCE.register(registryKey, monitor);

                input.put(provider.name(), monitor.newSensor());
            }
        }

        Template template = configuration.getTemplate(sourceDirectory.relativize(sourceFile).toString());

        try (Writer writer = new FileWriter(targetFile.toFile())) {

            template.process(input, writer);

        } catch (TemplateException e) {

            throw new IOException(e.getMessage(), e);
        }
    }
}
