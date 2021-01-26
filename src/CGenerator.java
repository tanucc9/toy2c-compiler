import java.util.ArrayList;
//EXPR: pos0 codiceC e pos1 idfunzione se è callproc o null
public class CGenerator implements Visitor{

    private ArrayList<ArrayList<RowTable>> typeEnvironment;
    private String fileC;
    private ArrayList<String> fileSplitted;
    private ArrayList<StructC> structMethod;
    public CGenerator () {
        this.typeEnvironment = new ArrayList<ArrayList<RowTable>>();
        fileC="";
        fileSplitted = new ArrayList<String>();
        structMethod= new ArrayList<StructC>();
    }

    private void enterScope(ArrayList<RowTable> table){
        this.typeEnvironment.add(table);
    }

    private ArrayList<RowTable> lookup(String symbol, String kind){
        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
            for(RowTable rowt : this.typeEnvironment.get(i)){
                if(rowt.getSymbol().equals(symbol) && rowt.getKind().equals(kind)) return this.typeEnvironment.get(i);
            }
        }
        return null;
    }

    private void addId(RowTable rt){
        this.typeEnvironment.get(this.typeEnvironment.size()-1).forEach(rowTable -> {
            if (rowTable.getSymbol().equals(rt.getSymbol()) && rowTable.getKind().equals(rt.getKind())){
                if(rt.getKind().equals("var")) throw new Error("La variabile "+ rt.getSymbol() +" è stata già dichiarata.");
                if(rt.getKind().equals("method")) throw new Error("La funzione "+ rt.getSymbol() +" è stata già dichiarata.");
            }
        });
        this.typeEnvironment.get(this.typeEnvironment.size()-1).add(rt);
    }
    private boolean probe(String symbol, String kind){
        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
            for(RowTable rowt : this.typeEnvironment.get(i)){
                if(rowt.getSymbol().equals(symbol) && rowt.getKind().equals(kind)) return true;
            }
        }
        return false;
    }
    private void exitScope(){
        this.typeEnvironment.remove(this.typeEnvironment.size()-1);
    }

    private String isCompatibleType(String operazione, String type1, String type2) {

        switch (operazione) {
            case "plus_operators":
                if(type1.equals("float") && type2.equals("float")) return "float";
                if(type1.equals("int") && type2.equals("int")) return "int";
                if(type1.equals("float") && type2.equals("int")) return "float";
                if(type1.equals("int") && type2.equals("float")) return "float";
                if(type1.equals("string") && type2.equals("string")) return "string";
                throw new Error("Non è possibile effettuare l'addizione tra "+ type1 +" e "+ type2);
            case "math_operators":
                if(type1.equals("float") && type2.equals("float")) return "float";
                if(type1.equals("int") && type2.equals("int")) return "int";
                if(type1.equals("float") && type2.equals("int")) return "float";
                if(type1.equals("int") && type2.equals("float")) return "float";
                throw new Error("Non è possibile effettuare l'operazione tra "+ type1 +" e "+ type2);
            case "boolean_operators":
                if(type1.equals("bool") && type2.equals("bool")) return "bool";
                else throw new Error("Non è possibile effettuare l'operazione booleana tra "+ type1 +" e "+ type2);
            case "boolean_not":
                String resNot = this.getResultNotType(type1);
                if(resNot!=null) return resNot;
                else throw new Error("Non è possibile effettuare l'operazione booleana con "+ type1);
            case "uminus":
                String res = this.getResultUminusType(type1);
                if (res != null)  return res;
                else throw new Error("Non è possibile aggiungere uminus con tipo "+ type1);
            case "relop":
                if(type1.equals(type2)) return "bool";
                if(type1.equals("float") && type2.equals("int")) return "bool";
                if(type1.equals("int") && type2.equals("float")) return "bool";
                else throw new Error("Non è possibile effettuare confrontare tra "+ type1 +" e "+ type2);

        }
        return null;
    }

    private String getResultUminusType(String type) {
        if (type.equals("int")) return "int";
        if(type.equals("float")) return "float";
        String[] resType = this.getStringSplitted(type, 1);
        if(resType.length == 1 && ( resType[0].equals("float") || resType[0].equals("int") )) return resType[0];

        return null;
    }
    private String getResultNotType(String type) {
        if (type.equals("bool")) return "bool";
        String[] resType = this.getStringSplitted(type, 1);
        if(resType.length == 1 && resType[0].equals("bool") ) return resType[0];

        return null;
    }

    private String[] getStringSplitted(String type, int index){
        String[] x= type.split("->");
        return type.split("->")[index].split(",");
    }

    @Override
    public Object visit(ProgramOP p) {
        this.fileSplitted.add("#include <stdio.h>\n"
                            + "#include <stdlib.h>\n"
                            + "#include <stdbool.h>\n"
                            + "#include <string.h> \n");

        this.enterScope(p.getGlobalTable());
        for (VarDeclOP var :p.getVarDeclList() ) {
            this.fileSplitted.set(0, this.fileSplitted.get(0)+(String) var.accept(this));
        }
        for (ProcOP var: p.getProcList()) {
            if(this.fileSplitted.get(2)!=null) this.fileSplitted.set(2, this.fileSplitted.get(2)+(String) var.accept(this));
            else this.fileSplitted.add((String) var.accept(this));
        }
        //TODO concatenamento
        if(!this.probe("main", "method")) throw new Error("Main mancante.");

        this.exitScope();

        return p;
    }

    @Override
    public Object visit(AndOP a) {
        RowTable expr1= (RowTable) a.getE().accept(this);
        RowTable expr2= (RowTable) a.getE1().accept(this);
        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'AND.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'AND.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("boolean_operators",type1, type2);
        a.getRt().setType(resultType);
        return a.getRt();
    }

    @Override
    public Object visit(AssignOP a) {
        ArrayList<RowTable> tableId;
        ArrayList<String> exprType= new ArrayList<String>();
        ArrayList<String> idType= new ArrayList<String>();
        for(Id id : a.getIlist()) {
            tableId= this.lookup(id.getId(), "var") ;
            if(tableId!=null) {
                for (RowTable idrt : tableId) {
                    if (idrt.getSymbol().equals(id.getId()) && idrt.getKind().equals("var")) idType.add(idrt.getType());
                }
            }else throw new Error ("La variabile "+ id.getId() +" non è stata dichiarata.");
        }
        for(Expr e : a.getElist()) {
            RowTable rt= (RowTable) e.accept(this);
            if(rt.getKind() != null && rt.getKind().equals("method")) {
                ArrayList<RowTable> table = this.lookup(e.getCp().getVal(), "method");
                if(table !=null) {
                    for(RowTable row : table) {
                        if(row.getSymbol().equals(e.getCp().getVal()) && row.getKind().equals("method")) e.setRt(row);
                    }
                    String[] resType = this.getStringSplitted(e.getRt().getType(), 1);

                    for(String type : resType) {
                        exprType.add(type);
                    }
                } else throw new Error ("Il proc "+ e.getCp().getVal() +" non è stato dichiarato.");
            } else {
                exprType.add(rt.getType());
            }
        }
        if(exprType.size() != idType.size()) throw new Error("Il numero delle espressioni non corrisponde al numero delle variabili attese.");
        if (!exprType.equals(idType)) throw new Error(" I tipi delle espressioni assegnate non corrispondono a quelli attesi. ");

        return true;
    }

    @Override
    public Object visit(BodyOP b) {
        for(Stat s:b.getStatList()){
            boolean stat= (boolean) s.accept(this);
        }
        return true;
    }

    @Override
    public Object visit(CallProcOP cp) {
        String callProcNode=cp.getVal()+ " ( ";

        if(cp.getElist() != null ) {
            for(Expr e : cp.getElist()) {
                ArrayList<String> expr = (ArrayList<String>) e.accept(this);
                if(expr.get(1)!=null){
                    StructC structC=null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get(1))) structC = sc;
                    }
                    if(structC != null){
                        for(int i=0; i< structC.getIndex();i++){
                            callProcNode += structC.getNome()+".var"+i;
                            if(i!=structC.getIndex()-1)  callProcNode += ", ";
                        }

                    }else callProcNode += expr.get(0);
                }else{
                    callProcNode += expr.get(0);
                }
                if(cp.getElist().size()-1 != cp.getElist().indexOf(e)) callProcNode += ", ";
            }
        }
        callProcNode +=");";
        return callProcNode;
    }

    @Override
    public Object visit(DivOP d) {
        RowTable expr1= (RowTable) d.getE().accept(this);
        RowTable expr2= (RowTable) d.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla divisione.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla divisione.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("math_operators",type1, type2);

        d.getRt().setType(resultType);
        return d.getRt();
    }

    @Override
    public Object visit(ElifOP c) {
        RowTable rt= (RowTable) c.getE().accept(this);
        if(!rt.getType().equals("bool"))throw new Error("La condizione deve essere di tipo boolean");
        return (boolean) c.getsList().accept(this);
    }

    @Override
    public Object visit(ElseOP e) {
        return (boolean) e.getsList().accept(this);
    }

    @Override
    public Object visit(EqualsOP eq) {
        RowTable expr1= (RowTable) eq.getE().accept(this);
        RowTable expr2= (RowTable) eq.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'Equals.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'Equals.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        eq.getRt().setType(resultType);
        return eq.getRt();
    }

    @Override
    public Object visit(Expr e) {
        ArrayList<String> exprNode = new ArrayList<String>();
        String[] callProcSplitted;
        if(e.getCp() != null){
            String callProcNode= (String) e.getCp().accept(this);
            callProcSplitted=callProcNode.split(" ");
            exprNode.add(callProcNode);
            exprNode.add(callProcSplitted[0]);
            return exprNode;
        }

        return null;
    }

    @Override
    public Object visit(GreaterEqualsOP ge) {
        RowTable expr1= (RowTable) ge.getE().accept(this);
        RowTable expr2= (RowTable) ge.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al GreaterEquals.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);

            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al GreaterEquals.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        ge.getRt().setType(resultType);
        return ge.getRt();
    }

    @Override
    public Object visit(GreaterThanOP gt) {
        RowTable expr1= (RowTable) gt.getE().accept(this);
        RowTable expr2= (RowTable) gt.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);

            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al GreaterThan.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);

            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al GreaterThan.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        gt.getRt().setType(resultType);
        return gt.getRt();
    }

    @Override
    public Object visit(Id id) {
        ArrayList<RowTable> table = this.lookup(id.getId(), "var");
        if(table == null) throw new Error("La variabile " + id.getId() +" non è stata dichiarata");
        else {
            for (RowTable rt : table ) {
                if(rt.getSymbol().equals(id.getId()) && rt.getKind().equals("var")) return rt;
            }
        }

        return null;
    }

    @Override
    public Object visit(IdListInitOP x) {
        String idListInitNode = x.getId().getId();
        if(x.getExpr() != null) {
            idListInitNode += " = ";
            idListInitNode +=(String) x.getExpr().accept(this);
            //TODO metodo
        }
        return idListInitNode;
    }

    @Override
    public Object visit(IfOP c) {
        String ifNode="if (";
        //TODO expr
        RowTable rt= (RowTable)c.getE().accept(this);
        ifNode += "){\n";

        //TODO stat
        boolean bodyop=(boolean) c.getsList().accept(this);

        //TODO elif
        boolean accElif= false, accElse= false;

        for(ElifOP elif: c.getElList()){
            accElif =(boolean) elif.accept(this);

        }
        //TODO else
        if(c.getEl()!=null) {
            accElse =(boolean) c.getEl().accept(this);
        }
        return accElif && accElse;
    }

    @Override
    public Object visit(LessEqualsOP le) {
        RowTable expr1= (RowTable) le.getE().accept(this);
        RowTable expr2= (RowTable) le.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);

            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al LessEquals.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al LessEquals.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        le.getRt().setType(resultType);
        return le.getRt();


    }


    @Override
    public Object visit(LessThanOP lt) {
        RowTable expr1= (RowTable) lt.getE().accept(this);
        RowTable expr2= (RowTable) lt.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al LessThan.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al LessThan.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        lt.getRt().setType(resultType);
        return lt.getRt();
    }

    @Override
    public Object visit(MinusOP m) {
        RowTable expr1= (RowTable) m.getE().accept(this);
        RowTable expr2= (RowTable) m.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla sottrazione.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla sottrazione.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("math_operators",type1, type2);

        m.getRt().setType(resultType);
        return m.getRt();
    }

    @Override
    public Object visit(NotEqualsOP ne) {
        RowTable expr1= (RowTable) ne.getE().accept(this);
        RowTable expr2= (RowTable) ne.getE1().accept(this);
        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al NotEquals.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione al NotEquals.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("relop",type1, type2);

        ne.getRt().setType(resultType);
        return ne.getRt();
    }

    @Override
    public Object visit(NotOP n) {
        RowTable expr= (RowTable) n.getNe().accept(this);
        String resultType = this.isCompatibleType("boolean_not", expr.getType(), null);

        n.getRt().setType(resultType);
        return n.getRt();
    }

    @Override
    public Object visit(OrOP or) {
        RowTable expr1= (RowTable) or.getE().accept(this);
        RowTable expr2= (RowTable) or.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'OR.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come espressione all'OR.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("boolean_operators",type1, type2);

        or.getRt().setType(resultType);
        return or.getRt();
    }

    @Override
    public Object visit(ParDeclOP p) {
        String parDeclNode="";
        for(Id id : p.getIdList()) {
            parDeclNode += p.getType() +" ";
            if(p.getIdList().indexOf(id) == p.getIdList().size()-1) parDeclNode += id.getId();
            else parDeclNode += id.getId() + ", ";
        }
        return parDeclNode;
    }

    @Override
    public Object visit(PlusOP p) {
        RowTable expr1= (RowTable) p.getE().accept(this);
        RowTable expr2= (RowTable) p.getE1().accept(this);
        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1 ) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando all'addizione.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando all'addizione.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("plus_operators",type1, type2);

        p.getRt().setType(resultType);
        return p.getRt();
    }

    @Override
    public Object visit(ProcBodyOP pb) {
        String procBodyNode="";
        for (VarDeclOP var : pb.getVdList() ) {
            procBodyNode += (String) var.accept(this);
        }

        if(pb.getsList()!=null) {
            //TODO stat
            boolean bodyOP = (boolean) pb.getsList().accept(this);
        }
        ArrayList<String> returnType= new ArrayList<String>();
        if(pb.getRe() != null) {
            for(Expr e : pb.getRe()) {
                RowTable rt=(RowTable) e.accept(this);
                if(rt.getKind() != null && rt.getKind().equals("method")) {
                    ArrayList<RowTable> table = this.lookup(e.getCp().getVal(), "method");
                    if(table !=null) {
                        for(RowTable row : table) {
                            if(row.getSymbol().equals(e.getCp().getVal()) && row.getKind().equals("method")) e.setRt(row);
                        }
                        String[] resType = this.getStringSplitted(e.getRt().getType(), 1);
                        for (String s: resType ) {
                            returnType.add(s);
                        }
                    } else throw new Error ("Il proc "+ e.getCp().getVal() +" non è stato dichiarato.");
                } else {
                    returnType.add(rt.getType());
                }
            }
        }

        return returnType;
    }

    @Override
    public Object visit(ProcOP p) {
        String procNode="";
        if(p.getRtList().size()==1){
            procNode += p.getRtList().get(0);

        }else{
            String struct = "typedef struct { \n";

            for(int i=0; i< p.getRtList().size() ; i++){
                struct += p.getRtList().get(i) + " var"+i+";";
            }
            struct += "}"+p.getId().getId()+";\n";
            this.fileSplitted.set(0,this.fileSplitted.get(0) + struct);
            procNode += p.getId().getId();
            this.structMethod.add(new StructC(p.getId().getId(),p.getRtList().size()));
        }

        procNode += " " + p.getId().getId();
        if(p.getPdList() != null) {
            procNode += "(";
            for (ParDeclOP parDecl  : p.getPdList()) {
                procNode += (String) parDecl.accept(this);
                if(p.getPdList().indexOf(parDecl) != p.getPdList().size()-1) procNode += ", ";
            }
            procNode += ")";
            this.fileSplitted.set(1, this.fileSplitted.get(1)+procNode+ ";\n");
        }
        else {
            procNode += "()";
            this.fileSplitted.set(1, this.fileSplitted.get(1)+procNode+ ";\n");
        }

        procNode += "{\n";

        //TODO PROCBODY
        ArrayList<String> returnType=(ArrayList<String>) p.getProcBodyOP().accept(this);
        procNode += "}\n";

        return procNode;
    }

    @Override
    public Object visit(ReadOP c) {
        for(Id i:c.getIdList()){
            if(!this.probe(i.getId(), "var") ) throw new Error("La variabile " + i.getId() +" non è stata dichiarate");
        }
        return true;
    }

    @Override
    public Object visit(Stat s) {
        if(s.getCp() != null){
            RowTable rt= (RowTable) s.getCp().accept(this);
            return true;
        }

        return false;
    }

    @Override
    public Object visit(TimesOP t) {
        String timesOP;
        RowTable expr1= (RowTable) t.getE().accept(this);
        RowTable expr2= (RowTable) t.getE1().accept(this);

        String type1="", type2="";
        if(expr1.getKind() != null && expr1.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr1.getType(), 1);
            if(resType.length==1) type1=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla moltiplicazione.");
        } else{
            type1=expr1.getType();
        }
        if(expr2.getKind() != null && expr2.getKind().equals("method")){
            String[] resType = this.getStringSplitted(expr2.getType(), 1);
            if(resType.length==1) type2=resType[0];
            else throw new Error("C'è un problema sui tipi di ritorno della funzione passata come operando alla moltiplicazione.");
        } else{
            type2=expr2.getType();
        }
        String resultType = this.isCompatibleType("math_operators",type1, type2);

        t.getRt().setType(resultType);
        return t.getRt();
    }

    @Override
    public Object visit(UMinusOP u) {
        String uMinusNode= "-";
        ArrayList<String> expr = (ArrayList<String>) u.accept(this);
        uMinusNode += expr.get(0);
        return uMinusNode;
    }

    @Override
    public Object visit(VarDeclOP c) {
        String varDeclNode = c.getType();

        for(IdListInitOP idList : c.getIdListInit()) {
            varDeclNode += (String) idList.accept(this);
            if(c.getIdListInit().indexOf(idList)==c.getIdListInit().size()-1) varDeclNode += ";\n";
            else varDeclNode += ", ";
        }
        return varDeclNode;
    }

    @Override
    public Object visit(WhileOP c) {
        if(c.getsList1() != null ) {
            Object bd=c.getsList1().accept(this);
        }
        RowTable rt = (RowTable) c.getE().accept(this);
        if(rt.getType().equals("bool")){
            Object bd=c.getsList2().accept(this);
        }else throw new Error("Il tipo della condizione deve essere boolean");

        return true;
    }

    @Override
    public Object visit(WriteOP c) {
        for(Expr e : c.getExprList()) {
            RowTable rt= (RowTable) e.accept(this);
            if(rt.getKind() != null && rt.getKind().equals("method")) {
                ArrayList<RowTable> table = this.lookup(e.getCp().getVal(), "method");
                if(table !=null) {
                    for(RowTable row : table) {
                        if(row.getSymbol().equals(e.getCp().getVal()) && row.getKind().equals("method")) e.setRt(row);
                    }
                    String[] resType = this.getStringSplitted(e.getRt().getType(), 1);

                    if( resType.length == 1 && resType[0].equals("void"))  throw new Error ("Il proc "+ e.getCp().getVal() +" ha come tipo di ritorno void.");

                } else throw new Error ("Il proc "+ e.getCp().getVal() +" non è stato dichiarato.");
            }
        }
        return true;
    }

    @Override
    public Object visit(StringConst sc) {
        return sc.getS();
    }

    @Override
    public Object visit(IntConst ic) {
        return ic.getVal()+"";
    }

    @Override
    public Object visit(Bool b) {
        return "" + b.isB();
    }

    @Override
    public Object visit(Null c) {
        return "";
    }

    @Override
    public Object visit(FloatConst fc) {
        return fc.getF()+"";
    }
}
