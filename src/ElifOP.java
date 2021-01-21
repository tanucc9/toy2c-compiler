import java.util.ArrayList;

public class ElifOP {
    private Expr e;
    private BodyOP sList;

    public ElifOP(Expr e, BodyOP sList) {
        this.e=e;
        this.sList=sList;
    }

    public void setE(Expr e) {
        this.e = e;
    }

    public void setsList(BodyOP sList) {
        this.sList = sList;
    }

    public Expr getE() {
        return e;
    }

    public BodyOP getsList() {
        return sList;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
