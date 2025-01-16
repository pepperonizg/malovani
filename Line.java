import java.awt.*;

class Line {
    public final Point start;
    public final Point end;
    public final Color color;
    public final int thickness;

    public Line(Point start, Point end, Color color, int thickness) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.thickness = thickness;
    }

    public boolean isNear(Point p, int buffer) {
        return p.distance(start) <= buffer || p.distance(end) <= buffer;
    }
}
