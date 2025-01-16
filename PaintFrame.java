import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

class PaintFrame extends JFrame {
    private final PaintCanvas canvas;

    public PaintFrame() {
        setTitle("Malování");
        setSize(1024, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));

        canvas = new PaintCanvas();
        add(canvas, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load Image");
        loadItem.addActionListener(e -> canvas.loadImage());
        fileMenu.add(loadItem);

        JMenuItem saveItem = new JMenuItem("Save Image");
        saveItem.addActionListener(e -> canvas.saveImage());
        fileMenu.add(saveItem);

        JMenuItem restoreItem = new JMenuItem("Restore Original Image");
        restoreItem.addActionListener(e -> canvas.restoreImage());
        fileMenu.add(restoreItem);

        menuBar.add(fileMenu);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to exit?", 
                "Confirm Exit", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        JMenu filterMenu = new JMenu("Filters");

        JMenuItem negativeItem = new JMenuItem("Negative");
        negativeItem.addActionListener(e -> canvas.applyNegativeFilter());
        filterMenu.add(negativeItem);

        JMenuItem thresholdItem = new JMenuItem("Threshold");
        thresholdItem.addActionListener(e -> showThresholdDialog());
        filterMenu.add(thresholdItem);

        JMenuItem rgbItem = new JMenuItem("RGB Adjust");
        rgbItem.addActionListener(e -> showRGBDialog());
        filterMenu.add(rgbItem);

        menuBar.add(filterMenu);

        JMenu generateMenu = new JMenu("Generate Image");
        JMenuItem generateItem = new JMenuItem("Generate Image");
        generateItem.addActionListener(e -> generateImage());
        generateMenu.add(generateItem);
        menuBar.add(generateMenu);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        JPanel buttonPanel = new JPanel();

        JButton clearButton = new JButton("Erase");
        clearButton.addActionListener(e -> canvas.clearCanvas());
        buttonPanel.add(clearButton);

        JButton undoButton = new JButton("Back");
        undoButton.addActionListener(e -> canvas.undoLastAction());
        buttonPanel.add(undoButton);

        JToggleButton eraserButton = new JToggleButton("Eraser");
        JToggleButton penButton = new JToggleButton("Pen", true);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(eraserButton);
        toolGroup.add(penButton);

        eraserButton.addActionListener(e -> canvas.setToolMode(PaintCanvas.ToolMode.ERASER));
        penButton.addActionListener(e -> canvas.setToolMode(PaintCanvas.ToolMode.PEN));

        buttonPanel.add(penButton);
        buttonPanel.add(eraserButton);

        JLabel thicknessLabel = new JLabel("Thickness: ");
        buttonPanel.add(thicknessLabel);

        JSpinner thicknessSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        thicknessSpinner.addChangeListener(e -> canvas.setToolSize((int) thicknessSpinner.getValue()));
        buttonPanel.add(thicknessSpinner);

        JLabel colorLabel = new JLabel("Color: ");
        buttonPanel.add(colorLabel);

        JButton colorButton = new JButton();
        colorButton.setBackground(Color.BLACK);
        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "Pen color", canvas.getCurrentColor());
            if (selectedColor != null) {
                canvas.setCurrentColor(selectedColor);
                colorButton.setBackground(selectedColor);
            }
        });
        buttonPanel.add(colorButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void generateImage() {
        Object[] options = {"Generate Wallpaper", "Generate Sun"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose the type of image to generate:",
                "Generate Image",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            canvas.setImage(makeColoredWallpaper());
        } else if (choice == JOptionPane.NO_OPTION) {
            canvas.setImage(makeColoredSun());
        }
        canvas.repaint();
    }

    private BufferedImage makeColoredWallpaper() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        Random random = new Random();
        for (int y = 0; y < height; y += 200) {
            for (int x = 0; x < width; x += 200) {
                GradientPaint gp = new GradientPaint(
                    x, y, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)),
                    x + 200, y + 200, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
                );
                g2d.setPaint(gp);
                g2d.fillRect(x, y, 200, 200);

                Polygon triangle1 = new Polygon();
                triangle1.addPoint(x, y);
                triangle1.addPoint(x + 200, y);
                triangle1.addPoint(x, y + 200);

                Polygon triangle2 = new Polygon();
                triangle2.addPoint(x + 200, y + 200);
                triangle2.addPoint(x + 200, y);
                triangle2.addPoint(x, y + 200);

                g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                g2d.fill(triangle1);

                g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                g2d.fill(triangle2);
            }
        }

        g2d.dispose();
        return image;
    }

    private BufferedImage makeColoredSun() {
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.CYAN);
        g2d.fillRect(0, 0, width, height);

        int centerX = width / 2;
        int centerY = height / 2;
        int sunRadius = 100;
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(centerX - sunRadius, centerY - sunRadius, sunRadius * 2, sunRadius * 2);

        Random random = new Random();
        int numRays = 16;
        int rayLength = 150;
        for (int i = 0; i < numRays; i++) {
            double angle = Math.toRadians((360 / numRays) * i);
            int x1 = centerX + (int) (sunRadius * Math.cos(angle));
            int y1 = centerY + (int) (sunRadius * Math.sin(angle));
            int x2 = centerX + (int) ((sunRadius + rayLength) * Math.cos(angle));
            int y2 = centerY + (int) ((sunRadius + rayLength) * Math.sin(angle));

            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g2d.setStroke(new BasicStroke(10));
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();
        return image;
    }

    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "About", true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("PaintApp");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        JLabel descriptionLabel = new JLabel("<html><center>Paint App<br>Version: 1.0<br>Autoři: Jaromír Mynarčík, Lukáš Markel, Tomáš Rosa<br></center></html>");
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(descriptionLabel);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> aboutDialog.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(closeButton);

        aboutDialog.add(panel);
        aboutDialog.setSize(300, 150);
        aboutDialog.setResizable(false);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    private void showThresholdDialog() {
        JDialog thresholdDialog = new JDialog(this, "Set Threshold", true);
        thresholdDialog.setSize(400, 150);
        thresholdDialog.setLayout(new BorderLayout());
        thresholdDialog.setLocationRelativeTo(this);
    
        JPanel sliderPanel = new JPanel();
        JLabel sliderValueLabel = new JLabel("Threshold: 128");
        JSlider thresholdSlider = new JSlider(0, 255, 128);
    
        sliderPanel.add(sliderValueLabel);
        sliderPanel.add(thresholdSlider);
    
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton exitButton = new JButton("Exit");
    
        buttonPanel.add(applyButton);
        buttonPanel.add(exitButton);
    
        thresholdDialog.add(sliderPanel, BorderLayout.CENTER);
        thresholdDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        BufferedImage originalImage = canvas.getLoadedImageCopy();
    
        thresholdSlider.addChangeListener(e -> {
            int thresholdValue = thresholdSlider.getValue();
            sliderValueLabel.setText("Threshold: " + thresholdValue);
            canvas.applyThreshold(thresholdValue);
        });
    
        applyButton.addActionListener(e -> {
            thresholdDialog.dispose();
        });
   
        exitButton.addActionListener(e -> {
            canvas.setLoadedImage(originalImage);
            canvas.repaint();
            thresholdDialog.dispose();
        });
    
        thresholdDialog.setVisible(true);
    }

    private void showRGBDialog() {
        JDialog rgbDialog = new JDialog(this, "RGB Adjust", true);
        rgbDialog.setSize(400, 200);
        rgbDialog.setLayout(new BorderLayout());
        rgbDialog.setLocationRelativeTo(this);
    
        JPanel sliderPanel = new JPanel(new GridLayout(4, 2));
        JLabel redLabel = new JLabel("Red: 0");
        JLabel greenLabel = new JLabel("Green: 0");
        JLabel blueLabel = new JLabel("Blue: 0");
        JSlider redSlider = new JSlider(-255, 255, 0);
        JSlider greenSlider = new JSlider(-255, 255, 0);
        JSlider blueSlider = new JSlider(-255, 255, 0);
    
        sliderPanel.add(redLabel);
        sliderPanel.add(redSlider);
        sliderPanel.add(greenLabel);
        sliderPanel.add(greenSlider);
        sliderPanel.add(blueLabel);
        sliderPanel.add(blueSlider);
    
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton exitButton = new JButton("Exit");
    
        buttonPanel.add(applyButton);
        buttonPanel.add(exitButton);
    
        rgbDialog.add(sliderPanel, BorderLayout.CENTER);
        rgbDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        BufferedImage originalImage = canvas.getLoadedImageCopy();
    
        redSlider.addChangeListener(e -> {
            redLabel.setText("Red: " + redSlider.getValue());
            canvas.applyRGBFilter(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
        });
        greenSlider.addChangeListener(e -> {
            greenLabel.setText("Green: " + greenSlider.getValue());
            canvas.applyRGBFilter(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
        });
        blueSlider.addChangeListener(e -> {
            blueLabel.setText("Blue: " + blueSlider.getValue());
            canvas.applyRGBFilter(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
        });
    
        applyButton.addActionListener(e -> {
            rgbDialog.dispose();
        });
    
        exitButton.addActionListener(e -> {
            canvas.setLoadedImage(originalImage);
            canvas.repaint();
            rgbDialog.dispose();
        });
    
        rgbDialog.setVisible(true);
    }
}
