package org.atpfivt;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportMojoTest {
    private ExportMojo exportMojo;
    private Path root;

    private Path target;

    @BeforeEach
    void setup() throws IOException {
        root = Files.createTempDirectory("mojotest");
        target = Files.createDirectory(root.resolve("target"));
        exportMojo = new ExportMojo();
        exportMojo.setBaseDirectory(root.toFile());
        exportMojo.setBuildDirectory(target.toFile());
        exportMojo.setZipFileName("export.zip");
        Files.write(root.resolve(".gitignore"),
                List.of("#gitignore test", "*.ignoreme", "target/"));
        Files.writeString(root.resolve("a"), "test");
        Files.writeString(root.resolve("b"), "test");
        Files.writeString(root.resolve("a.ignoreme"), "test");
    }

    @Test
    void exportSavesFilesRespectingTheGitignore() throws MojoExecutionException, IOException {
        exportMojo.execute();
        Set<String> filenames = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(target.resolve("export.zip").toFile()))) {
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                filenames.add(nextEntry.getName());
            }
        }
        assertEquals(Set.of("a", "b", ".gitignore"), filenames);

    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(root)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}