package nodes;

import visitors.Visitor;

import java.util.ArrayList;

public class ReadOP extends Stat{
    private ArrayList<Id> idList;

    public ReadOP() {
        super();
        this.idList=new ArrayList<Id>();
    }

    public ReadOP (ArrayList<Id> idList) {
        super();
        this.idList=idList;
    }

    public ArrayList<Id> getIdList() {
        return idList;
    }

    public void setIdList(ArrayList<Id> idList) {
        this.idList = idList;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }



}
