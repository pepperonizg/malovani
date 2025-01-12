// File: PaintCanvas.java
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
    private final int toolSize = 10;
    private Point cursorPosition = new Point(0, 0);
    private BufferedImage loadedImage = null; // Obrázek načtený z počítače

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
        loadedImage = null; // Vymaže i načtený obrázek
        repaint();
    }

    public void undoLastAction() {
        if (!undoStack.isEmpty()) {
            Operation lastOperation = undoStack.pop();
            if (lastOperation.type == OperationType.DRAW) {
                paths.remove(lastOperation.path);
            } else if (lastOperation.type == OperationType.ERASE) {
                paths.add(lastOperation.path);
            } else if (lastOperation.type == OperationType.LOAD_IMAGE) { // Vrátíme načtení obrázku
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
                undoStack.push(new Operation(OperationType.LOAD_IMAGE, null)); // Uložíme načtení obrázku na zásobník
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
            this.paint(g2d); // Vykreslí obsah panelu na obrázek
            g2d.dispose();
            try {
                ImageIO.write(image, "jpg", file);
                JOptionPane.showMessageDialog(this, "Obrázek byl úspěšně uložen!", "Uloženo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Chyba při ukládání obrázku!", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void applyNegativeFilter() {
        if (loadedImage != null) {
            // Získání rozměrů obrázku
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();
    
            // Vytvoření nového obrázku pro negativ
            BufferedImage negativeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
            // Procházení všech pixelů a převod na negativ
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
    
            loadedImage = negativeImage;  // Nastavení nového obrázku jako načtený obrázek
            repaint();  // Překreslení panelu s novým obrázkem
        }
    }
    
    public void applyThreshold(int threshold) {
        if (loadedImage == null) return;
    
        for (int x = 0; x < loadedImage.getWidth(); x++) {
            for (int y = 0; y < loadedImage.getHeight(); y++) {
                Color color = new Color(loadedImage.getRGB(x, y));
                int brightness = (int)(color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114); // Luminance formula
                if (brightness < threshold) {
                    loadedImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    loadedImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    if (loadedImage != null) {
        // Získáme rozměry panelu
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Získáme rozměry obrázku
        int imageWidth = loadedImage.getWidth();
        int imageHeight = loadedImage.getHeight();

        // Vypočítáme poměr stran obrázku
        double aspectRatio = (double) imageWidth / imageHeight;

        // Vypočítáme novou velikost obrázku podle panelu, zachováme poměr stran
        int newWidth;
        int newHeight;

        if ((double) panelWidth / panelHeight > aspectRatio) {
            // Šířka panelu je větší než poměr stran obrázku, omezíme podle výšky
            newHeight = panelHeight;
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            // Výška panelu je větší než poměr stran obrázku, omezíme podle šířky
            newWidth = panelWidth;
            newHeight = (int) (newWidth / aspectRatio);
        }

        // Vypočítáme pozici obrázku tak, aby byl vystředěný
        int x = (panelWidth - newWidth) / 2;
        int y = (panelHeight - newHeight) / 2;

        // Vykreslíme obrázek
        g2d.drawImage(loadedImage, x, y, newWidth, newHeight, null);
    }

    // Kreslíme všechny nakreslené čáry
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

    // Kreslí kurzor jako bílý čtverec s černým okrajem
    g2d.setColor(Color.WHITE);
    g2d.fillRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
    g2d.setColor(Color.BLACK);
    g2d.drawRect(cursorPosition.x - toolSize / 2, cursorPosition.y - toolSize / 2, toolSize, toolSize);
    }
}    