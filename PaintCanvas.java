import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

class PaintCanvas extends JPanel {
    private final ArrayList<ArrayList<Line>> paths = new ArrayList<>();
    private final Stack<Operation> undoStack = new Stack<>();
    private ArrayList<Line> currentPath;
    private ToolMode currentTool = ToolMode.PEN;
    private int toolSize = 10;
    private Color currentColor = Color.BLACK;
    private Point cursorPosition = new Point(0, 0);
    private BufferedImage loadedImage = null;
    private BufferedImage originalImage = null; // Uloží původní načtený obrázek

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
                        currentPath.add(new Line(startPoint, endPoint, currentColor, toolSize));
                    } else {
                        currentPath.add(new Line(e.getPoint(), e.getPoint(), currentColor, toolSize));
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

    public void setToolSize(int size) {
        toolSize = size;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        repaint();
    }

    public Color getCurrentColor() {
        return currentColor;
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
        loadedImage = null;
        originalImage = null; // Vymaže i původní obrázek
        repaint();
    }

    public void undoLastAction() {
        if (!undoStack.isEmpty()) {
            Operation lastOperation = undoStack.pop();
            if (lastOperation.type == OperationType.DRAW) {
                paths.remove(lastOperation.path);
            } else if (lastOperation.type == OperationType.ERASE) {
                paths.add(lastOperation.path);
            } else if (lastOperation.type == OperationType.LOAD_IMAGE) {
                loadedImage = null;
            }
            repaint();
        }
    }

    public void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                loadedImage = ImageIO.read(fileChooser.getSelectedFile());
                originalImage = ImageIO.read(fileChooser.getSelectedFile()); // Uloží původní obrázek
                undoStack.push(new Operation(OperationType.LOAD_IMAGE, null));
                repaint();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Chyba při načítání obrázku!", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".jpg")) {
                file = new File(file.getAbsolutePath() + ".jpg");
            }
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            this.paint(g2d);
            g2d.dispose();
            try {
                ImageIO.write(image, "jpg", file);
                JOptionPane.showMessageDialog(this, "Obrázek byl úspěšně uložen!", "Uloženo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Chyba při ukládání obrázku!", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void restoreImage() {
        if (originalImage != null) {
            loadedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
            Graphics g = loadedImage.getGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.dispose();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Žádný obrázek k obnovení!", "Upozornění", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void applyNegativeFilter() {
        if (loadedImage != null) {
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();
            BufferedImage negativeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = new Color(loadedImage.getRGB(x, y));
                    int red = 255 - color.getRed();
                    int green = 255 - color.getGreen();
                    int blue = 255 - color.getBlue();
                    Color negativeColor = new Color(red, green, blue);
                    negativeImage.setRGB(x, y, negativeColor.getRGB());
                }
            }
            loadedImage = negativeImage;
            repaint();
        }
    }

    public void applyThreshold(int threshold) {
        if (originalImage == null) return; // Pracujeme s originálním obrázkem
    
        loadedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                Color color = new Color(originalImage.getRGB(x, y));
                int brightness = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                if (brightness < threshold) {
                    loadedImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    loadedImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        repaint(); // Okamžitě překreslí panel s obrázkem
    }

    public void applyRGBFilter(int redOffset, int greenOffset, int blueOffset) {
        if (originalImage == null) return; // Pracujeme s originálním obrázkem
    
        loadedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                Color color = new Color(originalImage.getRGB(x, y));
                int red = Math.min(255, Math.max(0, color.getRed() + redOffset));
                int green = Math.min(255, Math.max(0, color.getGreen() + greenOffset));
                int blue = Math.min(255, Math.max(0, color.getBlue() + blueOffset));
                Color newColor = new Color(red, green, blue);
                loadedImage.setRGB(x, y, newColor.getRGB());
            }
        }
        repaint(); // Okamžitě překreslí panel s obrázkem
    }
    
    
    public BufferedImage getLoadedImageCopy() {
        if (loadedImage == null) return null;
        BufferedImage copy = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), loadedImage.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(loadedImage, 0, 0, null);
        g.dispose();
        return copy;
    }
    
    public void setLoadedImage(BufferedImage image) {
        this.loadedImage = image;
        repaint();
    }

    public void setImage(BufferedImage image) {
        this.loadedImage = image;
        repaint();
    }

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    if (loadedImage != null) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();
        double aspectRatio = (double) imageWidth / imageHeight;
        int newWidth;
        int newHeight;

        if ((double) panelWidth / panelHeight > aspectRatio) {
            newHeight = panelHeight;
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            newWidth = panelWidth;
            newHeight = (int) (newWidth / aspectRatio);
        }

        int x = (panelWidth - newWidth) / 2;
        int y = (panelHeight - newHeight) / 2;
        g2d.drawImage(loadedImage, x, y, newWidth, newHeight, null);
    }

    for (ArrayList<Line> path : paths) {
        for (Line line : path) {
            g2d.setColor(line.color);
            g2d.setStroke(new BasicStroke(line.thickness));
            g2d.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
        }
    }

    if (currentPath != null) {
        for (Line line : currentPath) {
            g2d.setColor(line.color);
            g2d.setStroke(new BasicStroke(line.thickness));
            g2d.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
        }
    }

    if (currentTool == ToolMode.ERASER) {
        g2d.setColor(Color.WHITE); // Bílá výplň
        g2d.fillRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
        g2d.setColor(Color.BLACK); // Černý rámeček
        g2d.drawRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
    } else {
        g2d.setColor(currentColor); // Barva kurzoru odpovídá zvolené barvě
        g2d.fillRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
        g2d.drawRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
    }
}

}
