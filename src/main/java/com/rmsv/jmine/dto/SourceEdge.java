package com.rmsv.jmine.dto;

/**
 * Created by viraj on 8/20/17.
 */
public class SourceEdge {

    private Integer edgeFrequency;
    private Developer vDevA;
    private Developer vDevB;

    public SourceEdge(Developer vDevA, Developer vDevB) {
        this.vDevA = vDevA;
        this.vDevB = vDevB;
        this.edgeFrequency = 1;
    }

    public void updateEdgeFrequency(){
        this.edgeFrequency++;
    }

    public Integer getEdgeFrequency() {
        return edgeFrequency;
    }

    public void setEdgeFrequency(Integer edgeFrequency) {
        this.edgeFrequency = edgeFrequency;
    }

    public Developer getvDevA() {
        return vDevA;
    }

    public void setvDevA(Developer vDevA) {
        this.vDevA = vDevA;
    }

    public Developer getvDevB() {
        return vDevB;
    }

    public void setvDevB(Developer vDevB) {
        this.vDevB = vDevB;
    }

    public double getEdgeCloseness(){
        double c = (1d/this.edgeFrequency)*100;
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceEdge that = (SourceEdge) o;

        if((this.vDevA.getId() == that.vDevA.getId() && this.vDevB.getId() == that.vDevB.getId()) || (this.vDevA.getId() == that.vDevB.getId() && this.vDevB.getId() == that.vDevA.getId())){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return vDevA.hashCode() + vDevB.hashCode();
    }

    @Override
    public String toString() {
        return "SourceEdge{" +
                "edgeFrequency=" + edgeFrequency +
                ", vDevA=" + vDevA.toString() +
                ", vDevB=" + vDevB.toString() +
                '}';
    }
}
