package nodes;

import visitors.Visitor;

public class IntConst extends Expr {
    private int val;


    public IntConst(int x){
        this.val=x;

    }

    public int getVal() {
        return val;
    }
    public void setVal(int val) {
        this.val = val;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
