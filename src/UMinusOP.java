public class UMinusOP extends Expr{

    private Expr e;

    public UMinusOP(Expr e){
        this.e=e;
    }

    public Expr getE() {
        return e;
    }

    public void setE(Expr e) {
        this.e = e;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
