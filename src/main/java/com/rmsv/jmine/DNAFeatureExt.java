package com.rmsv.jmine;

import com.rmsv.jmine.dto.Developer;
import com.rmsv.jmine.dto.SourceEdge;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by viraj on 8/20/17.
 */
public class DNAFeatureExt {
    public static void main( String[] args )
    {
        String repo = "spring-boot";

        String dataPath = "C:\\Users\\Dab\\Documents\\MCS\\Research\\finalDataSets\\"+repo;
        String repoPath = "C:\\Users\\Dab\\Documents\\MCS\\Research\\Data\\"+repo;

        File repoDir = new File(repoPath+"\\.git");

        BufferedWriter dnWriter = null;
        try {
            dnWriter = new BufferedWriter(new FileWriter(dataPath+"\\DnData.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter devWriter = null;
        try {
            devWriter = new BufferedWriter(new FileWriter(dataPath+"\\DnDevs.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter commitWriter = null;
        try {
            commitWriter = new BufferedWriter(new FileWriter(dataPath+"\\commit.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }




        // C:\\Users\\Dab\\Documents\\MCS\\Research\\Data\\RxJava\\.git
        // now open the resulting repository with a FileRepositoryBuilder
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(repoDir)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            System.out.println("Having repository: " + repository.getDirectory());
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> commits = git.log().all().call();
                //int count = 0;
                ObjectId newHead = null, oldHead = null;
                Map<String, Developer> devs = new HashMap<>();
                final Map<String, List<Developer>> changes = new HashMap<>();
                //final String commiter;
                for (RevCommit commit : commits) {
                    final String commiter = commit.getAuthorIdent().getName();

                    commitWriter.write("LogCommit: - " +commiter+" - "+commit.getAuthorIdent().getEmailAddress()+"\n");

                    LocalDateTime ldt = LocalDateTime.ofInstant(commit.getAuthorIdent().getWhen().toInstant(), ZoneId.systemDefault());
                    //System.out.println("\n\nLogCommit: " + commit.getShortMessage() + " - "+ commiter +" - " + ldt.getHour() );


                    if(devs.containsKey(commiter)){
                        devs.replace(commiter, devs.get(commiter).updateEncounter().setCommitHours(ldt.getHour()));
                    }else{
                        devs.put(commiter, new Developer(commit.getAuthorIdent()).setCommitHours(ldt.getHour()));
                    }

                    newHead = commit.getTree().getId();
                    if(oldHead != null){
                        //System.out.println("Printing diff between tree: " + oldHead + " and " + newHead);

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

                            Developer d = Developer.getDeveloper(commiter);
                            d.setCommitHours(ldt.getHour());
                            d.setCommitDates(ldt);

                            if(diffs.size() > 5){
                                d.setMajorChanges(ldt);
                            }else {
                                d.setMinorChanges(ldt);
                            }

                            diffs.stream().filter(di -> di.getNewPath().endsWith(".java")).forEach(entry -> {
                                //System.out.println("\nEntry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId()+"\n\n");

                                if(changes.containsKey(entry.getNewPath())){
                                    List<Developer> l = changes.get(entry.getNewPath());
                                    if (!l.contains(d)){
                                        l.add(d);
                                        changes.replace(commiter, l);
                                    }
                                }else{
                                    List<Developer> l = new ArrayList<>();
                                    l.add(d);
                                    changes.put(entry.getNewPath(), l);
                                }
                            });
                        }
                    }
                    oldHead = newHead;
                }
                List<Developer> l = new ArrayList<>();

                for (Map.Entry entry : devs.entrySet()) {
                    //System.out.println(entry.getKey() + ", " + entry.getValue());
                    l.add((Developer)entry.getValue());
                }
                Collections.sort(l);
                devWriter.write("[");
                for (Developer d : l){
                    devWriter.write(d.getDocFormat()+",\n");
                    System.out.println(d.getDocFormat());
                }
                devWriter.write("]");

                Map<SourceEdge, SourceEdge> edges = new HashMap<>();
                for (Map.Entry entry : changes.entrySet()) {
                    //System.out.println(entry.getKey()+" =>");
                    Developer keyDev = null;
                    SourceEdge sEdge;
                    for(Developer dev : (List<Developer>)entry.getValue()){
                        //System.out.println("\t=>"+dev);
                        if(keyDev == null){
                            keyDev = dev;
                            continue;
                        }else {
                            sEdge = new SourceEdge(keyDev, dev);
                            if(edges.containsKey(sEdge)){
                                edges.get(sEdge).updateEdgeFrequency();
                            }else {
                                edges.put(sEdge,sEdge);
                            }
                        }
                    }
                }

                SourceEdge se;
                for (Map.Entry entry : edges.entrySet()) {
                    se = (SourceEdge) entry.getValue();
                    dnWriter.write(se.getvDevA().getId()+","+se.getvDevB().getId()+","+1+"\n");
                    //System.out.println(se.getvDevA().getId()+","+se.getvDevB().getId()+","+1);
                    //System.out.println(entry.getValue());
                }


                //Developer.printAllDevs();

            } catch (NoHeadException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dnWriter.close();
            devWriter.close();
            commitWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
