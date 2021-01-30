package visitors;

import nodes.*;

public interface Visitor {

    public Object visit(ProgramOP p);
    public Object visit(AndOP a);
    public Object visit(AssignOP a);
    public Object visit(BodyOP b);
    public Object visit(CallProcOP c);
    public Object visit(DivOP c);
    public Object visit(ElifOP c);
    public Object visit(ElseOP c);
    public Object visit(EqualsOP c);
    public Object visit(Expr c);
    public Object visit(GreaterEqualsOP c);
    public Object visit(GreaterThanOP c);
    public Object visit(Id c);
    public Object visit(IdListInitOP c);
    public Object visit(IfOP c);
    public Object visit(LessEqualsOP c);
    public Object visit(LessThanOP c);
    public Object visit(MinusOP c);
    public Object visit(NotEqualsOP c);
    public Object visit(NotOP c);
    public Object visit(OrOP c);
    public Object visit(ParDeclOP c);
    public Object visit(PlusOP c);
    public Object visit(ProcBodyOP c);
    public Object visit(ProcOP c);
    public Object visit(ReadOP c);
    public Object visit(Stat c);
    public Object visit(TimesOP c);
    public Object visit(UMinusOP c);
    public Object visit(VarDeclOP c);
    public Object visit(WhileOP c);
    public Object visit(WriteOP c);
    public Object visit(StringConst c);
    public Object visit(IntConst c);
    public Object visit(Bool c);
    public Object visit(Null c);
    public Object visit(FloatConst c);

}
