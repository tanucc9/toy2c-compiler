public class Bool extends Expr {
    private boolean b;
    public Bool(boolean b){
        this.b=b;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }


    public Object accept(Visitor v){
        return v.visit(this);
    }

}
