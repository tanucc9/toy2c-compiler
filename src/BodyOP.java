import java.util.ArrayList;

public class BodyOP {
    private ArrayList<Stat> statList;

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
