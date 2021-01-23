import java.util.ArrayList;

public class ParDeclOP {
    String type;
    ArrayList<Id> IdList;
    private RowTable rt= new RowTable();


    public ParDeclOP(String type, ArrayList<Id> idList) {
        this.type = type;
        IdList = idList;
    }

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Id> getIdList() {
        return IdList;
    }

    public void setIdList(ArrayList<Id> idList) {
        IdList = idList;
    }
}
