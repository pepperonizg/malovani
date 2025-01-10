import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Stack;

class PaintCanvas extends JPanel {
    private final ArrayList<ArrayList<Line>> paths = new ArrayList<>();
    private final Stack<Operation> undoStack = new Stack<>();
    private ArrayList<Line> currentPath;
    private ToolMode currentTool = ToolMode.PEN;
    private final int toolSize = 10;
    private Point cursorPosition = new Point(0, 0);

    public enum ToolMode {
        PEN, ERASER
    }

    public PaintCanvas() {
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPath = new ArrayList<>();
                cursorPosition = e.getPoint();
                if (currentTool == ToolMode.ERASER) {
                    eraseAtPoint(e.getPoint());
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cursorPosition = e.getPoint();
                if (currentTool == ToolMode.PEN && currentPath != null && !currentPath.isEmpty()) {
                    paths.add(currentPath);
                    undoStack.push(new Operation(OperationType.DRAW, new ArrayList<>(currentPath)));
                }
                currentPath = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                cursorPosition = e.getPoint();
                if (currentTool == ToolMode.ERASER) {
                    eraseAtPoint(e.getPoint());
                } else if (currentTool == ToolMode.PEN && currentPath != null) {
                    Point endPoint = e.getPoint();
                    if (!currentPath.isEmpty()) {
                        Point startPoint = currentPath.get(currentPath.size() - 1).end;
                        currentPath.add(new Line(startPoint, endPoint));
                    } else {
                        currentPath.add(new Line(e.getPoint(), e.getPoint()));
                    }
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                cursorPosition = e.getPoint();
                repaint();
            }
        });
    }

    public void setToolMode(ToolMode mode) {
        currentTool = mode;
    }

    private void eraseAtPoint(Point point) {
        for (int i = paths.size() - 1; i >= 0; i--) {
            ArrayList<Line> path = paths.get(i);
            for (Line line : path) {
                if (line.isNear(point, toolSize)) {
                    undoStack.push(new Operation(OperationType.ERASE, paths.remove(i)));
                    repaint();
                    return;
                }
            }
        }
    }

    public void clearCanvas() {
        paths.clear();
        undoStack.clear();
        currentPath = null;
        repaint();
    }

    public void undoLastAction() {
        if (!undoStack.isEmpty()) {
            Operation lastOperation = undoStack.pop();
            if (lastOperation.type == OperationType.DRAW) {
                paths.remove(lastOperation.path);
            } else if (lastOperation.type == OperationType.ERASE) {
                paths.add(lastOperation.path);
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (ArrayList<Line> path : paths) {
            for (Line line : path) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
            }
        }

        if (currentPath != null) {
            for (Line line : currentPath) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
            }
        }

        // Draw cursor as a white square with a black border
        g2d.setColor(Color.WHITE);
        g2d.fillRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
    }
}