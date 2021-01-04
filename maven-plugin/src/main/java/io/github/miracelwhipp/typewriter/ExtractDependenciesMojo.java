package io.github.miracelwhipp.typewriter;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mojo(name = "extract-twa", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ExtractDependenciesMojo extends AbstractTypewriterMojo {

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    @Component
    private ArchiverManager archiverManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            DefaultDependencyResolutionRequest request = new DefaultDependencyResolutionRequest();

            request.setMavenProject(getProject());

            request.setRepositorySession(repoSession);

            request.setResolutionFilter((dependencyNode, list) -> {

                Dependency dependency = dependencyNode.getDependency();

                if (dependency == null) {

                    return false;
                }

                Artifact artifact = dependencyNode.getArtifact();

                String extension = artifact.getExtension();

                return extension.equalsIgnoreCase("twa");
            });

            DependencyResolutionResult result = projectDependenciesResolver.resolve(request);

            List<Dependency> dependencies = result.getResolvedDependencies();

            UnArchiver unArchiver = getUnArchiver();

            if (unArchiver instanceof ZipUnArchiver) {
                ((ZipUnArchiver) unArchiver).setEncoding(StandardCharsets.UTF_8.name());
//                getLog().info( "Unpacks '" + type + "' with encoding '" + encoding + "'." );
            }

            dependencies.forEach(dependency -> {


                try {

                    FileUtils.forceMkdir(freemarkerIncludeDirectory());

                } catch (IOException e) {

                    throw new WrappedCheckedException(e);
                }

                unArchiver.setDestDirectory(freemarkerIncludeDirectory());
                unArchiver.setSourceFile(dependency.getArtifact().getFile().getAbsoluteFile());
                unArchiver.setFileSelectors(new FileSelector[]{new FileSelector() {

                    @Override
                    public boolean isSelected(@Nonnull FileInfo fileInfo) throws IOException {
                        return !fileInfo.isSymbolicLink() && !fileInfo.getName().startsWith("META-INF");
                    }
                }});
                unArchiver.extract();
            });

        } catch (DependencyResolutionException e) {

            throw new MojoExecutionException(e.getMessage(), e);

        } catch (WrappedCheckedException e) {

            throw new MojoExecutionException(e.getCause().getMessage(), e.getCause());
        }
    }

    private UnArchiver getUnArchiver() {

        try {

            return archiverManager.getUnArchiver("jar");

        } catch (NoSuchArchiverException e) {

            try {

                return archiverManager.getUnArchiver(new File("some.jar"));

            } catch (NoSuchArchiverException noSuchArchiverException) {

                throw new WrappedCheckedException(noSuchArchiverException);
            }
        }
    }
}
