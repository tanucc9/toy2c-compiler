import java.util.ArrayList;

public class ElseOP {
    private BodyOP sList;
    private RowTable rt;

    public ElseOP(BodyOP sList) {
        this.sList=sList;
    }

    public void setsList(BodyOP sList) {
        this.sList = sList;
    }

    public BodyOP getsList() {
        return sList;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
