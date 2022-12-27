package com.rmsv.jmine;

import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        File repoDir = new File("/home/viraj/Documents/MCS/R/Data/metrics.git/.git");
        // now open the resulting repository with a FileRepositoryBuilder
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(repoDir)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            System.out.println("Having repository: " + repository.getDirectory());

            // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
//            Ref head = repository.exactRef("refs/heads/master");
//            System.out.println("Ref of refs/heads/master: " + head);

            try (Git git = new Git(repository)) {
                Iterable<RevCommit> commits = git.log().all().call();
                //int count = 0;
                ObjectId newHead = null, oldHead = null;
                Map<String, Integer> commiters = new HashMap<>();
                final Map<String, List<String>> changes = new HashMap<>();
                //final String commiter;
                for (RevCommit commit : commits) {
                    final String commiter = commit.getAuthorIdent().getName();
                    System.out.println("\n\nLogCommit: " + commit.getShortMessage() + " - "+ commiter);

                    if(commiters.containsKey(commiter)){
                        Integer a = commiters.get(commiter)+1;
                        commiters.replace(commiter, a);
                    }else{
                        commiters.put(commiter, 1);
                    }

                    newHead = commit.getTree().getId();
                    if(oldHead != null){
                        System.out.println("Printing diff between tree: " + oldHead + " and " + newHead);

                        // prepare the two iterators to compute the diff between
                        try (ObjectReader reader = repository.newObjectReader()) {
                            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                            oldTreeIter.reset(reader, oldHead);
                            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                            newTreeIter.reset(reader, newHead);

                            // finally get the list of changed files
                            List<DiffEntry> diffs = git.diff()
                                    .setNewTree(newTreeIter)
                                    .setOldTree(oldTreeIter)
                                    .call();
                            diffs.stream().filter(d -> d.getNewPath().endsWith(".java")).forEach(entry -> {
                                //System.out.println("\nEntry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId()+"\n\n");
                                //entry.getNewPath()

                                if(changes.containsKey(entry.getNewPath())){
                                    final List<String> l = changes.get(entry.getNewPath());
                                    if (!l.contains(commiter)){
                                        l.add(commiter);
                                        changes.replace(commiter, l);
                                    }
                                }else{
                                    final List<String> l = new ArrayList<>();
                                    l.add(commiter);
                                    changes.put(entry.getNewPath(), l);
                                }
                                /*try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                                    formatter.setRepository(repository);
                                    //formatter.setDiffComparator(RawTextComparator.DEFAULT);
                                    formatter.format(entry);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                            });

                            //for (DiffEntry entry : diffs) {
                                //System.out.println("\nEntry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId()+"\n\n");

                                //try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                                //    formatter.setRepository(repository);
                                //    formatter.format(entry);
                                //}
                                //System.out.println("Entry: " + entry);
                            //}
                        }

                    }
                    oldHead = newHead;
/*
                    RevTree tree = commit.getTree();
                    System.out.println("Having tree: " + tree);

                    // now use a TreeWalk to iterate over all files in the Tree recursively
                    // you can set Filters to narrow down the results if needed
                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(false);
                        while (treeWalk.next()) {
                            System.out.println("found: " + treeWalk.getPathString());
                        }
                    }
*/
                    //count++;
                }


                for (Map.Entry entry : commiters.entrySet()) {
                    System.out.println(entry.getKey() + ", " + entry.getValue());
                }

                for (Map.Entry entry : changes.entrySet()) {
                    System.out.println(entry.getKey()+" =>");
                    for(String user : (List<String>)entry.getValue()){
                        System.out.println("\t=>"+user);
                    }
                }

                //System.out.println(count);
            } catch (NoHeadException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }


           /* ObjectId oldHead = repository.resolve("HEAD^^^^{tree}");
            ObjectId head = repository.resolve("HEAD^{tree}");

            System.out.println("Printing diff between tree: " + oldHead + " and " + head);

            // prepare the two iterators to compute the diff between
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldHead);
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, head);

                // finally get the list of changed files
                try (Git git = new Git(repository)) {
                    List<DiffEntry> diffs= git.diff()
                            .setNewTree(newTreeIter)
                            .setOldTree(oldTreeIter)
                            .call();
                    for (DiffEntry entry : diffs) {
                        System.out.println("Entry: " + entry);
                    }
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }
            }*/



            //Ref head = repository.findRef("HEAD");

            // a RevWalk allows to walk over commits based on some filtering that is defined
            /*try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(head.getObjectId());
                RevTree tree = commit.getTree();
                System.out.println("Having tree: " + tree);

                // now use a TreeWalk to iterate over all files in the Tree recursively
                // you can set Filters to narrow down the results if needed
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        System.out.println("found: " + treeWalk.getPathString());
                    }
                }
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
