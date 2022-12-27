package com.rmsv.jmine.dto;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by viraj on 12/2/17.
 */
public class Contributor {

    private static Map<String, Contributor> CONTRIBUTORS = new HashMap<>();

    private Integer id;
    private String name;
    private String email;
    private Integer issues;
    private Integer rows;
    private Integer commits;
    private Integer insertions;
    private Integer deletions;
    private Double percentageOfChanges;
    private Double stability;
    private Double age;
    private Double percentageInComments;
    private Double averageComplexity;
    private Double dnBetweennes;
    private Double dnCloseness;
    private Integer frequentCommitHour;
    private List<Integer> complexityList;
    private Map<String, Integer> contributions;

    public Contributor(String name, String email) {
        this.name = name;
        this.email = email;
        this.issues = 0;
        this.contributions = new HashMap<>();
        complexityList = new ArrayList<>();
        CONTRIBUTORS.put(this.email, this);
    }

    public static Contributor getContributor(String email){
        return CONTRIBUTORS.get(email);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIssues() {
        return issues;
    }

    public void setIssues(Integer issues) {
        this.issues = issues;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getCommits() {
        return commits;
    }

    public void setCommits(Integer commits) {
        this.commits = commits;
    }

    public Integer getInsertions() {
        return insertions;
    }

    public void setInsertions(Integer insertions) {
        this.insertions = insertions;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Double getPercentageOfChanges() {
        return percentageOfChanges;
    }

    public void setPercentageOfChanges(Double percentageOfChanges) {
        this.percentageOfChanges = percentageOfChanges;
    }

    public Double getStability() {
        return stability;
    }

    public void setStability(Double stability) {
        this.stability = stability;
    }

    public Double getAge() {
        return age;
    }

    public void setAge(Double age) {
        this.age = age;
    }

    public Double getPercentageInComments() {
        return percentageInComments;
    }

    public void setPercentageInComments(Double percentageInComments) {
        this.percentageInComments = percentageInComments;
    }

    public Double getDnBetweennes() {
        return dnBetweennes;
    }

    public void setDnBetweennes(Double dnBetweennes) {
        this.dnBetweennes = dnBetweennes;
    }

    public Double getDnCloseness() {
        return dnCloseness;
    }

    public void setDnCloseness(Double dnCloseness) {
        this.dnCloseness = dnCloseness;
    }

    public Integer getFrequentCommitHour() {
        return frequentCommitHour;
    }

    public void setFrequentCommitHour(Integer frequentCommitHour) {
        this.frequentCommitHour = frequentCommitHour;
    }

    public Double getAverageComplexity() {
        if(this.complexityList.size() > 0) {
            this.averageComplexity = complexityList.stream().mapToDouble(a -> a).average().getAsDouble();
        }else{
            this.averageComplexity = null;
        }
        return this.averageComplexity;
    }

    public void setAverageComplexity(Integer averageComplexity) {
        this.complexityList.add(averageComplexity);
    }

    public void addSourceFileContribution(String fileName, Integer rows){
        this.contributions.put(fileName, rows);
    }

    public Integer getSourceFileContribution(String fileName){
        return this.contributions.get(fileName);
    }

    public String getCSVFormat(){
        return this.email + "," + this.name + "," + _(this.rows) + "," + _(this.commits) + "," + _(this.frequentCommitHour) + "," + _(this.insertions) + "," + _(this.deletions) + "," + _(this.age) + "," + _(this.stability) + "," + _(getAverageComplexity()) + "," + _(this.percentageInComments) + "," + _(this.percentageOfChanges) + "," + _(this.dnBetweennes) + "," + _(this.dnCloseness) + "," + meanPercentageIssues();
    }

    public String getDocFormat(){
        return "{email=" + email + ", name=" + name + ", rows=" + rows + "}";
    }

    @Override
    public String toString(){
        return getDocFormat();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contributor contributor = (Contributor) o;

        return email.equals(contributor.email);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    public static void documentAll(String dataPath) {

        BufferedWriter devWriter = null;
        try {
            devWriter = new BufferedWriter(new FileWriter(dataPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            devWriter.write("email,name,rows,commits,frqComHour,insertions,deletions,age,stability,averageComplexity,percentageInComments,percentageOfChanges,betweennes,closeness,issues\n");
            for (Map.Entry<String, Contributor> e : CONTRIBUTORS.entrySet()) {
                //System.out.println(e.getValue().getCSVFormat()+"\n");
                devWriter.write(e.getValue().getCSVFormat()+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            devWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("email,name,rows,commits,frqComHour,insertions,deletions,age,stability,averageComplexity,percentageInComments,percentageOfChanges,betweennes,closeness,issues");
    }

    private Double meanPercentageIssues() {
        if (rows != null) {
            return Double.valueOf(issues) / Double.valueOf(rows);
        }else {
            return Double.valueOf(issues);
        }
    }
    private String _(Number a){
        if(a != null){
            return a.toString();
        }else {
            return "-1";
        }

    }
}
