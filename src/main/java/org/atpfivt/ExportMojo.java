package org.atpfivt;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Goal which exports the project into zip file respecting .gitignore.
 */
@Mojo(name = "export", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public final class ExportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File baseDirectory;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDirectory;

    @Parameter(defaultValue = "export.zip")
    private String zipFileName;

    void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    void setBuildDirectory(File buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public void execute()
            throws MojoExecutionException {
        File zipFile = new File(buildDirectory, zipFileName);
        try {
            Files.createDirectories(buildDirectory.toPath());
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                Path basePath = baseDirectory.toPath();
                PathsCollector collector = new PathsCollector(basePath);
                List<Path> pathList = collector.getFiles();
                for (Path path : pathList) {
                    Path relativePath = basePath.relativize(path);
                    zos.putNextEntry(new ZipEntry(relativePath.toString()));
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating ZIP file", e);
        }
        getLog().info("Created ZIP file at " + zipFile.getAbsolutePath());
    }
}
