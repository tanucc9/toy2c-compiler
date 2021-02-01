package visitors;

import models.RowTable;
import nodes.*;
import models.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private int countElifOP;

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
        this.countElifOP = 0;
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
        //System.out.println(this.fileC);
        return this.fileC;
    }

    @Override
    public Object visit(AndOP a) {
        Map<String, String> expr1= (Map<String, String>) a.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) a.getE1().accept(this);
        Map<String, String> andNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        andNode.put("code", expr1.get("code") + " && " + expr2.get("code"));
        if( ! structInstructions.equals("")) andNode.put("serviceInstr", structInstructions);
       // andNode.add(null);
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
            Map<String, String> expr= (Map<String, String>) e.accept(this);
            if (expr.containsKey("serviceInstr")) structInstructions += expr.get("serviceInstr");
            if(expr.containsKey("idProc")){
                StructC structC = null;
                for(StructC sc: this.structMethod){
                    if(sc.getNome().equals(expr.get("idProc")+"_struct")) structC = sc;
                }
                if(structC != null){
                    structInstructions += structC.getNome() + " " + structC.getNome() + "Var" + this.indexAssignStruct + " = " + expr.get("code") + ";\n";
                    for(int i=0; i< structC.getIndex();i++){
                        exprNode.add(structC.getNome()+"Var" + this.indexAssignStruct + ".var"+i);
                    }
                    this.indexAssignStruct++;
                } else exprNode.add(expr.get("code"));
            } else {
                exprNode.add(expr.get("code"));
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
                Map<String, String> expr = (Map<String, String>) e.accept(this);
                if(expr.containsKey("serviceInstr")) structInstructions = expr.get("serviceInstr");
                if(expr.containsKey("idProc")){
                    StructC structC=null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get("idProc") + "_struct")) structC = sc;
                    }
                    if(structC != null){
                        structInstructions += structC.getNome() + " " + structC.getNome() + "ParamCP" + this.indexParamCallProcStruct + " = " + expr.get("code") + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            callProcNode += structC.getNome() + "ParamCP" + this.indexParamCallProcStruct + ".var"+i;
                            if(i!=structC.getIndex()-1)  callProcNode += ", ";
                        }
                        this.indexParamCallProcStruct++;

                    }else callProcNode += expr.get("code");
                }else{
                    callProcNode += expr.get("code");
                }
                if(cp.getElist().size()-1 != cp.getElist().indexOf(e)) callProcNode += ", ";
            }
        }
        callProcNode +=")";

        ArrayList<String> resultCallProc = new ArrayList<String>();
        resultCallProc.add(callProcNode);
        if (! structInstructions.equals("")) resultCallProc.add(structInstructions);

        return resultCallProc;
    }

    @Override
    public Object visit(DivOP d) {
        Map<String, String> expr1= (Map<String, String>) d.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) d.getE1().accept(this);
        Map<String, String> divNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        divNode.put("code", expr1.get("code") + " / " + expr2.get("code"));
        if ( ! structInstructions.equals("")) divNode.put("serviceInstr", structInstructions);
        // divNode.add(null);

        return divNode;
    }

    @Override
    public Object visit(ElifOP c) {

        /* Dato che non vengono chiuse le parentesi } dell'elif (dell'else), viene incrementato
        * un contatore di parentesi da chiudere, che verranno poi aggiunte nell'if, alla fine di tutto,
        * decrementando di uno per ogni parentesi messa. */
        this.countElifOP ++;

        String elifNode = "else {\n";
        Map<String, String> expr= (Map<String, String>) c.getE().accept(this);
        if(expr.containsKey("serviceInstr")) elifNode += expr.get("serviceInstr");
        elifNode += "if (";
        elifNode += expr.get("code");
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
        Map<String, String> expr1= (Map<String, String>) eq.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) eq.getE1().accept(this);
        Map<String, String> eqNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");


        eqNode.put("code", expr1.get("code") + " == " + expr2.get("code"));
        if( ! structInstructions.equals("")) eqNode.put("serviceInstr", structInstructions);
        //eqNode.add(null);
        return eqNode;
    }

    @Override
    public Object visit(Expr e) {
        Map<String, String> exprNode = new HashMap<String, String>();
        //String[] callProcSplitted;
        if(e.getCp() != null){
            ArrayList<String> callProcNode= (ArrayList<String>) e.getCp().accept(this);
            //callProcSplitted=callProcNode.split(" ");
            exprNode.put("code", callProcNode.get(0));
            exprNode.put("idProc", e.getCp().getRt().getSymbol());
            if(callProcNode.size() > 1) exprNode.put("serviceInstr", callProcNode.get(1));

            return exprNode;
        }

        return null;
    }

    @Override
    public Object visit(GreaterEqualsOP ge) {
        Map<String, String> expr1= (Map<String, String>) ge.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) ge.getE1().accept(this);
        Map<String, String> geNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        geNode.put("code", expr1.get("code") + " >= " + expr2.get("code"));
        if( ! structInstructions.equals("")) geNode.put("serviceInstr", structInstructions);
       // geNode.add(null);


        return geNode;
    }

    @Override
    public Object visit(GreaterThanOP gt) {
        Map<String, String> expr1= (Map<String, String>) gt.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) gt.getE1().accept(this);
        Map<String, String> gtNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        gtNode.put("code", expr1.get("code") + " > " + expr2.get("code"));
        if( ! structInstructions.equals("")) gtNode.put("serviceInstr", structInstructions);
        //gtNode.add(null);


        return gtNode;
    }

    @Override
    public Object visit(Id id) {
        Map<String, String> idNode= new HashMap<String, String>();
        idNode.put("code", id.getId());
        //idNode.add(null);

        return idNode;
    }

    @Override
    public Object visit(IdListInitOP x) {
        String idListInitNode = this.typeVarDecl.equals("string") ? "*" + x.getId().getId() : x.getId().getId();
        if(x.getExpr() != null) {
            Map<String, String> expr = (Map<String, String>) x.getExpr().accept(this);
            if (this.isGlobalVar) {
                 this.globalAssignVar += x.getId().getId() + " = " + expr.get("code") +";\n";
            } else {
                idListInitNode += " = ";
                idListInitNode += expr.get("code");
            }
        }
        return idListInitNode;
    }

    @Override
    public Object visit(IfOP c) {
        String ifNode= "";
        Map<String, String> expr= (Map<String, String>) c.getE().accept(this);
        if(expr.containsKey("serviceInstr")) ifNode += expr.get("serviceInstr");
        ifNode += "if (";
        ifNode += expr.get("code") + ") {\n";
        ifNode += (String) c.getsList().accept(this);
        ifNode += "}\n";

        for(ElifOP elif: c.getElList()){
            ifNode += (String) elif.accept(this);
        }

        if(c.getEl()!=null) {
            ifNode += (String) c.getEl().accept(this);
        }

        /* Aggiungo le parentesi } che devono essere chiuse degli elif. */
        while(this.countElifOP != 0) {
            ifNode += "}\n";
            this.countElifOP--;
        }

        return ifNode;
    }

    @Override
    public Object visit(LessEqualsOP le) {
        Map<String, String> expr1= (Map<String, String>) le.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) le.getE1().accept(this);
        Map<String, String> leNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        leNode.put("code", expr1.get("code") + " <= " + expr2.get("code"));
        if( ! structInstructions.equals("")) leNode.put("serviceInstr", structInstructions);
       // leNode.add(null);
        return leNode;
    }


    @Override
    public Object visit(LessThanOP lt) {
        Map<String, String> expr1= (Map<String, String>) lt.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) lt.getE1().accept(this);
        Map<String, String> ltNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        ltNode.put("code", expr1.get("code") + " < " + expr2.get("code"));
        if( ! structInstructions.equals("")) ltNode.put("serviceInstr", structInstructions);
       // ltNode.add(null);
        return ltNode;
    }

    @Override
    public Object visit(MinusOP m) {
        Map<String, String> expr1= (Map<String, String>) m.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) m.getE1().accept(this);
        Map<String, String> minusNode= new HashMap<String, String>();
        String structInstructions = "";

        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        minusNode.put("code", expr1.get("code") + " - " + expr2.get("code"));
        if( ! structInstructions.equals("")) minusNode.put("serviceInstr", structInstructions);
       // minusNode.add(null);

        return minusNode;
    }

    @Override
    public Object visit(NotEqualsOP ne) {
        Map<String, String> expr1= (Map<String, String>) ne.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) ne.getE1().accept(this);
        Map<String, String> neNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        neNode.put("code", expr1.get("code") + " != " + expr2.get("code"));
        if( ! structInstructions.equals("")) neNode.put("serviceInstr", structInstructions);
        //neNode.add(null);
        return neNode ;
    }

    @Override
    public Object visit(NotOP n) {
        Map<String, String> expr= (Map<String, String>) n.getNe().accept(this);
        Map<String, String> notNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr.containsKey("serviceInstr")) structInstructions += expr.get("serviceInstr");

        notNode.put("code", "!"+expr.get("code"));
        if( ! structInstructions.equals("")) notNode.put("serviceInstr", structInstructions);
       // notNode.add(null);

        return notNode;
    }

    @Override
    public Object visit(OrOP or) {
        Map<String, String> expr1= (Map<String, String>) or.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) or.getE1().accept(this);
        Map<String, String> orNode= new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        orNode.put("code", expr1.get("code") + " || " + expr2.get("code"));
        if( ! structInstructions.equals("")) orNode.put("serviceInstr", structInstructions);
        //orNode.add(null);
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
        Map<String, String> expr1= (Map<String, String>) p.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) p.getE1().accept(this);

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        Map<String, String> plusNode= new HashMap<String, String>();
        plusNode.put("code", expr1.get("code") + " + " + expr2.get("code"));
        if( ! structInstructions.equals("")) plusNode.put("serviceInstr", structInstructions);
       // plusNode.add(null);

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
                Map<String, String> expr =(Map<String, String>) e.accept(this);
                if(expr.containsKey("idProc")){
                    StructC structC=null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get("idProc") + "_struct")) structC = sc;
                    }
                    if(structC != null){
                        structInstructions += structC.getNome() + " " + structC.getNome() + "VarRes" + this.indexResultStruct + " = " + expr.get("code") + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            returnList.add(structC.getNome()+ "VarRes" + this.indexResultStruct + ".var"+i);
                        }
                        this.indexResultStruct++;
                    } else returnList.add(expr.get("code"));
                } else {
                    returnList.add(expr.get("code"));
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
        if(s.getCp() != null) {
            ArrayList<String> cp = (ArrayList<String>) s.getCp().accept(this);
            if (cp.size() > 1 ) return cp.get(1) + cp.get(0) + ";\n";
            else return cp.get(0) + ";\n";

        }
        return null;
    }

    @Override
    public Object visit(TimesOP t) {
        Map<String, String> expr1= (Map<String, String>) t.getE().accept(this);
        Map<String, String> expr2= (Map<String, String>) t.getE1().accept(this);
        Map<String, String> timesNode = new HashMap<String, String>();

        String structInstructions = "";
        if(expr1.containsKey("serviceInstr")) structInstructions += expr1.get("serviceInstr");
        if(expr2.containsKey("serviceInstr")) structInstructions += expr2.get("serviceInstr");

        timesNode.put("code", expr1.get("code") + " * " + expr2.get("code"));
        if( ! structInstructions.equals("")) timesNode.put("serviceInstr", structInstructions);
        //timesNode.add(null);

        return timesNode;
    }

    @Override
    public Object visit(UMinusOP u) {
        Map<String, String> uMinusNode= new HashMap<String, String>();
        Map<String, String> expr = (Map<String, String>) u.getE().accept(this);

        String structInstructions = "";
        if(expr.containsKey("serviceInstr")) structInstructions += expr.get("serviceInstr");

        uMinusNode.put("code", "-"+expr.get("code"));
        if( ! structInstructions.equals("")) uMinusNode.put("serviceInstr", structInstructions);
        //uMinusNode.add(null);

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

        Map<String, String> expr = (Map<String, String>) c.getE().accept(this);

        if(expr.containsKey("serviceInstr")) whileNode += expr.get("serviceInstr");

        whileNode += "while (";
        whileNode += expr.get("code");
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
        String structInstructions = "";

        for(Expr e : c.getExprList()) {
            Map<String, String> expr= (Map<String, String>) e.accept(this);
            if (e instanceof StringConst) {
                stringNodes.add(expr.get("code"));
            } else {
                if(expr.containsKey("serviceInstr")) structInstructions += expr.get("serviceInstr");
                if (expr.containsKey("idProc")) {

                    StructC structC = null;
                    for(StructC sc: this.structMethod){
                        if(sc.getNome().equals(expr.get("idProc")+"_struct")) structC = sc;
                    }

                    String [] returnType = this.getStringSplitted(e.getRt().getType(), 1);
                    if (structC != null) {
                        structInstructions += structC.getNome() + " " + structC.getNome() + "VarWrite" + this.indexWriteStruct + " = " + expr.get("code") + ";\n";

                        for(int i=0; i< structC.getIndex();i++){
                            stringNodes.add(this.getTypeInWR(returnType[i], "write"));
                            exprNodes.add(structC.getNome()+ "VarWrite" + this.indexWriteStruct + ".var"+i);
                        }
                        this.indexWriteStruct++;
                    } else {
                        stringNodes.add(this.getTypeInWR(returnType[0], "write"));
                        exprNodes.add(expr.get("code"));
                    }
                } else {
                    stringNodes.add(this.getTypeInWR(e.getRt().getType(), "write"));
                    exprNodes.add(expr.get("code"));
                }

            }
        }

        /* Aggiungo le istruzioni di servizio (es. struct) prima della print. */
        if (! structInstructions.equals("")) writeOp += structInstructions;

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
        Map<String, String> stringConstNode= new HashMap<String, String>();
        stringConstNode.put("code", "\"" + sc.getS() + "\"");
        //stringConstNode.add(null);

        return stringConstNode;
    }

    @Override
    public Object visit(IntConst ic) {
        Map<String, String> intConstNode= new HashMap<String, String>();
        intConstNode.put("code", ic.getVal()+"");
        //intConstNode.add(null);

        return intConstNode;
    }

    @Override
    public Object visit(Bool b) {
        Map<String, String> boolNode= new HashMap<String, String>();
        boolNode.put("code", ""+b.isB());
        //boolNode.add(null);

        return boolNode;
    }

    @Override
    public Object visit(Null c) {
        Map<String, String> nullNode= new HashMap<String, String>();
        nullNode.put("code", "\"\"");
        //nullNode.add(null);
        return nullNode;
    }

    @Override
    public Object visit(FloatConst fc) {
        Map<String, String> floatNode= new HashMap<String, String>();
        floatNode.put("code", fc.getF()+"");
        //floatNode.add(null);
        return floatNode;
    }
}
