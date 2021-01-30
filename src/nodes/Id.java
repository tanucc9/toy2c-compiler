package nodes;

import visitors.Visitor;

public class Id extends Expr{

    private String id;


    public Id(String id){
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object accept(Visitor v){
        return v.visit(this);
    }

}
