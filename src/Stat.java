public class Stat {
    private CallProcOP cp;
    private RowTable rt;

    public Stat(CallProcOP cp) {
        this.cp=cp;
    }
    public Stat() {}

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
