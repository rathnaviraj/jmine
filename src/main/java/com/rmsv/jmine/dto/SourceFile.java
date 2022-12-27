package com.rmsv.jmine.dto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by viraj on 8/20/17.
 */
public class SourceFile {

    public static Map<String, SourceFile> SOURCE_FILES = new HashMap<>();

    private String path;
    private Integer complexity;
    private Integer rowCount = 0;
    private Map<String, Contributor> contributors = new HashMap<>();

    public SourceFile(String path){
        this.path = path;
        SOURCE_FILES.put(path, this);
    }

    public static SourceFile getOrNew(String path){
        if(SOURCE_FILES.containsKey(path)){
            //System.out.println("EF");
            return SOURCE_FILES.get(path);
        }else {
            //System.out.println("NF");
            return new SourceFile(path);
        }
    }

    public Integer getComplexity() {
        return complexity;
    }

    public void setComplexity(Integer complexity) {
        this.complexity = complexity;
    }

    public String getPath() {
        return path;
    }

    public void addRowCount(Integer rowCount){
        this.rowCount +=rowCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public Map<String, Contributor> getContributors(){
        return contributors;
    }

    /*public Contributor getMainContributor(){
        if(contributors.size() > 1){
            return contributors.entrySet().stream().map(v -> v.getValue()).max(Comparator.comparingInt(Contributor::getRows)).get();
        }else if (contributors.size() == 1){
            return contributors.entrySet().iterator().next().getValue();
        }else {
            return null;
        }
    }*/

    public void addContributor(Contributor contributor){
        this.contributors.put(contributor.getEmail(), contributor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceFile sourceFile = (SourceFile) o;

        return path.equals(sourceFile.path);
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result;
        return result;
    }

    @Override
    public String toString() {
        return "SourceFile{path=" + path +"}";
    }
}
