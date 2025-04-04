# Export Maven Plugin

[![Actions Status: build](https://github.com/atp-mipt/export-maven-plugin/workflows/build/badge.svg)](https://github.com/atp-mipt/export-maven-plugin/actions?query=workflow%3A"build")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.atp-fivt/export-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.atp-fivt/export-maven-plugin)

The Export Maven Plugin is an Apache Maven plugin designed for exporting an entire Maven project into a .zip file. It respects the `.gitignore` settings and, regardless of `.gitignore`, always omits the build directory (`target`).

## Why is it needed?

Typically, archiving a project involves the following command:

```shell
git archive -o export.zip HEAD
```

However, in teaching environments, particularly when instructing large groups in Java, it's not always feasible to assume that Git is installed on all systems. In scenarios where Git is not part of the course curriculum, and lab/homework submissions are required in .zip format, it's important to ensure that students' submissions do not include unwanted files, such as those in the build folder or IDE-specific directories.

Since students are expected to use Maven and create their projects via [homework-quickstart archetype](https://github.com/atp-mipt/homework-quickstart), this simple plugin enables students to automatically export their projects in the required format without including unwanted files.

## How to use

To use the plugin, insert the following configuration into the plugins section of your pom.xml:

```xml
    <plugin>
        <groupId>org.atp-fivt</groupId>
        <artifactId>export-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
            <zipFileName>Name_Surname.zip</zipFileName>
        </configuration>
    </plugin>
```

The `zipFileName` parameter allows you to specify the desired name for the zip file. By default, the file name is set to export.zip.

To run the plugin, execute:

```shell
mvn export:export
```

The resulting `.zip` file will be located in the target directory.

## How does it work

This plugin leverages the [jGit](https://www.eclipse.org/jgit/) library to compile a list of all files in the project, adhering to the rules specified in `.gitignore` files. Importantly, the functionality of this plugin does not require Git to be installed on the target machine. This is particularly beneficial in educational settings or environments where Git installation cannot be assumed. By using jGit, the plugin operates independently of the local Git installation.
