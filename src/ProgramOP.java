import java.util.ArrayList;

public class ProgramOP {
    private ArrayList<VarDeclOP> VarDeclList ;
    private ArrayList<ProcOP> ProcList;
    private ArrayList<RowTable> globalTable= new ArrayList<RowTable>();
    private RowTable rt;


    ProgramOP(ArrayList<VarDeclOP> VarDeclListOP ,ArrayList<ProcOP> ProcListOP){
        this.ProcList= ProcListOP;
        this.VarDeclList= VarDeclListOP;

    }

    public ArrayList<RowTable> getGlobalTable() {
        return globalTable;
    }

    public void setGlobalTable(ArrayList<RowTable> globalTable) {
        this.globalTable = globalTable;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

    public ArrayList<VarDeclOP> getVarDeclList() {
        return VarDeclList;
    }

    public void setVarDeclList(ArrayList<VarDeclOP> varDeclList) {
        VarDeclList = varDeclList;
    }

    public ArrayList<ProcOP> getProcList() {
        return ProcList;
    }

    public void setProcList(ArrayList<ProcOP> procList) {
        ProcList = procList;
    }
}
