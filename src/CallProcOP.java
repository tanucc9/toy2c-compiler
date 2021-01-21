import java.util.ArrayList;

public class CallProcOP {
    private String val;
    private ArrayList<Expr> elist;

    public CallProcOP(String val, ArrayList<Expr> elist) {
        this.val=val;
        this.elist=elist;
    }
    public CallProcOP(String val){
        this.val=val;

    }

    public void setVal(String val) {
        this.val = val;
    }

    public void setElist(ArrayList<Expr> elist) {
        this.elist = elist;
    }

    public String getVal() {
        return val;
    }

    public ArrayList<Expr> getElist() {
        return elist;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
