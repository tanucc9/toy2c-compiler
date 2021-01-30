package nodes;

import models.RowTable;

public class AssignSingle {
    Id id;
    Expr e;
    private RowTable rt;

    public AssignSingle(Id id, Expr e) {
        this.id = id;
        this.e = e;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Expr getE() {
        return e;
    }

    public void setE(Expr e) {
        this.e = e;
    }
}
