public class Bool extends Expr {
    private boolean b;
    private RowTable rt = new RowTable();

    public Bool(boolean b){
        this.b=b;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    @Override
    public RowTable getRt() {
        return rt;
    }

    @Override
    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

}
