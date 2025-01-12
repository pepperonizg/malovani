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

        // Vytvoření hlavního menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // Možnost pro načtení obrázku
        JMenuItem loadItem = new JMenuItem("Load Image");
        loadItem.addActionListener(e -> canvas.loadImage());
        fileMenu.add(loadItem);

        // Možnost pro uložení obrázku
        JMenuItem saveItem = new JMenuItem("Save Image");
        saveItem.addActionListener(e -> canvas.saveImage());
        fileMenu.add(saveItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Vytvoření menu Filters
        JMenu filterMenu = new JMenu("Filters");

        // Možnost pro negativ
        JMenuItem negativeItem = new JMenuItem("Negative");
        negativeItem.addActionListener(e -> canvas.applyNegativeFilter());
        filterMenu.add(negativeItem);
        
        menuBar.add(filterMenu);
        setJMenuBar(menuBar);

        JMenuItem thresholdItem = new JMenuItem("Threshold");
        thresholdItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog("Enter threshold value (0-255):");
            if (input != null && !input.isEmpty()) {
                try {
                    int threshold = Integer.parseInt(input);
                    if (threshold >= 0 && threshold <= 255) {
                        canvas.applyThreshold(threshold);
                    } else {
                        JOptionPane.showMessageDialog(this, "Please enter a value between 0 and 255.", "Invalid input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        filterMenu.add(thresholdItem);

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
