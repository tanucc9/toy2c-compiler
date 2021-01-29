import java.util.ArrayList;

public class SemanticAnalisys implements Visitor{

    private ArrayList<ArrayList<RowTable>> typeEnvironment;

    public SemanticAnalisys () {
        this.typeEnvironment = new ArrayList<ArrayList<RowTable>>();
    }

    private void enterScope(ArrayList<RowTable> table){
        this.typeEnvironment.add(table);
    }

    private ArrayList<RowTable> lookup(String symbol){
        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
            for(RowTable rowt : this.typeEnvironment.get(i)){
                if(rowt.getSymbol().equals(symbol)) return this.typeEnvironment.get(i);
            }
        }
        return null;
    }

    private void addId(RowTable rt){
        this.typeEnvironment.get(this.typeEnvironment.size()-1).forEach(rowTable -> {
            if (rowTable.getSymbol().equals(rt.getSymbol())){
                throw new Error(rt.getSymbol() +" è stato già dichiarato.");
            }
        });
        this.typeEnvironment.get(this.typeEnvironment.size()-1).add(rt);
    }

    private boolean probe(String symbol, String kind){
        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
              for(RowTable rowt : this.typeEnvironment.get(i)){
                  if(rowt.getSymbol().equals(symbol) && rowt.getKind().equals(kind)) return true;
                  else if (rowt.getSymbol().equals(symbol) && kind == null) return true;
              }
        }
        return false;
    }
    private void exitScope(){
        this.typeEnvironment.remove(this.typeEnvironment.size()-1);
    }

    private String isCompatibleType(String operazione, String type1, String type2) {
        String res = "";
        switch (operazione) {
            case "plus_operators":
                res = this.numberCompatibility(type1, type2);
                if(res != null) return res;
                else throw new Error("Non è possibile effettuare l'operazione tra "+ type1 +" e "+ type2);
            case "math_operators":
                res = this.numberCompatibility(type1, type2);
                if(res != null) return res;
                else throw new Error("Non è possibile effettuare l'operazione tra "+ type1 +" e "+ type2);
            case "boolean_operators":
                if(type1.equals("bool") && type2.equals("bool")) return "bool";
                else throw new Error("Non è possibile effettuare l'operazione booleana tra "+ type1 +" e "+ type2);
            case "boolean_not":
                String resNot = this.getResultNotType(type1);
                if(resNot!=null) return resNot;
                else throw new Error("Non è possibile effettuare l'operazione booleana con "+ type1);
            case "uminus":
                res = this.getResultUminusType(type1);
                if (res != null)  return res;
                else throw new Error("Non è possibile aggiungere uminus con tipo "+ type1);
            case "relop":
                if(type1.equals(type2)) return "bool";
                if(type1.equals("float") && type2.equals("int")) return "bool";
                if(type1.equals("int") && type2.equals("float")) return "bool";
                else throw new Error("Non è possibile effettuare confrontare tra "+ type1 +" e "+ type2);
            case "compatible_assign":
                res = this.numberCompatibility(type1, type2);
                if(res != null) return res;
                else throw new Error("I tipi delle espressioni assegnate non corrispondono a quelli attesi.");
        }
        return null;
    }

    private String numberCompatibility (String type1, String type2) {
        if(type1.equals("float") && type2.equals("float")) return "float";
        if(type1.equals("int") && type2.equals("int")) return "int";
        if(type1.equals("float") && type2.equals("int")) return "float";
        if(type1.equals("int") && type2.equals("float")) return "float";

        return null;
    }

    private boolean isCompatibleVarInitType(String type1, String type2){
        if((type1.equals("string") && type2.equals("string")) || (type1.equals("bool") && type2.equals("bool"))) return true;
        if((type1.equals("int") && type2.equals("int")) || (type1.equals("int") && type2.equals("float")) ) return true;
        if((type1.equals("float") && type2.equals("float")) || (type1.equals("float") && type2.equals("int")) ) return true;
        return false;
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
        this.enterScope(p.getGlobalTable());
        for (VarDeclOP var :p.getVarDeclList() ) {
            boolean acc = ((boolean) var.accept(this));
        }
        for (ProcOP var: p.getProcList()) {
            boolean acc = ((boolean) var.accept(this));
        }
        if(!this.probe("main", "method")) throw new Error("Non è stata implementata la funzione 'main'.");

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
            tableId= this.lookup(id.getId()) ;
            if(tableId!=null) {
                for (RowTable idrt : tableId) {
                    if (idrt.getSymbol().equals(id.getId())) idType.add(idrt.getType());
                }
            }else throw new Error ("La variabile "+ id.getId() +" non è stata dichiarata.");
        }
        for(Expr e : a.getElist()) {
            RowTable rt= (RowTable) e.accept(this);
            if(rt.getKind() != null && rt.getKind().equals("method")) {
                ArrayList<RowTable> table = this.lookup(e.getCp().getVal());
                if(table !=null) {
                    for(RowTable row : table) {
                        if(row.getSymbol().equals(e.getCp().getVal())) e.setRt(row);
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

        if(!idType.equals(exprType)) {
            //Fa il controllo nel caso tipi diversi, ma compatibili
            for (int i = 0; i < idType.size(); i++) {
                if (this.isCompatibleType("compatible_assign", idType.get(i), exprType.get(i)) == null)
                    throw new Error(" I tipi delle espressioni assegnate non corrispondono a quelli attesi. ");
            }
        }

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
        ArrayList<RowTable> tableCp = this.lookup(cp.getVal());
        if(tableCp !=null) {
            for(RowTable row : tableCp) {
                if(row.getSymbol().equals(cp.getVal())) cp.setRt(row);
            }
        } else throw new Error ("Il proc "+ cp.getVal() +" non è stato dichiarato.");

        if(cp.getElist() != null ) {
            ArrayList<String> parType= new ArrayList<String>();
            for(Expr e : cp.getElist()) {
                RowTable rt = (RowTable) e.accept(this);
                if(rt.getKind() != null && rt.getKind().equals("method")) {
                    ArrayList<RowTable> table = this.lookup(e.getCp().getVal());
                    if(table !=null) {
                        for(RowTable row : table) {
                            if(row.getSymbol().equals(e.getCp().getVal())) e.setRt(row);
                        }
                        String[] resType = this.getStringSplitted(e.getRt().getType(), 1);
                        for(String type : resType) {
                            parType.add(type);
                        }
                    } else throw new Error ("Il proc "+ e.getCp().getVal() +" non è stato dichiarato.");
                } else {
                    parType.add(rt.getType());
                }
            }

           String[] parTypeCp = this.getStringSplitted(cp.getRt().getType(), 0);

            ArrayList<String> parTypeCpList = new ArrayList<String>();
            for (String s : parTypeCp) {
                parTypeCpList.add(s);
            }

            if(parType.size() != parTypeCpList.size()) throw new Error("Il numero dei parametri passati al proc "+ cp.getVal() +" non corrisponde al numero dei parametri attesi.");
            if (!parType.equals(parTypeCpList)) throw new Error(" I tipi dei parametri passati al proc "+ cp.getVal() +" non corrispondono a quelli attesi. ");

        }
        return cp.getRt();
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
        if(e.getCp() != null){
            RowTable rt= (RowTable) e.getCp().accept(this);
            return rt;
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
        ArrayList<RowTable> table = this.lookup(id.getId());
        if(table == null) throw new Error("La variabile " + id.getId() +" non è stata dichiarata");
        else {
            for (RowTable rt : table ) {
                if(rt.getSymbol().equals(id.getId())) {
                    id.setRt(rt);
                    return id.getRt();
                }
            }
        }

        return null;
    }

    @Override
    public Object visit(IdListInitOP x) {
        String id =x.getId().getId();
        if(x.getExpr() != null) {
            RowTable r=(RowTable) x.getExpr().accept(this);
            if(r.getKind() != null && r.getKind().equals("method")){
                String[] resType = this.getStringSplitted(r.getType(), 1);
                if(resType.length==1) {
                    x.getRt().setSymbol(id);
                    x.getRt().setType(resType[0]);
                }
                else throw new Error("La funzione ritorna un numero di valori non atteso.");
            } else{
                x.getRt().setSymbol(id);
                x.getRt().setType(r.getType());
            }

        }
        else {
            x.getRt().setSymbol(id);
        }
        return x.getRt();
    }

    @Override
    public Object visit(IfOP c) {
        RowTable rt= (RowTable)c.getE().accept(this);
        if(rt.getKind() != null && rt.getKind().equals("method")) {
            String [] resType = this.getStringSplitted(rt.getType(), 1);
            if(resType.length != 1 || !resType[0].equals("bool")) throw new Error("Il tipo della condizione deve essere boolean ");
        } else if(!rt.getType().equals("bool"))throw new Error("Il tipo della condizione deve essere boolean ");



        boolean bodyop=(boolean) c.getsList().accept(this);
        boolean accElif= false, accElse= false;
        for(ElifOP elif: c.getElList()){
            accElif =(boolean) elif.accept(this);

        }
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
        ArrayList<RowTable> rt= new ArrayList<RowTable>();
        for(Id id : p.getIdList()) {
            RowTable rowt= new RowTable();
            String symbol= id.getId();
            rowt.setType(p.getType());
            rowt.setSymbol(symbol);
            rowt.setKind("var");
            rt.add(rowt);
        }
        return rt;
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
        for (VarDeclOP var : pb.getVdList() ) {
            boolean acc= (boolean) var.accept(this);
        }

        if(pb.getsList()!=null) {
            boolean bodyOP = (boolean) pb.getsList().accept(this);
        }
        ArrayList<String> returnType= new ArrayList<String>();
        if(pb.getRe() != null) {
            for(Expr e : pb.getRe()) {
                RowTable rt=(RowTable) e.accept(this);
                if(rt.getKind() != null && rt.getKind().equals("method")) {
                    ArrayList<RowTable> table = this.lookup(e.getCp().getVal());
                    if(table !=null) {
                        for(RowTable row : table) {
                            if(row.getSymbol().equals(e.getCp().getVal())) e.setRt(row);
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
        String idProc=p.getId().getId();
        ArrayList<RowTable> parDeclOP= new ArrayList<RowTable>();
        if(p.getPdList() != null) {
            for (ParDeclOP parDecl  : p.getPdList()) {
                parDeclOP.addAll( (ArrayList<RowTable>) parDecl.accept(this) );
            }
        }

        String resType = "->";
        for (String s: p.getRtList()) {
            resType=resType.concat(s+",");
        }
        String parListType= "";

        for (RowTable rt : parDeclOP) {
            parListType = parListType.concat(rt.getType()+",");
        }

        parListType = parListType.concat(resType);

        p.getRowT().setType(parListType);
        p.getRowT().setKind("method");
        p.getRowT().setSymbol(idProc);

       String[] resType2 = this.getStringSplitted(p.getRowT().getType(), 1);
        if(resType2.length > 1){
            for(String s: resType2){
                if (s.equals("void")) throw  new Error("E' stato utilizzato il tipo void in una lista di tipi di ritorno.");
            }
        }
        this.addId(p.getRowT());


        this.enterScope(p.getLocalTable());

        parDeclOP.forEach(rowTable -> {
            this.addId(rowTable);
        });


        ArrayList<String> returnType=(ArrayList<String>) p.getProcBodyOP().accept(this);


        ArrayList<String> resultType= new ArrayList<String>();
        for(String s: resType2){
            resultType.add(s);
        }

        if( ! (returnType.size() == 0 && resultType.size() ==1 && resultType.get(0).equals("void"))) {
            if(resultType.size() != returnType.size()) throw new Error("Il numero dei valori ritornati non corrisponde a quello atteso.");
            if (!resultType.equals(returnType)) throw new Error(" I tipi dei valori ritornati da "+ p.getId().getId() +" non corrispondono a quelli attesi. ");
        }

        this.exitScope();

        return true;
    }

    @Override
    public Object visit(ReadOP c) {
        for(Id i:c.getIdList()){
            ArrayList<RowTable> table = this.lookup(i.getId());
            if(table !=null) {
                for(RowTable row : table) {
                    if(row.getSymbol().equals(i.getId())) i.setRt(row);
                }

            } else throw new Error ("La variabile "+ i.getId()+" non è stata dichiarata.");
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
        RowTable rt = (RowTable) u.getE().accept(this);
        String res = this.isCompatibleType("uminus", rt.getType(), null);
        u.getRt().setType(res);

        return u.getRt();
    }

    @Override
    public Object visit(VarDeclOP c) {

         for(IdListInitOP idList : c.getIdListInit()) {
             idList.setRt((RowTable) idList.accept(this));
             idList.getRt().setKind("var");
             if(idList.getRt().getType()==null){
                 idList.getRt().setType(c.getType());
                 this.addId(idList.getRt());
             }
             else if(this.isCompatibleVarInitType(c.getType(), idList.getRt().getType())){
                 idList.getRt().setType(c.getType());
                 this.addId(idList.getRt());
             }else throw new Error("Il tipo di "+idList.getRt().getSymbol()+" non è compatibile con "+ idList.getRt().getType());
        }
        return true;
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
                ArrayList<RowTable> table = this.lookup(e.getCp().getVal());
                if(table !=null) {
                    for(RowTable row : table) {
                        if(row.getSymbol().equals(e.getCp().getVal()) ) e.setRt(row);
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
        sc.getRt().setType("string");
        return sc.getRt();
    }

    @Override
    public Object visit(IntConst ic) {
        ic.getRt().setType("int");
        return ic.getRt();
    }

    @Override
    public Object visit(Bool b) {
        if(b.isB()) b.getRt().setType("bool");
        else b.getRt().setType("bool");
        return b.getRt();
    }

    @Override
    public Object visit(Null c) {
        c.getRt().setType("string");
        return c.getRt();
    }

    @Override
    public Object visit(FloatConst fc) {
        fc.getRt().setType("float");
        return fc.getRt();
    }
}
