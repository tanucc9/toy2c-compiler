package nodes;

import models.RowTable;
import visitors.Visitor;

import java.util.ArrayList;

public class BodyOP {
    private ArrayList<Stat> statList;
    private RowTable rt = new RowTable();

    public BodyOP (ArrayList<Stat> statList) {
        this.statList=statList;
    }

    public BodyOP () {
        this.statList=new ArrayList<Stat>();
    }

    public BodyOP(Stat s) {
        this.statList=new ArrayList<Stat>();
        statList.add(s);
    }

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public void setStatList(ArrayList<Stat> statList) {
        this.statList = statList;
    }

    public ArrayList<Stat> getStatList() {
        return statList;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }

}
