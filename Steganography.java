import java.awt.Color;
import java.util.ArrayList;

public class Steganography {

    public static void clearLow(Pixel p) {
        int r = (p.getRed() / 4) * 4;
        int g = (p.getGreen() / 4) * 4;
        int b = (p.getBlue() / 4) * 4;
        p.setColor(new Color(r, g, b));
    }

    public static Picture testClearLow(Picture pic) {
        Picture copy = new Picture(pic);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel px : row) clearLow(px);
        return copy;
    }

    public static void setLow(Pixel p, Color c) {
        clearLow(p);
        Color old = p.getColor();
        int r = old.getRed() + c.getRed() / 64;
        int g = old.getGreen() + c.getGreen() / 64;
        int b = old.getBlue() + c.getBlue() / 64;
        p.setColor(new Color(r, g, b));
    }

    public static Picture testSetLow(Picture pic, Color c) {
        Picture copy = new Picture(pic);
        for (Pixel[] row : copy.getPixels2D())
            for (Pixel px : row) setLow(px, c);
        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] src = hidden.getPixels2D();
        Pixel[][] dst = copy.getPixels2D();
        for (int r = 0; r < dst.length; r++)
            for (int c = 0; c < dst[0].length; c++) {
                Color col = src[r][c].getColor();
                int r2 = (col.getRed() % 4) * 64;
                int g2 = (col.getGreen() % 4) * 64;
                int b2 = (col.getBlue() % 4) * 64;
                dst[r][c].setColor(new Color(r2, g2, b2));
            }
        return copy;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return source.getWidth() >= secret.getWidth() && source.getHeight() >= secret.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret) {
        Picture hidden = new Picture(source);
        Pixel[][] hp = hidden.getPixels2D();
        Pixel[][] sp = secret.getPixels2D();
        int rows = Math.min(hp.length, sp.length);
        int cols = Math.min(hp[0].length, sp[0].length);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                setLow(hp[r][c], sp[r][c].getColor());
        return hidden;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> codes = new ArrayList<>();
        for (int i = 0; i < s.length(); i++)
            codes.add(s.charAt(i) == ' ' ? 27 : alpha.indexOf(s.charAt(i)) + 1);
        codes.add(0);
        return codes;
    }

    private static String decodeString(ArrayList<Integer> codes) {
        StringBuilder sb = new StringBuilder();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int code : codes) {
            if (code == 27) sb.append(' ');
            else if (code > 0) sb.append(alpha.charAt(code - 1));
        }
        return sb.toString();
    }

    private static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        for (int i = 0; i < 3; i++) {
            bits[i] = num % 4;
            num /= 4;
        }
        return bits;
    }

    public static Picture hideText(Picture source, String msg) {
        ArrayList<Integer> code = encodeString(msg);
        Picture stego = new Picture(source);
        Pixel[][] px = stego.getPixels2D();
        int idx = 0;
        outer:
        for (int r = 0; r < px.length; r++)
            for (int c = 0; c < px[0].length; c++) {
                if (idx >= code.size()) break outer;
                int[] bits = getBitPairs(code.get(idx++));
                Pixel p = px[r][c];
                clearLow(p);
                Color old = p.getColor();
                p.setColor(new Color(old.getRed() + bits[0], old.getGreen() + bits[1], old.getBlue() + bits[2]));
            }
        return stego;
    }

    public static String revealText(Picture source) {
        ArrayList<Integer> codes = new ArrayList<>();
        Pixel[][] px = source.getPixels2D();
        outer:
        for (Pixel[] row : px)
            for (Pixel p : row) {
                Color col = p.getColor();
                int code = (col.getRed() % 4) + (col.getGreen() % 4) * 4 + (col.getBlue() % 4) * 16;
                if (code == 0) break outer;
                codes.add(code);
            }
        return decodeString(codes);
    }

    public static boolean isSame(Picture p1, Picture p2) {
        if (p1.getWidth() != p2.getWidth() || p1.getHeight() != p2.getHeight())
            return false;
        Pixel[][] a = p1.getPixels2D(), b = p2.getPixels2D();
        for (int r = 0; r < a.length; r++)
            for (int c = 0; c < a[0].length; c++)
                if (!a[r][c].getColor().equals(b[r][c].getColor())) return false;
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture p1, Picture p2) {
        ArrayList<Point> list = new ArrayList<>();
        if (!isSameSize(p1, p2)) return list;
        Pixel[][] a = p1.getPixels2D(), b = p2.getPixels2D();
        for (int r = 0; r < a.length; r++)
            for (int c = 0; c < a[0].length; c++)
                if (!a[r][c].getColor().equals(b[r][c].getColor()))
                    list.add(new Point(r, c));
        return list;
    }

    private static boolean isSameSize(Picture a, Picture b) {
        return a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight();
    }

    public static Picture showDifferentArea(Picture src, ArrayList<Point> pts) {
        if (pts.isEmpty()) return new Picture(src);
        int minR = Integer.MAX_VALUE, minC = Integer.MAX_VALUE, maxR = -1, maxC = -1;
        for (Point p : pts) {
            minR = Math.min(minR, p.getRow());
            minC = Math.min(minC, p.getCol());
            maxR = Math.max(maxR, p.getRow());
            maxC = Math.max(maxC, p.getCol());
        }
        Picture out = new Picture(src);
        Pixel[][] px = out.getPixels2D();
        Color red = Color.RED;
        for (int c = minC; c <= maxC; c++) {
            px[minR][c].setColor(red);
            px[maxR][c].setColor(red);
        }
        for (int r = minR; r <= maxR; r++) {
            px[r][minC].setColor(red);
            px[r][maxC].setColor(red);
        }
        return out;
    }

    public static class Point {
        private int row, col;
        public Point(int r, int c) { row = r; col = c; }
        public int getRow() { return row; }
        public int getCol() { return col; }
        public String toString() { return "(" + row + "," + col + ")"; }
    }

    public static void main(String[] args) {
        Picture beach = new Picture("beach.jpg");
        Picture cleared = testClearLow(beach);
        Picture pinked = testSetLow(beach, Color.PINK);
        cleared.explore();
        pinked.explore();

        Picture secret = new Picture("arch.jpg");
        if (canHide(beach, secret)) {
            Picture combo = hidePicture(beach, secret);
            combo.explore();
            Picture revealed = revealPicture(combo);
            revealed.explore();
        }

        Picture carrier = hideText(beach, "THIS IS A TEST");
        carrier.explore();
        String msg = revealText(carrier);
        System.out.println("Decoded message: " + msg);

        if (canHide(beach, secret)) {
            Picture altered = hidePicture(beach, secret);
            ArrayList<Point> diffs = findDifferences(beach, altered);
            Picture boxed = showDifferentArea(beach, diffs);
            boxed.explore();
        }
    }
}