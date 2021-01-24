public class RowTable {
    private String symbol, kind, type;

    public RowTable(String symbol, String kind, String type) {
        this.symbol = symbol;
        this.kind = kind;
        this.type = type;
    }

    public RowTable(String symbol) {
        this.symbol = symbol;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
