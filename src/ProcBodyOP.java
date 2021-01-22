import java.util.ArrayList;

public class ProcBodyOP {
    private ArrayList<VarDeclOP> vdList;
    private BodyOP sList;
    private ArrayList<Expr> re;
    private RowTable rt;

    public ProcBodyOP(ArrayList<VarDeclOP> vdList, BodyOP sList,ArrayList<Expr> re){
        this.vdList= vdList;
        this.sList= sList;
        this.re = re;
    }
    public ProcBodyOP(ArrayList<VarDeclOP> vdList, ArrayList<Expr> re){
        this.vdList= vdList;
        this.re = re;
    }

    public ArrayList<VarDeclOP> getVdList() {
        return vdList;
    }

    public void setVdList(ArrayList<VarDeclOP> vdList) {
        this.vdList = vdList;
    }

    public BodyOP getsList() {
        return sList;
    }

    public void setsList(BodyOP sList) {
        this.sList = sList;
    }

    public ArrayList<Expr> getRe() {
        return re;
    }

    public void setRe(ArrayList<Expr> re) {
        this.re = re;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
