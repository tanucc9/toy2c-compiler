package nodes;

import visitors.Visitor;

public class NotOP extends Expr {
    private Expr ne;

    public NotOP(Expr expr){
        this.ne=expr;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

    public Expr getNe() {
        return ne;
    }

    public void setNe(Expr ne) {
        this.ne = ne;
    }
}
