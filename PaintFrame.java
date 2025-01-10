import javax.swing.*;
import java.awt.*;

class PaintFrame extends JFrame {
    private final PaintCanvas canvas;

    public PaintFrame() {
        setTitle("Malování");
        setSize(1024, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        canvas = new PaintCanvas();
        add(canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton clearButton = new JButton("Vymazat");
        clearButton.addActionListener(e -> {
            canvas.clearCanvas();
        });
        buttonPanel.add(clearButton);

        JButton undoButton = new JButton("Krok zpět");
        undoButton.addActionListener(e -> {
            canvas.undoLastAction();
        });
        buttonPanel.add(undoButton);

        JToggleButton eraserButton = new JToggleButton("Guma");
        JToggleButton penButton = new JToggleButton("Pero", true);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(eraserButton);
        toolGroup.add(penButton);

        eraserButton.addActionListener(e -> {
            canvas.setToolMode(PaintCanvas.ToolMode.ERASER);
        });

        penButton.addActionListener(e -> {
            canvas.setToolMode(PaintCanvas.ToolMode.PEN);
        });

        buttonPanel.add(penButton);
        buttonPanel.add(eraserButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}