public class StringConst extends Expr{
    private String s;
    private RowTable rt = new RowTable();

    public StringConst(String s){
        this.s=s;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
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
