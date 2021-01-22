import java.util.ArrayList;
//null
public class ReturnExprsOP {
    ArrayList<Expr> ExprList;
    private RowTable rt;

    public ReturnExprsOP(ArrayList<Expr> exprList) {
        ExprList = exprList;
    }

    public ArrayList<Expr> getExprList() {
        return ExprList;
    }

    public void setExprList(ArrayList<Expr> exprList) {
        ExprList = exprList;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
