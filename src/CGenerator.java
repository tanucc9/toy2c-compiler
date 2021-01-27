import java.lang.reflect.Array;
import java.util.ArrayList;



public class CGenerator implements Visitor{

    private ArrayList<ArrayList<RowTable>> typeEnvironment;
    private String fileC;
    private ArrayList<String> fileSplitted;
    private ArrayList<StructC> structMethod;
    private ProcOP currentProc;

    public CGenerator () {
        this.typeEnvironment = new ArrayList<ArrayList<RowTable>>();
        fileC="";
        fileSplitted = new ArrayList<String>();
        structMethod= new ArrayList<StructC>();
        this.currentProc = new ProcOP();
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

    private String getTypeInWriteOP (String type) {
        if (type.equals("int") || type.equals("bool")) {
            return "%d";
        } else if (type.equals("float")) {
            return "%f";
        } else if (type.equals("string")) {
            return "%s";
        }

        return null;
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
        ArrayList<String> expr1= (ArrayList<String>) a.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) a.getE1().accept(this);

        return expr1.get(0) + " && " + expr2.get(0);
    }

    @Override
    public Object visit(AssignOP a) {
        ArrayList<String> exprNode= new ArrayList<String>();
        ArrayList<String> idNode= new ArrayList<String>();
        for(Id id : a.getIlist()) {
            idNode.add(id.getId()+" = ");
        }
        for(Expr e : a.getElist()) {
            ArrayList<String> expr= (ArrayList<String>) e.accept(this);

            if(expr.get(1) != null){
                StructC structC = null;
                for(StructC sc: this.structMethod){
                    if(sc.getNome().equals(expr.get(1)+"_struct")) structC = sc;
                }
                if(structC != null){
                    for(int i=0; i< structC.getIndex();i++){
                        exprNode.add(structC.getNome()+".var"+i);
                    }
                } else exprNode.add(expr.get(0));
            } else {
                exprNode.add(expr.get(0));
            }
        }

        String assignNode = "";
        for(int i = 0; i<idNode.size(); i++) {
            assignNode += idNode.get(i) + exprNode.get(i) + ";\n";
        }

        return assignNode;
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
                        if(sc.getNome().equals(expr.get(1) + "_struct")) structC = sc;
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
        callProcNode +=")";
        return callProcNode;
    }

    @Override
    public Object visit(DivOP d) {
        ArrayList<String> expr1= (ArrayList<String>) d.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) d.getE1().accept(this);

        return expr1.get(0) + " / " + expr2.get(0);
    }

    @Override
    public Object visit(ElifOP c) {
        String elifNode = "else if (";
        ArrayList<String> expr= (ArrayList<String>) c.getE().accept(this);

        elifNode += expr.get(0);
        elifNode += ") {\n";

        elifNode += (String) c.getsList().accept(this);
        elifNode += "}\n";

        return elifNode;
    }

    @Override
    public Object visit(ElseOP e) {
        String elseNode = "else {\n";
        elseNode += (String) e.getsList().accept(this);
        elseNode += "}\n";

        return elseNode;
    }

    @Override
    public Object visit(EqualsOP eq) {
        ArrayList<String> expr1= (ArrayList<String>) eq.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) eq.getE1().accept(this);

        return expr1.get(0) + " == " + expr2.get(0);
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
        ArrayList<String> expr1= (ArrayList<String>) ge.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) ge.getE1().accept(this);

        return expr1.get(0) + " >= " + expr2.get(0);
    }

    @Override
    public Object visit(GreaterThanOP gt) {
        ArrayList<String> expr1= (ArrayList<String>) gt.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) gt.getE1().accept(this);

        return expr1.get(0) + " > " + expr2.get(0);
    }

    @Override
    public Object visit(Id id) {
        return id.getId();
    }

    @Override
    public Object visit(IdListInitOP x) {
        String idListInitNode = x.getId().getId();
        if(x.getExpr() != null) {
            idListInitNode += " = ";
            ArrayList<String> expr = (ArrayList<String>) x.getExpr().accept(this);
            idListInitNode += expr.get(0);
        }
        return idListInitNode;
    }

    @Override
    public Object visit(IfOP c) {
        String ifNode="if (";
        ArrayList<String> expr= (ArrayList<String>) c.getE().accept(this);
        ifNode += expr.get(0) + ") {\n";
        ifNode += (String) c.getsList().accept(this);
        ifNode += "}\n";

        for(ElifOP elif: c.getElList()){
            ifNode += (String) elif.accept(this);
        }

        if(c.getEl()!=null) {
            ifNode += (String) c.getEl().accept(this);
        }
        return ifNode;
    }

    @Override
    public Object visit(LessEqualsOP le) {
        ArrayList<String> expr1= (ArrayList<String>) le.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) le.getE1().accept(this);

        return expr1.get(0) + " <= " + expr2.get(0);
    }


    @Override
    public Object visit(LessThanOP lt) {
        ArrayList<String> expr1= (ArrayList<String>) lt.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) lt.getE1().accept(this);

        return expr1.get(0) + " < " + expr2.get(0);
    }

    @Override
    public Object visit(MinusOP m) {
        ArrayList<String> expr1= (ArrayList<String>) m.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) m.getE1().accept(this);

        return expr1.get(0) + " - " + expr2.get(0);
    }

    @Override
    public Object visit(NotEqualsOP ne) {
        ArrayList<String> expr1= (ArrayList<String>) ne.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) ne.getE1().accept(this);

        return expr1.get(0) + " != " + expr2.get(0);
    }

    @Override
    public Object visit(NotOP n) {
        ArrayList<String> expr= (ArrayList<String>) n.getNe().accept(this);

        return "! "+expr.get(0);
    }

    @Override
    public Object visit(OrOP or) {
        ArrayList<String> expr1= (ArrayList<String>) or.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) or.getE1().accept(this);

        return expr1.get(0) + " || " + expr2.get(0);
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
        ArrayList<String> expr1= (ArrayList<String>) p.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) p.getE1().accept(this);

        //TODO concatenzazione string strcat(...,..)
        return expr1.get(0) + " + " + expr2.get(0);
    }

    @Override
    public Object visit(ProcBodyOP pb) {
        String procBodyNode="";
        for (VarDeclOP var : pb.getVdList() ) {
            procBodyNode += (String) var.accept(this);
        }

        if(pb.getsList()!=null) {
            procBodyNode += (String) pb.getsList().accept(this);
        }

        if(pb.getRe() != null) {
            ArrayList<String> returnList = new ArrayList<String>();
            String structInstructions = "";
            for(Expr e : pb.getRe()) {
                ArrayList<String> expr =(ArrayList<String>) e.accept(this);
                if(expr.get(1)!=null){
                    StructC structC=null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get(1) + "_struct")) structC = sc;
                    }
                    if(structC != null){
                        structInstructions += structC.getNome() + " " + structC.getNome() + "Var = " + expr.get(0) + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            returnList.add(structC.getNome()+ "Var" +".var"+i);
                        }
                    } else returnList.add(expr.get(0));
                } else {
                    returnList.add(expr.get(0));
                }
            }

            if (! structInstructions.equals("")) procBodyNode += structInstructions;

            String returnC = "";

            if (returnList.size() == 1) {
                returnC += "return " + returnList.get(0) + ";";
            } else {
                String returnStructInstruction = "";
                String idProc = this.currentProc.id.getId();
                StructC structC = null;
                for(StructC sc: this.structMethod){
                    if(sc.getNome().equals( idProc + "_struct")) structC = sc;
                }
                if(structC != null) {
                    returnStructInstruction += structC.getNome() + " " + structC.getNome() + "Var;\n";
                    for(int i=0; i< structC.getIndex();i++){
                        returnStructInstruction += structC.getNome()+ "Var" +".var"+ i + " = " + returnList.get(i) + ";\n";
                    }
                    returnC += returnStructInstruction + "return " + structC.getNome() + "Var;\n";
                }
            }

            procBodyNode += returnC;
        }

        return procBodyNode;
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
            struct += "}"+p.getId().getId()+"_struct;\n";
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

        this.currentProc = p;
        procNode += (String) p.getProcBodyOP().accept(this);
        procNode += "}\n";

        return procNode;
    }

    @Override
    public Object visit(ReadOP c) {
        String readNode = "scanf(";
        for(Id i:c.getIdList()){
            //TODO cercare nelle tabelle il tipo
        }
        return true;
    }

    @Override
    public Object visit(Stat s) {
        if(s.getCp() != null) return (String) s.getCp().accept(this)+";\n";

        return null;
    }

    @Override
    public Object visit(TimesOP t) {
        ArrayList<String> expr1= (ArrayList<String>) t.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) t.getE1().accept(this);

        return expr1.get(0) + " * " + expr2.get(0);
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
        String whileNode = "";

        //TODO vedere uso di while
        if(c.getsList1() != null ) {
            whileNode += (String) c.getsList1().accept(this);
        }

        whileNode += "while (";
        ArrayList<String> expr = (ArrayList<String>) c.getE().accept(this);
        whileNode += expr.get(0);
        whileNode += ") {\n";

        whileNode += (String) c.getsList2().accept(this);
        whileNode += "}\n";

        return whileNode;
    }


    @Override
    public Object visit(WriteOP c) {
        String writeOp = "";
        ArrayList<String> stringNodes = new ArrayList<String>();
        ArrayList<String> exprNodes = new ArrayList<String>();
        for(Expr e : c.getExprList()) {
            ArrayList<String> expr= (ArrayList<String>) e.accept(this);
            if (e instanceof StringConst) {
                stringNodes.add(expr.get(0));
            } else {
                if (expr.get(1) != null ) {
                    String structInstructions = "";
                    StructC structC = null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get(1)+"_struct")) structC = sc;
                    }

                    String [] returnType = this.getStringSplitted(e.getRt().getType(), 1);
                    if (structC != null) {
                        structInstructions += structC.getNome() + " " + structC.getNome() + "Var = " + expr.get(0) + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            stringNodes.add(this.getTypeInWriteOP(returnType[i]));
                            exprNodes.add(structC.getNome()+ "Var" +".var"+i);
                        }
                    } else {
                        stringNodes.add(this.getTypeInWriteOP(returnType[0]));
                        exprNodes.add(expr.get(0));
                    }

                    if (! structInstructions.equals("")) writeOp += structInstructions;
                } else {
                    stringNodes.add(this.getTypeInWriteOP(e.getRt().getType()));
                    exprNodes.add(expr.get(0));
                }

            }
        }
        writeOp += "printf(\"";
        for (String s : stringNodes) {
            s = s.replaceAll("\"", "");
            writeOp += s;
        }
        writeOp += "\"";

        for (String expr : exprNodes) {
            writeOp += ", "+ expr;
        }

        writeOp += ");";

        return writeOp;
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
