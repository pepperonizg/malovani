import java.awt.*;

class Line {
    public final Point start;
    public final Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public boolean isNear(Point p, int buffer) {
        return p.distance(start) <= buffer || p.distance(end) <= buffer;
    }
}
