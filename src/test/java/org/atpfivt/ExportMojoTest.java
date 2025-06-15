package org.atpfivt;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    void exportSavesFilesRespectingTheGitignore() throws MojoExecutionException, IOException {
        Files.write(root.resolve(".gitignore"),
                List.of("#gitignore test", "*.ignoreme", "target/"));
        Files.writeString(root.resolve("a"), "test");
        Files.writeString(root.resolve("b"), "test");
        Files.writeString(root.resolve("a.ignoreme"), "test");
        Files.createDirectories(root.resolve("src"));
        Files.writeString(root.resolve("src").resolve("foo"), "test");
        exportMojo.execute();
        Set<String> filenames = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(target.resolve("export.zip").toFile()))) {
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                filenames.add(nextEntry.getName());
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    zipInputStream.transferTo(baos);
                    assertTrue(baos.toString(StandardCharsets.UTF_8).contains("test"));
                }
            }
        }
        assertEquals(Set.of("a", "b", ".gitignore", "src/foo"), filenames);
    }

    @Test
    void exportOmitsTargetEvenIfNoGitignoreProvided() throws MojoExecutionException, IOException {
        Files.writeString(root.resolve("a"), "test");
        Files.writeString(root.resolve("b"), "test");
        Files.writeString(root.resolve("a.ignoreme"), "test");
        Files.writeString(target.resolve("helloworld.class"), "cafebabe");
        exportMojo.execute();
        Set<String> filenames = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(target.resolve("export.zip").toFile()))) {
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                filenames.add(nextEntry.getName());
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    zipInputStream.transferTo(baos);
                    assertEquals("test", baos.toString(StandardCharsets.UTF_8));
                }
            }
        }
        assertEquals(Set.of("a", "b", "a.ignoreme"), filenames);
    }


    @Test
    void exportStripsMarkedSectionsAndPomTag() throws MojoExecutionException, IOException {
        /* ── Arrange: project structure ─────────────────────────────────────────── */
        // Java source with two marked regions
        String javaSrc =
                "public class StudentExercise {\n" +
                        "    //[[\n" +
                        "    private int answer = 42;\n" +
                        "    //]]\n" +
                        "    public void solve() {\n" +
                        "        //[[\n" +
                        "        System.out.println(answer);\n" +
                        "        //]]\n" +
                        "    }\n" +
                        "}\n";
        Files.writeString(root.resolve("StudentExercise.java"), javaSrc, StandardCharsets.UTF_8);

        // POM with the marker flag
        String pom =
                "<project>\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "  <groupId>example</groupId>\n" +
                        "  <artifactId>exercise</artifactId>\n" +
                        "  <version>1.0-SNAPSHOT</version>\n" +
                        "  <properties>\n" +
                        "    <stripMarked>true</stripMarked>\n" +
                        "  </properties>\n" +
                        "</project>\n";
        Files.writeString(root.resolve("pom.xml"), pom, StandardCharsets.UTF_8);

        // Enable strip-marked mode
        exportMojo.setStripMarked(true);

        /* ── Act: run the mojo ──────────────────────────────────────────────────── */
        exportMojo.execute();

        /* ── Assert: check the produced ZIP ─────────────────────────────────────── */
        Map<String, String> zipContents = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(
                new FileInputStream(target.resolve("export.zip").toFile()),
                StandardCharsets.UTF_8)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                zis.transferTo(baos);
                zipContents.put(entry.getName(), baos.toString(StandardCharsets.UTF_8));
            }
        }

        // 1. ZIP contains the expected files
        assertEquals(Set.of("StudentExercise.java", "pom.xml"), zipContents.keySet());

        // 2. Java source has no \\[[ or \\]] and the secret implementation vanished
        String strippedJava = zipContents.get("StudentExercise.java");
        assertFalse(strippedJava.contains("//[["), "Markers must be removed");
        assertFalse(strippedJava.contains("//]]"), "Markers must be removed");
        assertFalse(strippedJava.contains("answer = 42"), "Hidden code must be removed");
        assertFalse(strippedJava.contains("println"), "Second hidden block must be removed");

        // 3. pom.xml no longer contains the marker element
        String strippedPom = zipContents.get("pom.xml");
        assertFalse(strippedPom.contains("<stripMarked>true</stripMarked>"),
                "pom.xml should not expose the stripMarked flag to students");
    }


    @AfterEach
    void tearDown() throws IOException {
        Files.walk(root)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}