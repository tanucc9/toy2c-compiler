package nodes;

import models.RowTable;
import visitors.Visitor;

public class Expr {

    private RowTable rt = new RowTable();
   private CallProcOP cp;

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public Expr(){}


    public Expr (CallProcOP cp) {
        this.cp=cp;
    }


    public CallProcOP getCp() {
        return cp;
    }

    public void setCp(CallProcOP cp) {
        this.cp = cp;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
