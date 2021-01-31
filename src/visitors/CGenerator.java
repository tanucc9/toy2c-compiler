package visitors;

import models.RowTable;
import nodes.*;
import models.*;


import java.util.ArrayList;

public class CGenerator implements Visitor {

    private ArrayList<ArrayList<RowTable>> typeEnvironment;
    private String fileC;
    private String globalAssignVar;
    private String procDecl;
    private String structDecl;
    private String procImpl;
    private String typeVarDecl;
    private int indexWriteStruct;
    private int indexAssignStruct;
    private int indexResultStruct;
    private int indexParamCallProcStruct;
    private ArrayList<String> fileSplitted;
    private ArrayList<StructC> structMethod;
    private ProcOP currentProc;
    private boolean isGlobalVar;

    public CGenerator () {
        this.typeEnvironment = new ArrayList<ArrayList<RowTable>>();
        fileC="";
        procDecl="";
        structDecl="";
        procImpl="";
        this.typeVarDecl="";
        indexWriteStruct=0;
        indexAssignStruct=0;
        indexResultStruct=0;
        this.indexParamCallProcStruct = 0;
        fileSplitted = new ArrayList<String>();
        structMethod= new ArrayList<StructC>();
        this.currentProc = new ProcOP();
        this.isGlobalVar = false;
        this.globalAssignVar = "";
    }

    private String[] getStringSplitted(String type, int index){
        String[] x= type.split("->");
        return type.split("->")[index].split(",");
    }

    private String getTypeInWR (String type, String op) {
        if (type.equals("int") || type.equals("bool")) {
            return "%d";
        } else if (type.equals("float")) {
            return "%f";
        } else if (type.equals("string") && op.equals("read")) {
            return " %[^\\n]s";
        } else if (type.equals("string") && op.equals("write")) {
            return "%s";
        }
        return null;
    }

    @Override
    public Object visit(ProgramOP p) {
        this.fileC += "#include <stdio.h>\n"
                + "#include <stdlib.h>\n"
                + "#include <stdbool.h>\n"
                + "#include <string.h> \n\n";

        this.isGlobalVar = true;
        for (VarDeclOP var :p.getVarDeclList() ) {
            this.fileC += (String) var.accept(this);
        }
        this.isGlobalVar = false;

        for (ProcOP var: p.getProcList()) {
            this.procImpl += "\n" + (String) var.accept(this);
        }

        this.fileC += "\n" + structDecl + "\n" + procDecl + "\n" + procImpl;
        System.out.println(this.fileC);
        return this.fileC;
    }

    @Override
    public Object visit(AndOP a) {
        ArrayList<String> expr1= (ArrayList<String>) a.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) a.getE1().accept(this);
        ArrayList<String> andNode= new ArrayList<String>();
        andNode.add(expr1.get(0) + " && " + expr2.get(0));
        andNode.add(null);
        return andNode;
    }

    @Override
    public Object visit(AssignOP a) {
        ArrayList<String> exprNode= new ArrayList<String>();
        ArrayList<String> idNode= new ArrayList<String>();
        String structInstructions = "";
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
                    structInstructions += structC.getNome() + " " + structC.getNome() + "Var" + this.indexAssignStruct + " = " + expr.get(0) + ";\n";
                    for(int i=0; i< structC.getIndex();i++){
                        exprNode.add(structC.getNome()+"Var" + this.indexAssignStruct + ".var"+i);
                    }
                    this.indexAssignStruct++;
                } else exprNode.add(expr.get(0));
            } else {
                exprNode.add(expr.get(0));
            }
        }

        String assignNode = structInstructions.equals("") ? "" : structInstructions ;
        for(int i = 0; i<idNode.size(); i++) {
            assignNode += idNode.get(i) + exprNode.get(i) + ";\n";
        }

        return assignNode;
    }

    @Override
    public Object visit(BodyOP b) {
        String bodyOpNode="";
        for(Stat s:b.getStatList()){
            String stat = (String) s.accept(this);
            bodyOpNode += stat;
        }
        return bodyOpNode;
    }

    @Override
    public Object visit(CallProcOP cp) {
        String callProcNode=cp.getVal()+ " ( ";
        String structInstructions = "";
        if(cp.getElist() != null ) {
            for(Expr e : cp.getElist()) {
                ArrayList<String> expr = (ArrayList<String>) e.accept(this);
                if(expr.get(1)!=null){
                    StructC structC=null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get(1) + "_struct")) structC = sc;
                    }
                    if(structC != null){
                        structInstructions += structC.getNome() + " " + structC.getNome() + "ParamCP" + this.indexParamCallProcStruct + " = " + expr.get(0) + ";\n";
                        callProcNode = structInstructions + callProcNode;
                        for(int i=0; i< structC.getIndex();i++){
                            callProcNode += structC.getNome() + "ParamCP" + this.indexParamCallProcStruct + ".var"+i;
                            if(i!=structC.getIndex()-1)  callProcNode += ", ";
                        }
                        this.indexParamCallProcStruct++;

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
        ArrayList<String> divNode= new ArrayList<String>();
        divNode.add(expr1.get(0) + " / " + expr2.get(0));
        divNode.add(null);

        return divNode;
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
        ArrayList<String> eqNode= new ArrayList<String>();
        eqNode.add(expr1.get(0) + " == " + expr2.get(0));
        eqNode.add(null);
        return eqNode;
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
        ArrayList<String> geNode= new ArrayList<String>();
        geNode.add(expr1.get(0) + " >= " + expr2.get(0));
        geNode.add(null);


        return geNode;
    }

    @Override
    public Object visit(GreaterThanOP gt) {
        ArrayList<String> expr1= (ArrayList<String>) gt.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) gt.getE1().accept(this);
        ArrayList<String> gtNode= new ArrayList<String>();
        gtNode.add(expr1.get(0) + " > " + expr2.get(0));
        gtNode.add(null);


        return gtNode;
    }

    @Override
    public Object visit(Id id) {
        ArrayList<String> idNode= new ArrayList<String>();
        idNode.add(id.getId());
        idNode.add(null);

        return idNode;
    }

    @Override
    public Object visit(IdListInitOP x) {
        String idListInitNode = this.typeVarDecl.equals("string") ? "*" + x.getId().getId() : x.getId().getId();
        if(x.getExpr() != null) {
            ArrayList<String> expr = (ArrayList<String>) x.getExpr().accept(this);
            if (this.isGlobalVar) {
                 this.globalAssignVar += x.getId().getId() + " = " + expr.get(0) +";\n";
            } else {
                idListInitNode += " = ";
                idListInitNode += expr.get(0);
            }
        }
        return idListInitNode;
    }

    @Override
    public Object visit(IfOP c) {
        String ifNode= "if (";
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
        ArrayList<String> leNode= new ArrayList<String>();
        leNode.add(expr1.get(0) + " <= " + expr2.get(0));
        leNode.add(null);
        return leNode;
    }


    @Override
    public Object visit(LessThanOP lt) {
        ArrayList<String> expr1= (ArrayList<String>) lt.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) lt.getE1().accept(this);
        ArrayList<String> ltNode= new ArrayList<String>();
        ltNode.add(expr1.get(0) + " < " + expr2.get(0));
        ltNode.add(null);
        return ltNode;
    }

    @Override
    public Object visit(MinusOP m) {
        ArrayList<String> expr1= (ArrayList<String>) m.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) m.getE1().accept(this);
        ArrayList<String> minusNode= new ArrayList<String>();
        minusNode.add(expr1.get(0) + " - " + expr2.get(0));
        minusNode.add(null);

        return minusNode;
    }

    @Override
    public Object visit(NotEqualsOP ne) {
        ArrayList<String> expr1= (ArrayList<String>) ne.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) ne.getE1().accept(this);
        ArrayList<String> neNode= new ArrayList<String>();
        neNode.add(expr1.get(0) + " != " + expr2.get(0));
        neNode.add(null);
        return neNode ;
    }

    @Override
    public Object visit(NotOP n) {
        ArrayList<String> expr= (ArrayList<String>) n.getNe().accept(this);
        ArrayList<String> notNode= new ArrayList<String>();
        notNode.add("!"+expr.get(0));
        notNode.add(null);

        return notNode;
    }

    @Override
    public Object visit(OrOP or) {
        ArrayList<String> expr1= (ArrayList<String>) or.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) or.getE1().accept(this);
        ArrayList<String> orNode= new ArrayList<String>();
        orNode.add(expr1.get(0) + " || " + expr2.get(0));
        orNode.add(null);
        return orNode;
    }

    @Override
    public Object visit(ParDeclOP p) {
        String parDeclNode="";
        String type=p.getType().equals("string") ? "char*" : p.getType();
        for(Id id : p.getIdList()) {
            parDeclNode += type +" ";
            if(p.getIdList().indexOf(id) == p.getIdList().size()-1) parDeclNode += id.getId();
            else parDeclNode += id.getId() + ", ";
        }
        return parDeclNode;
    }

    @Override
    public Object visit(PlusOP p) {
        ArrayList<String> expr1= (ArrayList<String>) p.getE().accept(this);
        ArrayList<String> expr2= (ArrayList<String>) p.getE1().accept(this);
        ArrayList<String> plusNode= new ArrayList<String>();

        plusNode.add(expr1.get(0) + " + " + expr2.get(0));
        plusNode.add(null);

        return plusNode;
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
                        structInstructions += structC.getNome() + " " + structC.getNome() + "VarRes" + this.indexResultStruct + " = " + expr.get(0) + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            returnList.add(structC.getNome()+ "VarRes" + this.indexResultStruct + ".var"+i);
                        }
                        this.indexResultStruct++;
                    } else returnList.add(expr.get(0));
                } else {
                    returnList.add(expr.get(0));
                }
            }

            if (! structInstructions.equals("")) procBodyNode += structInstructions;

            String returnC = "";

            if (returnList.size() == 1) {
                returnC += "return " + returnList.get(0) + ";\n";
            } else {
                String returnStructInstruction = "";
                String idProc = this.currentProc.getId().getId();
                StructC structC = null;
                for(StructC sc: this.structMethod){
                    if(sc.getNome().equals( idProc + "_struct")) structC = sc;
                }
                if(structC != null) {
                    returnStructInstruction += structC.getNome() + " " + structC.getNome() + "Return;\n";
                    for(int i=0; i< structC.getIndex();i++){
                        returnStructInstruction += structC.getNome()+ "Return" +".var"+ i + " = " + returnList.get(i) + ";\n";
                    }
                    returnC += returnStructInstruction + "return " + structC.getNome() + "Return;\n";
                }
            }

            procBodyNode += returnC;
        }else {
            if(this.currentProc.getId().getId().equals("main")) procBodyNode += "return 0;\n";
        }

        return procBodyNode;
    }

    @Override
    public Object visit(ProcOP p) {
        String procNode="";
        if(p.getRtList().size()==1){
            if(p.getRtList().get(0).equals("string")) procNode += "char *";
            else if(p.getId().getId().equals("main")) procNode += "int";
            else procNode += p.getRtList().get(0);

        }else{
            String struct = "typedef struct { \n";

            for(int i=0; i< p.getRtList().size() ; i++){
                if (p.getRtList().get(i).equals("string")) struct += "char *" + " var"+i+";\n";
                else struct += p.getRtList().get(i) + " var"+i+";\n";
            }
            struct += "} "+p.getId().getId()+"_struct;\n";
            this.structDecl += struct;
            procNode += p.getId().getId() + "_struct";
            this.structMethod.add(new StructC(p.getId().getId() + "_struct" ,p.getRtList().size()));
        }

        procNode += " " + p.getId().getId();
        if(p.getPdList() != null) {
            procNode += "(";
            for (ParDeclOP parDecl  : p.getPdList()) {
                procNode += (String) parDecl.accept(this);
                if(p.getPdList().indexOf(parDecl) != p.getPdList().size()-1) procNode += ", ";
            }
            procNode += ")";
            if( ! p.getId().getId().equals("main")) this.procDecl += procNode+ ";\n";
        }
        else {
            procNode += "()";
            if( ! p.getId().getId().equals("main")) this.procDecl += procNode+ ";\n";
        }

        procNode += "{\n";

        if( p.getId().getId().equals("main")) procNode += this.globalAssignVar;

        this.currentProc = p;
        procNode += (String) p.getProcBodyOP().accept(this);
        procNode += "}\n";

        return procNode;
    }

    /*
    char s1[100];
    scanf("%s", s1);
    s = s1;
    */
    @Override
    public Object visit(ReadOP c) {
        String idlist ="";
        String charInstrBefore = "";
        String charInstrAfter = "";
        String readNode = "";

        for(Id i:c.getIdList()){
            readNode += "scanf(\"" + this.getTypeInWR(i.getRt().getType(), "read");
            if(i.getRt().getType().equals("string")) {
                charInstrBefore += "char " + i.getId() +"_tempVarReadString[100];\n";
                idlist = ", " + i.getId() + "_tempVarReadString";
                charInstrAfter += i.getId() + " = " + i.getId() + "_tempVarReadString;\n";
            } else idlist = ", &" + i.getId();
            readNode += "\"" + idlist;
            readNode += ");\n";
        }

        readNode = charInstrBefore + readNode + charInstrAfter;

        return readNode;
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
        ArrayList<String> timesNode = new ArrayList<String>();
        timesNode.add(expr1.get(0) + " * " + expr2.get(0));
        timesNode.add(null);

        return timesNode;
    }

    @Override
    public Object visit(UMinusOP u) {
        ArrayList<String> uMinusNode= new ArrayList<String>();
        ArrayList<String> expr = (ArrayList<String>) u.getE().accept(this);
        uMinusNode.add("-"+expr.get(0));
        uMinusNode.add(null);

        return uMinusNode;
    }

    @Override
    public Object visit(VarDeclOP c) {

        String varDeclNode = c.getType().equals("string") ? "char " : c.getType() + " ";
        this.typeVarDecl=c.getType();
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
                        structInstructions += structC.getNome() + " " + structC.getNome() + "VarWrite" + this.indexWriteStruct + " = " + expr.get(0) + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            stringNodes.add(this.getTypeInWR(returnType[i], "write"));
                            exprNodes.add(structC.getNome()+ "VarWrite" + this.indexWriteStruct + ".var"+i);
                        }
                        this.indexWriteStruct++;
                    } else {
                        stringNodes.add(this.getTypeInWR(returnType[0], "write"));
                        exprNodes.add(expr.get(0));
                    }

                    if (! structInstructions.equals("")) writeOp += structInstructions;
                } else {
                    stringNodes.add(this.getTypeInWR(e.getRt().getType(), "write"));
                    exprNodes.add(expr.get(0));
                }

            }
        }
        writeOp += "printf(\"";
        for (String s : stringNodes) {
            s = s.replace("\n", "\\n");
            s = s.replace("\t", "\\t");
            s = s.replace("\r", "\\r");
            s = s.replace("\"", "");

            writeOp += s + " ";
        }
        writeOp += "\"";

        for (String expr : exprNodes) {
            writeOp += ", "+ expr;
        }

        writeOp += ");\n";

        return writeOp;
    }

    @Override
    public Object visit(StringConst sc) {
        ArrayList<String> stringConstNode= new ArrayList<String>();
        stringConstNode.add("\"" + sc.getS() + "\"");
        stringConstNode.add(null);

        return stringConstNode;
    }

    @Override
    public Object visit(IntConst ic) {
        ArrayList<String> intConstNode= new ArrayList<String>();
        intConstNode.add(ic.getVal()+"");
        intConstNode.add(null);

        return intConstNode;
    }

    @Override
    public Object visit(Bool b) {
        ArrayList<String> boolNode= new ArrayList<String>();
        boolNode.add(""+b.isB());
        boolNode.add(null);

        return boolNode;
    }

    @Override
    public Object visit(Null c) {
        ArrayList<String> nullNode= new ArrayList<String>();
        nullNode.add("\"\"");
        nullNode.add(null);
        return nullNode;
    }

    @Override
    public Object visit(FloatConst fc) {
        ArrayList<String> floatNode= new ArrayList<String>();
        floatNode.add(fc.getF()+"");
        floatNode.add(null);
        return floatNode;
    }
}
