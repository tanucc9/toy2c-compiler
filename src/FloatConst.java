public class FloatConst extends Expr{
    private float f;
    private RowTable rt;

    public FloatConst(float f){
        this.f=f;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }

}
