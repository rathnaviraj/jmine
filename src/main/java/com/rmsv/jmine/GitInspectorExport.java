package com.rmsv.jmine;

import com.rmsv.jmine.dto.Contributor;
import com.rmsv.jmine.dto.SourceFile;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by viraj on 12/2/17.
 */
public class GitInspectorExport {

    private static String M_PATH = "C:\\Users\\Dab\\Documents\\MCS\\Research\\finalDataSets\\";
    private static String REPO = "spring-boot";

    private static String GIT_INSPECT_STAT = M_PATH + REPO + "\\Stats.json";
    private static String GIT_VUL_STAT = M_PATH + REPO + "\\Vulnerables.json";
    private static String GIT_DEVS = M_PATH + REPO + "\\DNDevs.json";
    private static String DN_BETWE = M_PATH + REPO + "\\betweennes_dn.json";
    private static String DN_CLOSE = M_PATH + REPO + "\\closeness_dn.json";

    private static Map<String, Integer> vDevs = new HashMap<>();
    private static Map<String, JSONObject> dnDevsA = new HashMap<>();
    private static Map<String, JSONObject> dnDevsB = new HashMap<>();

    private static Map<Integer, Double> dnBetwe = new HashMap<>();
    private static Map<Integer, Double> dnClose = new HashMap<>();

    public static void main(String[] args){
        File gitInspectorStat = new File(GIT_INSPECT_STAT);
        File gitVulStat = new File(GIT_VUL_STAT);
        File gitDevs = new File(GIT_DEVS);

        File gitDNBetwe = new File(DN_BETWE);
        File gitDNClose = new File(DN_CLOSE);

        JSONObject giStat = getJSONObj(gitInspectorStat, GIT_INSPECT_STAT);
        JSONArray giVuStat = getJSONArr(gitVulStat, GIT_VUL_STAT);
        JSONArray gitDnDevs = getJSONArr(gitDevs, GIT_DEVS);

        JSONArray gitDnBetw = getJSONArr(gitDNBetwe, DN_BETWE);
        JSONArray gitDnClos = getJSONArr(gitDNClose, DN_CLOSE);

        for (int x = 0; x < gitDnBetw.length(); x++){
            JSONObject dt = gitDnBetw.getJSONObject(x);
            dnBetwe.put(dt.getInt("id"), dt.getDouble("betweennes"));
        }

        for (int x = 0; x < gitDnClos.length(); x++){
            JSONObject dt = gitDnClos.getJSONObject(x);
            dnClose.put(dt.getInt("id"), dt.getDouble("closeness"));
        }


        for (int x = 0; x < gitDnDevs.length(); x++){
            JSONObject dt = gitDnDevs.getJSONObject(x);
            dnDevsA.put(dt.getString("email"), dt);
            dnDevsB.put(dt.getString("name"), dt);
        }

        for (int x = 0; x < giVuStat.length(); x++){
            JSONObject dt = giVuStat.getJSONObject(x);
            vDevs.put(dt.getString("id"), dt.getInt("issues"));
        }

        JSONArray authorsCh = giStat.getJSONObject("gitinspector").getJSONObject("changes").getJSONArray("authors");
        System.out.println("Authors Changes: "+authorsCh.length());

        JSONObject jsonAuth = null;
        Contributor cnt;
        for (int i = 0; i < authorsCh.length(); i++) {
            jsonAuth = authorsCh.getJSONObject(i);

            if(Contributor.getContributor(jsonAuth.getString("name")) == null){
                cnt = new Contributor(
                        jsonAuth.getString("name"),
                        jsonAuth.getString("email")
                );

                cnt.setCommits(jsonAuth.getInt("commits"));
                cnt.setInsertions(jsonAuth.getInt("insertions"));
                cnt.setDeletions(jsonAuth.getInt("deletions"));
                cnt.setPercentageOfChanges(jsonAuth.getDouble("percentage_of_changes"));

                if(vDevs.containsKey(cnt.getEmail())){
                    cnt.setIssues(vDevs.get(cnt.getEmail()));
                }

                JSONObject dev = null;

                if(dnDevsA.containsKey(cnt.getEmail())){
                    dev = dnDevsA.get(cnt.getEmail());
                }else if(dnDevsB.containsKey(cnt.getName())) {
                    dev = dnDevsB.get(cnt.getName());
                }else {
                    System.out.println("----------------------------- missing user : "+cnt.getEmail());
                }

                if(dev != null) {
                    cnt.setId(dev.getInt("id"));
                    cnt.setFrequentCommitHour(dev.getInt("commitHour"));
                    cnt.setDnBetweennes(dnBetwe.get(cnt.getId()));
                    cnt.setDnCloseness(dnClose.get(cnt.getId()));
                }
            }
        }

        JSONArray authorsBl = giStat.getJSONObject("gitinspector").getJSONObject("blame").getJSONArray("authors");
        System.out.println("Authors Blame: "+authorsBl.length());

        JSONObject jso = null;
        Contributor cnt2;
        for (int i = 0; i < authorsBl.length(); i++) {
            jso = authorsBl.getJSONObject(i);

            if(Contributor.getContributor(jso.getString("email")) == null){
                cnt2 = new Contributor(jso.getString("name"),
                        jso.getString("email")
                );
            }else{
                cnt2 = Contributor.getContributor(jso.getString("email"));
            }

            cnt2.setRows(jso.getInt("rows"));
            cnt2.setStability(jso.getDouble("stability"));
            cnt2.setAge(jso.getDouble("age"));
            cnt2.setPercentageInComments(jso.getDouble("percentage_in_comments"));
        }

        JSONObject metrics = giStat.getJSONObject("gitinspector").getJSONObject("metrics");

        JSONArray violations = metrics.getJSONArray("violations");
        JSONArray responsibilities = giStat.getJSONObject("gitinspector").getJSONObject("responsibilities").getJSONArray("authors");

        List<JSONObject> cyclomaticComplexity = arrayToStream(violations).map(JSONObject.class::cast).filter(o -> o.get("type").equals("cyclomatic-complexity")).collect(Collectors.toList());

        System.out.println("Cyclomatically Complex Files : "+ cyclomaticComplexity.size());
        for(JSONObject j : cyclomaticComplexity){
            //System.out.println(j);
            String fileName = j.getString("file_name");
            SourceFile sf = SourceFile.getOrNew(fileName);
            sf.setComplexity(j.getInt("value"));
            Contributor contrb;
            for (int i = 0; i < responsibilities.length(); i++) {
                //System.out.println(responsibilities.getJSONObject(i));

                JSONObject cData = responsibilities.getJSONObject(i);

                List<JSONObject> a = arrayToStream(cData.getJSONArray("files")).map(JSONObject.class::cast).filter(o -> o.get("name").equals(fileName)).collect(Collectors.toList());

                if(a.size() > 0){
                    //System.out.println(a.get(0)+" : "+cData.getString("email"));
                    contrb = Contributor.getContributor(cData.getString("email"));
                    contrb.addSourceFileContribution(fileName, a.get(0).getInt("rows"));

                    sf.addContributor(contrb);
                    sf.addRowCount(a.get(0).getInt("rows"));
                }
            }
        }

        for (Map.Entry entry : SourceFile.SOURCE_FILES.entrySet()) {
            //System.out.println(entry.getKey() + ", " + entry.getValue());
            SourceFile s = (SourceFile)entry.getValue();

            for (Map.Entry<String, Contributor> e : s.getContributors().entrySet()) {
                e.getValue().setAverageComplexity(s.getComplexity());
                //System.out.println(entry.getKey() + "/" + entry.getValue());
            }
            /*if(s.getMainContributor() != null) {
                //System.out.println(s.getPath() + " : " + s.getMainContributor() +" : "+s.getComplexity());
                System.out.println(s.getMainContributor() +" : "+s.getComplexity());
            }*/
        }

        Contributor.documentAll(M_PATH + REPO + "\\finalData.csv");
    }

    private static Stream<Object> arrayToStream(JSONArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    private static JSONObject getJSONObj(File f, String path){
        JSONObject giStat = null;
        if (f.exists()){
            InputStream is = null;
            try {
                is = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String jsonTxt = null;
            try {
                jsonTxt = IOUtils.toString(is, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            giStat = new JSONObject(jsonTxt);

            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return giStat;
    }

    private static JSONArray getJSONArr(File f, String path){
        JSONArray giStat = null;
        if (f.exists()){
            InputStream is = null;
            try {
                is = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String jsonTxt = null;
            try {
                jsonTxt = IOUtils.toString(is, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            giStat = new JSONArray(jsonTxt);

            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return giStat;
    }
}
