import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {
    public static void clearLow(Pixel p) {
        int red = (p.getRed() / 4) * 4;
        int green = (p.getGreen() / 4) * 4;
        int blue = (p.getBlue() / 4) * 4;
        p.setColor(new Color(red, green, blue));
    }
    
    public static Picture testClearLow(Picture pic) {
        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                clearLow(pixels[r][c]);
            }
        }
        return copy;
    }
    
    public static void setLow(Pixel p, Color c) {
        int red = (p.getRed() / 4) * 4;
        int green = (p.getGreen() / 4) * 4;
        int blue = (p.getBlue() / 4) * 4;
        int redBits = c.getRed() / 64;
        int greenBits = c.getGreen() / 64;
        int blueBits = c.getBlue() / 64;
        red = red + redBits;
        green = green + greenBits;
        blue = blue + blueBits;
        p.setColor(new Color(red, green, blue));
    }
    
    public static Picture testSetLow(Picture pic, Color c) {
        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int col = 0; col < pixels[0].length; col++) {
                setLow(pixels[r][col], c);
            }
        }
        return copy;
    }
    
    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] pixels = copy.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                int redLow = pixels[r][c].getRed() % 4;
                int greenLow = pixels[r][c].getGreen() % 4;
                int blueLow = pixels[r][c].getBlue() % 4;
                int newRed = redLow * 64;
                int newGreen = greenLow * 64;
                int newBlue = blueLow * 64;
                pixels[r][c].setColor(new Color(newRed, newGreen, newBlue));
            }
        }
        return copy;
    }
    
    public static boolean canHide(Picture source, Picture secret) {
        return (source.getWidth() == secret.getWidth() && source.getHeight() == secret.getHeight());
    }
    
    public static Picture hidePicture(Picture source, Picture secret) {
        if (!canHide(source, secret)) {
            System.out.println("Error: The source and secret images must be the same size.");
            return null;
        }
        Picture combined = new Picture(source);
        Pixel[][] sourcePixels = combined.getPixels2D();
        Pixel[][] secretPixels = secret.getPixels2D();
        for (int r = 0; r < sourcePixels.length; r++) {
            for (int c = 0; c < sourcePixels[0].length; c++) {
                setLow(sourcePixels[r][c], secretPixels[r][c].getColor());
            }
        }
        return combined;
    }
    
    public static boolean isSame(Picture pic1, Picture pic2) {
        if (pic1.getWidth() != pic2.getWidth() || pic1.getHeight() != pic2.getHeight()) {
            return false;
        }
        Pixel[][] pixels1 = pic1.getPixels2D();
        Pixel[][] pixels2 = pic2.getPixels2D();
        for (int r = 0; r < pixels1.length; r++) {
            for (int c = 0; c < pixels1[0].length; c++) {
                if (!pixels1[r][c].getColor().equals(pixels2[r][c].getColor())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static ArrayList<Point> findDifferences(Picture pic1, Picture pic2) {
        ArrayList<Point> differences = new ArrayList<Point>();
        if (pic1.getWidth() != pic2.getWidth() || pic1.getHeight() != pic2.getHeight()) {
            return differences;
        }
        Pixel[][] pixels1 = pic1.getPixels2D();
        Pixel[][] pixels2 = pic2.getPixels2D();
        for (int r = 0; r < pixels1.length; r++) {
            for (int c = 0; c < pixels1[0].length; c++) {
                if (!pixels1[r][c].getColor().equals(pixels2[r][c].getColor())) {
                    differences.add(new Point(c, r));
                }
            }
        }
        return differences;
    }
    
    public static Picture showDifferentArea(Picture pic, ArrayList<Point> differences) {
        Picture copy = new Picture(pic);
        if (differences.size() == 0) {
            return copy;
        }
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
        for (Point pt : differences) {
            int col = pt.x;
            int row = pt.y;
            if (row < minRow) minRow = row;
            if (row > maxRow) maxRow = row;
            if (col < minCol) minCol = col;
            if (col > maxCol) maxCol = col;
        }
        Pixel[][] pixels = copy.getPixels2D();
        for (int col = minCol; col <= maxCol; col++) {
            if (minRow >= 0 && minRow < copy.getHeight())
                pixels[minRow][col].setColor(Color.RED);
            if (maxRow >= 0 && maxRow < copy.getHeight())
                pixels[maxRow][col].setColor(Color.RED);
        }
        for (int row = minRow; row <= maxRow; row++) {
            if (minCol >= 0 && minCol < copy.getWidth())
                pixels[row][minCol].setColor(Color.RED);
            if (maxCol >= 0 && maxCol < copy.getWidth())
                pixels[row][maxCol].setColor(Color.RED);
        }
        return copy;
    }
    
    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < s.length(); i++) {
            String ch = s.substring(i, i+1);
            if (ch.equals(" ")) {
                result.add(27);
            } else {
                int code = ch.charAt(0) - 'A' + 1;
                result.add(code);
            }
        }
        result.add(0);
        return result;
    }
    
    public static String decodeString(ArrayList<Integer> codes) {
        String result = "";
        for (int code : codes) {
            if (code == 0) break;
            if (code == 27) {
                result += " ";
            } else if (code >= 1 && code <= 26) {
                result += (char)('A' + code - 1);
            }
        }
        return result;
    }
    
    public static Picture hideText(Picture source, String s) {
        Picture copy = new Picture(source);
        ArrayList<Integer> codes = encodeString(s);
        Pixel[][] pixels = copy.getPixels2D();
        int index = 0;
        outer:
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (index >= codes.size()) break outer;
                int code = codes.get(index);
                int redBits = (code >> 4) & 0x3;
                int greenBits = (code >> 2) & 0x3;
                int blueBits = code & 0x3;
                int red = (pixels[r][c].getRed() / 4) * 4 + redBits;
                int green = (pixels[r][c].getGreen() / 4) * 4 + greenBits;
                int blue = (pixels[r][c].getBlue() / 4) * 4 + blueBits;
                pixels[r][c].setColor(new Color(red, green, blue));
                index++;
            }
        }
        if (index < codes.size()) {
            System.out.println("Warning: Not enough pixels to hide the entire message.");
        }
        return copy;
    }
    
    public static String revealText(Picture source) {
        Pixel[][] pixels = source.getPixels2D();
        ArrayList<Integer> codes = new ArrayList<Integer>();
        outer:
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                int redLow = pixels[r][c].getRed() % 4;
                int greenLow = pixels[r][c].getGreen() % 4;
                int blueLow = pixels[r][c].getBlue() % 4;
                int code = (redLow << 4) | (greenLow << 2) | blueLow;
                codes.add(code);
                if (code == 0) break outer;
            }
        }
        return decodeString(codes);
    }
    
    public static Picture creativeEffect(Picture pic, int offset) {
        Picture copy = new Picture(pic);
        Pixel[][] pixels = copy.getPixels2D();
        for (int c = 0; c < pixels[0].length; c++) {
            for (int r = 0; r < pixels.length; r++) {
                int red = pixels[r][c].getRed() + offset;
                if (red > 255) red = 255;
                int green = pixels[r][c].getGreen() - offset;
                if (green < 0) green = 0;
                int blue = pixels[r][c].getBlue();
                pixels[r][c].setColor(new Color(red, green, blue));
            }
        }
        return copy;
    }
    
    public static void main(String[] args) {
        System.out.println("Steganography Lab - Exploring Color");
        Picture beach = new Picture("beach.jpg");
        beach.explore();
        Picture cleared = testClearLow(beach);
        cleared.explore();
        Picture setLowTest = testSetLow(beach, Color.PINK);
        setLowTest.explore();
        Picture revealed = revealPicture(setLowTest);
        revealed.explore();
        Picture secretPic = new Picture("robot.jpg");
        if (canHide(beach, secretPic)) {
            Picture hiddenPic = hidePicture(beach, secretPic);
            hiddenPic.explore();
            Picture revealedPic = revealPicture(hiddenPic);
            revealedPic.explore();
        } else {
            System.out.println("Source and secret images are not the same size. Resize secret image accordingly.");
        }
        Picture swan = new Picture("swan.jpg");
        Picture swan2 = new Picture("swan.jpg");
        System.out.println("Swan and swan2 are the same: " + isSame(swan, swan2));
        Picture modifiedSwan = testClearLow(swan);
        System.out.println("Swan and modified swan are the same (after clearLow): " + isSame(modifiedSwan, swan2));
        ArrayList<Point> diff = findDifferences(swan, modifiedSwan);
        System.out.println("Number of different pixels: " + diff.size());
        Picture diffArea = showDifferentArea(swan, diff);
        diffArea.explore();
        String message = "HELLO WORLD";
        Picture textHidden = hideText(beach, message);
        textHidden.explore();
        String revealedMessage = revealText(textHidden);
        System.out.println("Revealed Text: " + revealedMessage);
        Picture creative = creativeEffect(beach, 30);
        creative.explore();
    }
}