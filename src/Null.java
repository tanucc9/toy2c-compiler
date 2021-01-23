public class Null extends Expr {
    private String n;
    private RowTable rt= new RowTable();

    public Null(){
        this.n="null";
    }

    public String getN() {
        return n;
    }

    @Override
    public RowTable getRt() {
        return rt;
    }

    @Override
    public void setRt(RowTable rt) {
        this.rt = rt;
    }

    public void setN(String n) {
        this.n = n;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }


}
