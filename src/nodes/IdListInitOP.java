package nodes;

import models.RowTable;
import visitors.Visitor;

//booooooooh
public class IdListInitOP {
    private Id id;
    //comma
    //assign
    //ArrayList<String> idList;
    private Expr expr;
    private RowTable rt = new RowTable();

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public IdListInitOP(Id id, Expr expr) {
        this.id = id;
        this.expr = expr;
    }
    public IdListInitOP(Id id) {
        this.id = id;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
