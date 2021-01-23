public class IntConst extends Expr {
    private int val;
    private RowTable rt = new RowTable();


    public IntConst(int x){
        this.val=x;

    }

    public int getVal() {
        return val;
    }

    @Override
    public RowTable getRt() {
        return rt;
    }

    @Override
    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public void setVal(int val) {
        this.val = val;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
