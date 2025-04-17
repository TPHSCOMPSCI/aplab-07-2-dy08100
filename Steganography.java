import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {

    public static void clearLow(Pixel p) {
        p.setColor(new Color((p.getRed() / 4) * 4, (p.getGreen() / 4) * 4, (p.getBlue() / 4) * 4));
    }

    public static Picture testClearLow(Picture pic) {
        Picture copy = new Picture(pic);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel px : row) clearLow(px);
        return copy;
    }

    public static void setLow(Pixel p, Color c) {
        int rBits = c.getRed() / 64;
        int gBits = c.getGreen() / 64;
        int bBits = c.getBlue() / 64;
        p.setColor(new Color((p.getRed() / 4) * 4 + rBits, (p.getGreen() / 4) * 4 + gBits, (p.getBlue() / 4) * 4 + bBits));
    }

    public static Picture testSetLow(Picture pic, Color c) {
        Picture copy = new Picture(pic);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel px : row) setLow(px, c);
        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel px : row)
                px.setColor(new Color((px.getRed() % 4) * 64, (px.getGreen() % 4) * 64, (px.getBlue() % 4) * 64));
        return copy;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return secret.getWidth() <= source.getWidth() && secret.getHeight() <= source.getHeight();
    }

    private static boolean canHideSub(Picture host, Picture sub, int r, int c) {
        return r >= 0 && c >= 0 && r + sub.getHeight() <= host.getHeight() && c + sub.getWidth() <= host.getWidth();
    }

    public static Picture hidePicture(Picture source, Picture secret) {
        if (!canHide(source, secret)) {
            System.out.println("Secret image is larger than the host – resize first.");
            return null;
        }
        return hidePicture(source, secret, 0, 0);
    }

    public static Picture hidePicture(Picture source, Picture secret, int startRow, int startCol) {
        if (!canHideSub(source, secret, startRow, startCol)) {
            System.out.println("Secret won’t fit at that location.");
            return null;
        }
        Picture combined = new Picture(source);
        Pixel[][] host = combined.getPixels2D();
        Pixel[][] sec = secret.getPixels2D();
        for (int r = 0; r < sec.length; r++)
            for (int c = 0; c < sec[0].length; c++)
                setLow(host[r + startRow][c + startCol], sec[r][c].getColor());
        return combined;
    }

    public static boolean isSame(Picture a, Picture b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        Pixel[][] p1 = a.getPixels2D();
        Pixel[][] p2 = b.getPixels2D();
        for (int r = 0; r < p1.length; r++)
            for (int c = 0; c < p1[0].length; c++)
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) return false;
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture a, Picture b) {
        ArrayList<Point> diffs = new ArrayList<>();
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return diffs;
        Pixel[][] p1 = a.getPixels2D();
        Pixel[][] p2 = b.getPixels2D();
        for (int r = 0; r < p1.length; r++)
            for (int c = 0; c < p1[0].length; c++)
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) diffs.add(new Point(c, r));
        return diffs;
    }

    public static Picture showDifferentArea(Picture pic, ArrayList<Point> diffs) {
        if (diffs == null || diffs.isEmpty()) return new Picture(pic);
        int minR = Integer.MAX_VALUE, maxR = -1, minC = Integer.MAX_VALUE, maxC = -1;
        for (Point p : diffs) {
            minR = Math.min(minR, p.y);
            maxR = Math.max(maxR, p.y);
            minC = Math.min(minC, p.x);
            maxC = Math.max(maxC, p.x);
        }
        Picture out = new Picture(pic);
        Pixel[][] px = out.getPixels2D();
        for (int c = minC; c <= maxC; c++) {
            px[minR][c].setColor(Color.RED);
            px[maxR][c].setColor(Color.RED);
        }
        for (int r = minR; r <= maxR; r++) {
            px[r][minC].setColor(Color.RED);
            px[r][maxC].setColor(Color.RED);
        }
        return out;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        ArrayList<Integer> list = new ArrayList<>();
        for (char ch : s.toCharArray()) {
            if (ch == ' ') list.add(27);
            else if ('A' <= ch && ch <= 'Z') list.add(ch - 'A' + 1);
        }
        list.add(0);
        return list;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        StringBuilder out = new StringBuilder();
        for (int code : codes) {
            if (code == 0) break;
            if (code == 27) out.append(' ');
            else if (code >= 1 && code <= 26) out.append((char) ('A' + code - 1));
        }
        return out.toString();
    }

    private static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        int code = num;
        for (int i = 0; i < 3; i++) {
            bits[i] = code % 4;
            code = code / 4;
        }
        return bits;
    }

    public static Picture hideText(Picture source, String s) {
        ArrayList<Integer> codes = encodeString(s);
        if (codes.size() > source.getWidth() * source.getHeight()) {
            System.out.println("Message too long for this picture.");
            return null;
        }
        Picture copy = new Picture(source);
        Pixel[][] px = copy.getPixels2D();
        int idx = 0;
        outer:
        for (int r = 0; r < px.length; r++)
            for (int c = 0; c < px[0].length; c++) {
                int code = codes.get(idx++);
                int[] pairs = getBitPairs(code);
                Color old = px[r][c].getColor();
                px[r][c].setColor(new Color((old.getRed() / 4) * 4 + pairs[2], (old.getGreen() / 4) * 4 + pairs[1], (old.getBlue() / 4) * 4 + pairs[0]));
                if (idx == codes.size()) break outer;
            }
        return copy;
    }

    public static String revealText(Picture pic) {
        ArrayList<Integer> codes = new ArrayList<>();
        Pixel[][] px = pic.getPixels2D();
        outer:
        for (Pixel[] row : px)
            for (Pixel p : row) {
                int code = ((p.getRed() % 4) << 4) | ((p.getGreen() % 4) << 2) | (p.getBlue() % 4);
                codes.add(code);
                if (code == 0) break outer;
            }
        return decodeString(codes);
    }

    public static Picture creativeEffect(Picture pic, int offset) {
        Picture copy = new Picture(pic);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel p : row) {
                int r = Math.min(255, p.getRed() + offset);
                int g = Math.max(0, p.getGreen() - offset);
                p.setColor(new Color(r, g, p.getBlue()));
            }
        return copy;
    }

    public static void main(String[] args) {
        Picture beach = new Picture("beach.jpg");
        Picture robot = new Picture("robot.jpg");
        Picture cleared = testClearLow(beach);
        Picture pinked = testSetLow(beach, Color.PINK);
        Picture revealed1 = revealPicture(pinked);
        Picture hidden = hidePicture(beach, robot);
        if (hidden != null) {
            Picture shown = revealPicture(hidden);
            hidden.explore();
            shown.explore();
        }
        Picture flower = new Picture("flower1.jpg");
        Picture host = hidePicture(beach, flower, 60, 200);
        if (host != null) {
            host.explore();
            revealPicture(host).explore();
        }
        Picture msgPic = hideText(beach, "HELLO WORLD");
        if (msgPic != null) System.out.println("Revealed text: " + revealText(msgPic));
        ArrayList<Point> diffs = findDifferences(beach, host);
        Picture boxed = showDifferentArea(beach, diffs);
        boxed.explore();
    }
}