public class Stat {
    private CallProcOP cp;
    private RowTable rt= new RowTable();

    public Stat(CallProcOP cp) {
        this.cp=cp;
    }
    public Stat() {}

    public RowTable getRt() {
        return rt;
    }

    public void setRt(RowTable rt) {
        this.rt = rt;
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
