public class StringConst extends Expr{
    private String s;

    public StringConst(String s){
        this.s=s;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

}
