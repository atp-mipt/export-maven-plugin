package org.atpfivt;

import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.NotIgnoredFilter;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursively collects paths to files and directories in the given folder
 * respecting the .gitignore files.
 * This class utilizes jGit API.
 */
final class PathsCollector {
    private final Path root;

    PathsCollector(Path root) {
        this.root = root;
    }

    public List<Path> getFiles() throws IOException {

        List<Path> entries = new ArrayList<>();

        FileTreeIterator fileTreeIterator = new FileTreeIterator(root.toFile(), FS.detect(),
                WorkingTreeOptions.KEY.parse(new Config()));
        try (InMemoryRepository repository = new InMemoryRepository.Builder()
                .setRepositoryDescription(new DfsRepositoryDescription())
                .setFS(FS.detect()).build();
             TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(fileTreeIterator);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(new NotIgnoredFilter(0));
            while (treeWalk.next()) {
                if (fileTreeIterator.isEntryIgnored()) {
                    System.out.println("ENTRY IGNORED: " + treeWalk.getPathString());
                }
                Path entry = root.resolve(treeWalk.getPathString());
                if (!Files.exists(entry)) {
                    throw new IllegalStateException();
                }
                entries.add(entry);
            }
        }
        return entries;
    }
}
