import java.util.ArrayList;

public class WriteOP extends Stat{
    private ArrayList<Expr> exprList;

    public WriteOP(ArrayList<Expr> exprList) {
        super();
        this.exprList=exprList;
    }

    public ArrayList<Expr> getExprList() {
        return exprList;
    }

    public void setExprList(ArrayList<Expr> exprList) {
        this.exprList = exprList;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
