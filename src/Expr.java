public class Expr {


   private CallProcOP cp;
    public Expr(){}


    public Expr (CallProcOP cp) {
        this.cp=cp;
    }


    public CallProcOP getCp() {
        return cp;
    }

    public void setCp(CallProcOP cp) {
        this.cp = cp;
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }
}
