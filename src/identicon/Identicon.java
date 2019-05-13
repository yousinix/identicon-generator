package identicon;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Identicon {

    private final String DEFAULT_TEXT = "";
    private final String DEFAULT_HASHING_ALGORITHM = "MD5";
    private final int DEFAULT_BORDER_PIXELS = 1;
    private final int DEFAULT_IDENTICON_PIXELS = 5;
    private final int DEFAULT_IMAGE_PIXELS = 250;
    private final int DEFAULT_RGBA = 255;

    private Color foreground;
    private int[] foregroundRGBA;

    private Color background;
    private int[] backgroundRGBA;

    private String text;
    private byte[] hashedText;
    private String hashingAlgorithm;

    private BufferedImage identicon;
    private WritableRaster raster;
    private int borderPixels;
    private int identiconPixels;
    private int totalPixels;

    private Image image;
    private BufferedImage bufferedImage;
    private int imagePixels;


    // Singleton Fields and Methods

    private static Identicon instance = new Identicon();

    public static Identicon getInstance() {
        return instance;
    }

    private Identicon() {
        // Generate White Identicon & Image
        foreground = background = Color.WHITE;
        generateIdenticon();
        generateImage();
    }


    //// Public Methods ////

    public void generateIdenticon() {

        determineText();
        determineHashingAlgorithm();
        hashText();

        determineIdenticonDimensions();
        initIdenticon();

        determineBackground();
        determineForeground();
        drawBorder();
        drawIdenticon();

    }

    public void generateImage() {

        determineImageDimensions();

        BufferedImage scaledImage = new BufferedImage(imagePixels, imagePixels, BufferedImage.TYPE_INT_ARGB);

        AffineTransform transform = new AffineTransform();
        int scale = imagePixels / totalPixels;
        transform.scale(scale, scale);

        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        scaledImage = transformOp.filter(identicon, scaledImage);

        image = SwingFXUtils.toFXImage(scaledImage, null);

    }


    //// Private Methods ////

    private void determineText() {
        text = text == null ? DEFAULT_TEXT : text;
    }

    private void determineHashingAlgorithm() {
        hashingAlgorithm = hashingAlgorithm == null ? DEFAULT_HASHING_ALGORITHM : hashingAlgorithm;
    }

    private void hashText() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(hashingAlgorithm);
        } catch(NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());

        }
        hashedText = messageDigest.digest(text.getBytes());
    }
    
    private void determineIdenticonDimensions() {
        identiconPixels = identiconPixels <= 0 ? DEFAULT_IDENTICON_PIXELS : identiconPixels;
        borderPixels    = borderPixels    <= 0 ? DEFAULT_BORDER_PIXELS : borderPixels;
        totalPixels     = identiconPixels + 2 * borderPixels;
    }

    private void initIdenticon() {
        identicon = new BufferedImage(totalPixels, totalPixels, BufferedImage.TYPE_INT_ARGB);
        raster = identicon.getRaster();
    }

    private void determineForeground() {
        int r = foreground == null ? hashedText[0] & DEFAULT_RGBA : (int) (foreground.getRed() * 255);
        int g = foreground == null ? hashedText[1] & DEFAULT_RGBA : (int) (foreground.getGreen() * 255);
        int b = foreground == null ? hashedText[2] & DEFAULT_RGBA : (int) (foreground.getBlue() * 255);
        int a = foreground == null ? DEFAULT_RGBA : (int) (foreground.getOpacity() * 255);
        foregroundRGBA = new int[]{r, g, b, a};
    }

    private void determineBackground() {
        int r = background == null ? DEFAULT_RGBA : (int) (background.getRed() * 255);
        int g = background == null ? DEFAULT_RGBA : (int) (background.getGreen() * 255);
        int b = background == null ? DEFAULT_RGBA : (int) (background.getBlue() * 255);
        int a = background == null ? DEFAULT_RGBA : (int) (background.getOpacity() * 255);
        backgroundRGBA = new int[]{r, g, b, a};
    }

    private void drawBorder() {
        for (int x = 0; x < totalPixels; x++) {
            for (int y = 0; y < totalPixels; y++) {
                boolean isBorderX = x < borderPixels || x >= totalPixels - borderPixels;
                boolean isBorderY = y < borderPixels || y >= totalPixels - borderPixels;
                if (isBorderX || isBorderY) {
                    raster.setPixel(x, y, backgroundRGBA);
                }
            }
        }
    }

    private void drawIdenticon() {
        for (int x = 0; x < identiconPixels; x++) {
            int i = x < 3 ? x : identiconPixels - 1 - x;
            i = i >= hashedText.length ? i % hashedText.length : i;
            for (int y = 0; y < identiconPixels; y++) {
                if ((hashedText[i] >> y & 1) == 1) {
                    raster.setPixel(x + borderPixels, y + borderPixels, foregroundRGBA);
                } else {
                    raster.setPixel(x + borderPixels, y + borderPixels, backgroundRGBA);
                }
            }
        }
    }

    private void determineImageDimensions() {
        imagePixels = imagePixels <= 0 ? DEFAULT_IMAGE_PIXELS : imagePixels;
    }

    //// Getter and Setters ////

    public int getTotalPixels() {
        return totalPixels;
    }

    public Image getImage() {
        return image;
    }

    public BufferedImage getBufferedImage() {
        return SwingFXUtils.fromFXImage(image, null);
    }

    public void setImagePixels(int imagePixels) {
        this.imagePixels = imagePixels;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHashingAlgorithm(String hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public void setBorderPixels(int borderPixels) {
        this.borderPixels = borderPixels;
    }

    public void setIdenticonPixels(int identiconPixels) {
        this.identiconPixels = identiconPixels;
    }


}
