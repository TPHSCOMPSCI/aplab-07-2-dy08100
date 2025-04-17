import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {

    private static int[] getBitPairs(int n) {
        return new int[]{n & 3, (n >> 2) & 3, (n >> 4) & 3};
    }

    public static void clearLow(Pixel p) {
        int red = p.getRed() & 0xFC;
        int green = p.getGreen() & 0xFC;
        int blue = p.getBlue() & 0xFC;
        p.setColor(new Color(red, green, blue));
    }

    public static Picture testClearLow(Picture pic) {
        Picture copy = new Picture(pic);
        Pixel[][] px = copy.getPixels2D();
        for (Pixel[] row : px) {
            for (Pixel p : row) clearLow(p);
        }
        return copy;
    }

    public static void setLow(Pixel p, Color c) {
        int red = (p.getRed() & 0xFC) | (c.getRed() >> 6);
        int green = (p.getGreen() & 0xFC) | (c.getGreen() >> 6);
        int blue = (p.getBlue() & 0xFC) | (c.getBlue() >> 6);
        p.setColor(new Color(red, green, blue));
    }

    public static Picture testSetLow(Picture pic, Color c) {
        Picture copy = new Picture(pic);
        Pixel[][] px = copy.getPixels2D();
        for (Pixel[] row : px) {
            for (Pixel p : row) setLow(p, c);
        }
        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] px = copy.getPixels2D();
        for (Pixel[] row : px) {
            for (Pixel p : row) {
                int r = (p.getRed() & 3) << 6;
                int g = (p.getGreen() & 3) << 6;
                int b = (p.getBlue() & 3) << 6;
                p.setColor(new Color(r, g, b));
            }
        }
        return copy;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return secret.getWidth() <= source.getWidth() && secret.getHeight() <= source.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret) {
        if (!canHide(source, secret)) return null;
        Picture combined = new Picture(source);
        Pixel[][] src = combined.getPixels2D();
        Pixel[][] sec = secret.getPixels2D();
        for (int r = 0; r < sec.length; r++) {
            for (int c = 0; c < sec[0].length; c++) setLow(src[r][c], sec[r][c].getColor());
        }
        return combined;
    }

    public static boolean isSame(Picture a, Picture b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        Pixel[][] p1 = a.getPixels2D();
        Pixel[][] p2 = b.getPixels2D();
        for (int r = 0; r < p1.length; r++) {
            for (int c = 0; c < p1[0].length; c++) {
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) return false;
            }
        }
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture a, Picture b) {
        ArrayList<Point> diff = new ArrayList<>();
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return diff;
        Pixel[][] p1 = a.getPixels2D();
        Pixel[][] p2 = b.getPixels2D();
        for (int r = 0; r < p1.length; r++) {
            for (int c = 0; c < p1[0].length; c++) {
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) diff.add(new Point(c, r));
            }
        }
        return diff;
    }

    public static Picture showDifferentArea(Picture pic, ArrayList<Point> diff) {
        Picture copy = new Picture(pic);
        if (diff.isEmpty()) return copy;
        int minR = Integer.MAX_VALUE, maxR = -1, minC = Integer.MAX_VALUE, maxC = -1;
        for (Point p : diff) {
            if (p.y < minR) minR = p.y;
            if (p.y > maxR) maxR = p.y;
            if (p.x < minC) minC = p.x;
            if (p.x > maxC) maxC = p.x;
        }
        Pixel[][] px = copy.getPixels2D();
        for (int c = minC; c <= maxC; c++) {
            px[minR][c].setColor(Color.RED);
            px[maxR][c].setColor(Color.RED);
        }
        for (int r = minR; r <= maxR; r++) {
            px[r][minC].setColor(Color.RED);
            px[r][maxC].setColor(Color.RED);
        }
        return copy;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        ArrayList<Integer> result = new ArrayList<>();
        for (char ch : s.toCharArray()) result.add(ch == ' ' ? 27 : ch - 'A' + 1);
        result.add(0);
        return result;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        StringBuilder sb = new StringBuilder();
        for (int code : codes) {
            if (code == 0) break;
            sb.append(code == 27 ? ' ' : (char) ('A' + code - 1));
        }
        return sb.toString();
    }

    public static Picture hideText(Picture source, String s) {
        Picture copy = new Picture(source);
        ArrayList<Integer> codes = encodeString(s);
        Pixel[][] px = copy.getPixels2D();
        int idx = 0;
        outer:
        for (int r = 0; r < px.length; r++) {
            for (int c = 0; c < px[0].length; c++) {
                if (idx >= codes.size()) break outer;
                int[] bits = getBitPairs(codes.get(idx));
                int red = (px[r][c].getRed() & 0xFC) | bits[2];
                int green = (px[r][c].getGreen() & 0xFC) | bits[1];
                int blue = (px[r][c].getBlue() & 0xFC) | bits[0];
                px[r][c].setColor(new Color(red, green, blue));
                idx++;
            }
        }
        return copy;
    }

    public static String revealText(Picture source) {
        Pixel[][] px = source.getPixels2D();
        ArrayList<Integer> codes = new ArrayList<>();
        outer:
        for (Pixel[] row : px) {
            for (Pixel p : row) {
                int code = ((p.getRed() & 3) << 4) | ((p.getGreen() & 3) << 2) | (p.getBlue() & 3);
                codes.add(code);
                if (code == 0) break outer;
            }
        }
        return decodeString(codes);
    }

    public static Picture creativeEffect(Picture pic, int offset) {
        Picture copy = new Picture(pic);
        Pixel[][] px = copy.getPixels2D();
        for (Pixel[] row : px) {
            for (Pixel p : row) {
                int red = Math.min(255, p.getRed() + offset);
                int green = Math.max(0, p.getGreen() - offset);
                p.setColor(new Color(red, green, p.getBlue()));
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
            System.out.println("Source and secret images are not compatible.");
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