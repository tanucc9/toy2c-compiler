public class FloatConst extends Expr{
    private float f;
    private RowTable rt = new RowTable();

    public FloatConst(float f){
        this.f=f;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
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
