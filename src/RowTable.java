public class RowTable {
    private String symbol, kind;
    private Object type;

    public RowTable(String symbol, String kind, Object type) {
        this.symbol = symbol;
        this.kind = kind;
        this.type = type;
    }

    public RowTable() {}

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }
}
