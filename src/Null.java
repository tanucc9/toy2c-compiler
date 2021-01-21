public class Null extends Expr {
    private String n;
    public Null(){
        this.n="null";
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }


}
