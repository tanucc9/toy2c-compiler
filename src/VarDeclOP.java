import java.util.ArrayList;

public class VarDeclOP {
    String type;
    //ArrayList<IdInitOP> IdListInit;
    ArrayList<IdListInitOP> IdListInit;
    private RowTable rt = new RowTable();

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public VarDeclOP(String type, ArrayList<IdListInitOP> idListInit) {
        this.type = type;
        IdListInit = idListInit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<IdListInitOP> getIdListInit() {
        return IdListInit;
    }

    public void setIdListInit(ArrayList<IdListInitOP> idListInit) {
        IdListInit = idListInit;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

}
