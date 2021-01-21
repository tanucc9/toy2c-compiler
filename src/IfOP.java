import java.util.ArrayList;

public class IfOP extends Stat{
    private Expr e;
    private BodyOP sList;
    private ArrayList<ElifOP>elList;
    private ElseOP el;

    public IfOP(Expr e, BodyOP sList, ArrayList<ElifOP> elList, ElseOP el) {
        super();
        this.e=e;
        this.sList=sList;
        this.elList=elList;
        this.el=el;
    }

    public Expr getE() {
        return e;
    }

    public BodyOP getsList() {
        return sList;
    }

    public ArrayList<ElifOP> getElList() {
        return elList;
    }

    public ElseOP getEl() {
        return el;
    }

    public void setE(Expr e) {
        this.e = e;
    }

    public void setsList(BodyOP sList) {
        this.sList = sList;
    }

    public void setElList(ArrayList<ElifOP> elList) {
        this.elList = elList;
    }

    public void setEl(ElseOP el) {
        this.el = el;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }
}
