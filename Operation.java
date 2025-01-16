import java.util.ArrayList;

class Operation {
    public final OperationType type;
    public final ArrayList<Line> path;

    public Operation(OperationType type, ArrayList<Line> path) {
        this.type = type;
        this.path = path;
    }
}

enum OperationType {
    DRAW, ERASE, LOAD_IMAGE
}
