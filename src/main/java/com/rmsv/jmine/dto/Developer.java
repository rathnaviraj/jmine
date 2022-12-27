package com.rmsv.jmine.dto;

import org.eclipse.jgit.lib.PersonIdent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by viraj on 8/20/17.
 */
public class Developer implements Comparable {

    private static Integer OBJECT_COUNT = 0;
    private static Map<String, Developer> DEVELOPERS = new HashMap<>();

    private Integer id;
    private PersonIdent data;
    private DeveloperGitHubProfile profile;
    private Integer encounters = 1;
    private Integer changeFrequency = 1;
    private Integer[] commitHours;
    private List<LocalDateTime> commitDates;
    private List<LocalDateTime> majorChanges;
    private List<LocalDateTime> minorChanges;

    public Developer(PersonIdent data){
        this.commitHours = new Integer[24];
        Arrays.fill(this.commitHours, 0);
        this.data = data;
        this.encounters = 1;
        this.changeFrequency = 1;
        this.commitDates = new ArrayList<>();
        this.majorChanges = new ArrayList<>();
        this.minorChanges = new ArrayList<>();
        OBJECT_COUNT++;
        this.id = OBJECT_COUNT;
        DEVELOPERS.put(this.data.getName(), this);


        this.loadGitHubData();
    }

    public static Developer getDeveloper(String name){
        return DEVELOPERS.get(name);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        id = id;
    }

    public PersonIdent getData() {
        return data;
    }

    public void setData(PersonIdent data) {
        this.data = data;
    }

    public DeveloperGitHubProfile getProfile() {
        return profile;
    }

    public void setProfile(DeveloperGitHubProfile profile) {
        this.profile = profile;
    }

    public Integer getEncounters() {
        return encounters;
    }

    public void setEncounters(Integer encounters) {
        this.encounters = encounters;
    }

    public Integer getChangeFrequency() {
        return changeFrequency;
    }

    public Developer setCommitHours(int hour){
        this.commitHours[hour]++;
        return this;
    }

    public Integer getMostFrequentCommitHour(){
        int maxAt = 0;

        for (int i = 0; i < this.commitHours.length; i++) {
            maxAt = this.commitHours[i] > this.commitHours[maxAt] ? i : maxAt;
        }
        return maxAt;
    }

    public List<LocalDateTime> getCommitDates() {
        return commitDates;
    }

    public Double getAverageCommitDistance(){
        LocalDateTime pldt = null;
        double hours = 0d;
        for(LocalDateTime ldt: this.commitDates){
            if(pldt != null){
                hours += pldt.until( ldt, ChronoUnit.HOURS);
            }
            pldt = ldt;
        }

        return hours/(double)(this.commitDates.size() -1);
    }

    public void setCommitDates(LocalDateTime commitDate) {
        this.commitDates.add(commitDate);
    }

    public static Integer getObjectCount() {
        return OBJECT_COUNT;
    }

    public static void setObjectCount(Integer objectCount) {
        OBJECT_COUNT = objectCount;
    }

    public List<LocalDateTime> getMajorChanges() {
        return majorChanges;
    }

    public Double getAverageMajorChangeDistance(){
        LocalDateTime pldt = null;
        double hours = 0d;
        for(LocalDateTime ldt: this.majorChanges){
            if(pldt != null){
                hours += pldt.until( ldt, ChronoUnit.HOURS);
            }
            pldt = ldt;
        }

        return hours/(double)(this.majorChanges.size() -1);
    }

    public void setMajorChanges(LocalDateTime majorChange) {
        this.majorChanges.add(majorChange);
    }

    public List<LocalDateTime> getMinorChanges() {
        return minorChanges;
    }

    public Double getAverageMinorChangeDistance(){
        //this.minorChanges;
        LocalDateTime pldt = null;
        double hours = 0d;
        for(LocalDateTime ldt: this.minorChanges){
            if(pldt != null){
                hours += pldt.until( ldt, ChronoUnit.HOURS);
            }
            pldt = ldt;
        }

        return hours/(double)(this.minorChanges.size() -1);
    }

    public void setMinorChanges(LocalDateTime minorChange) {
        this.minorChanges.add(minorChange);
    }

    public void setChangeFrequency(Integer changeFrequency) {
        this.changeFrequency = changeFrequency;
    }

    public Developer updateEncounter(){
        this.encounters++;
        return this;
    }

    public Developer updateChangeFrequency(){
        this.changeFrequency++;
        return this;
    }

    public static void printAllDevs(){

        Developer[] devs = DEVELOPERS.values().toArray(new Developer[DEVELOPERS.size()]);
        Arrays.sort(devs, new Comparator<Developer>() {
            @Override
            public int compare(Developer left, Developer right) {
                return left.getId() - right.getId(); // use your logic
            }
        });

        for (Developer d : devs){
            System.out.print(d.getDocFormat()+", ");
        }
    }

    public String getDocFormat(){
        return "{\"id\":"+ id + ", \"name\":\""+ data.getName() + "\", \"email\":\""+this.data.getEmailAddress()+"\", \"commitHour\":"+this.getMostFrequentCommitHour() + ( this.profile != null ? ", " + this.profile.toJSON() : "" ) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Developer developer = (Developer) o;

        return id.equals(developer.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + data.getName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Developer{" +
                "id=" + id +
                ", name=" + data.getName() +
                ", encounters=" + encounters +
                ", commitHour=" + this.getMostFrequentCommitHour() +
                ", changeFrequency=" + changeFrequency +
                "}";
    }

    @Override
    public int compareTo(Object o) {
        Developer d = (Developer) o;
        if(d.id > this.id){
            return -1;
        }else if(d.id < this.id){
            return 1;
        }else {
            return 0;
        }
    }

    private void loadGitHubData(){
        String token = "158035a5e7a645d22ec293a2c0c9dccf831acf1b";
        String githubApiURL = "https://api.github.com/users";
        String username = this.data.getEmailAddress().split("@")[0];

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        //Add the Jackson Message converter
        MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
        // Note: here we are making this converter to process any kind of response,
        // not only application/*json, which is the default behaviour
        converter.setSupportedMediaTypes(Arrays.asList(new MediaType[]{MediaType.ALL}));
        messageConverters.add(converter);

        String url = githubApiURL + "/"+username + "?access_token=" + token;
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setMessageConverters(messageConverters);
            this.profile = restTemplate.getForObject(url, DeveloperGitHubProfile.class);

            System.out.println(this.profile);
        }catch (Exception e){
            System.out.println("No Git Hub For : "+e);
        }


    }
}
