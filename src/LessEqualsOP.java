public class LessEqualsOP extends Expr {
    private Expr e, e1;

    public LessEqualsOP(Expr e, Expr e1){
        this.e=e;
        this.e1=e1;

    }

    public Expr getE() {
        return e;
    }

    public void setE(Expr e) {
        this.e = e;
    }

    public Expr getE1() {
        return e1;
    }

    public void setE1(Expr e1) {
        this.e1 = e1;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
