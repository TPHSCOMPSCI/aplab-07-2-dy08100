import java.awt.Color;
import java.util.ArrayList;
import java.awt.Point;

public class Steganography {

    public static void clearLow(Pixel p) {
        Color o = p.getColor();
        int r = (o.getRed()   / 4) * 4;
        int g = (o.getGreen() / 4) * 4;
        int b = (o.getBlue()  / 4) * 4;
        p.setColor(new Color(r, g, b));
    }

    public static Picture testClearLow(Picture pic) {
        Picture p = new Picture(pic);
        Pixel[][] px = p.getPixels2D();
        for (int i = 0; i < px.length; i++)
            for (int j = 0; j < px[0].length; j++)
                clearLow(px[i][j]);
        return p;
    }

    public static void setLow(Pixel p, Color c) {
        clearLow(p);
        Color o = p.getColor();
        int r = o.getRed()   + c.getRed()   / 64;
        int g = o.getGreen() + c.getGreen() / 64;
        int b = o.getBlue()  + c.getBlue()  / 64;
        p.setColor(new Color(r, g, b));
    }

    public static Picture testSetLow(Picture pic, Color col) {
        Picture p = new Picture(pic);
        Pixel[][] px = p.getPixels2D();
        for (int i = 0; i < px.length; i++)
            for (int j = 0; j < px[0].length; j++)
                setLow(px[i][j], col);
        return p;
    }

    public static boolean canHide(Picture src, Picture sec) {
        return src.getWidth() >= sec.getWidth() && src.getHeight() >= sec.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret, int sr, int sc) {
        Picture h = new Picture(source);
        Pixel[][] hp = h.getPixels2D();
        Pixel[][] sp = secret.getPixels2D();
        int ht = sp.length, wd = sp[0].length;
        for (int r = 0; r < ht; r++)
            for (int c = 0; c < wd; c++)
                setLow(hp[sr + r][sc + c], sp[r][c].getColor());
        return h;
    }

    public static Picture hidePicture(Picture source, Picture secret) {
        return hidePicture(source, secret, 0, 0);
    }

    public static Picture revealPicture(Picture hidden) {
        Picture c = new Picture(hidden);
        Pixel[][] h = hidden.getPixels2D();
        Pixel[][] t = c.getPixels2D();
        for (int i = 0; i < h.length; i++)
            for (int j = 0; j < h[0].length; j++) {
                Color col = h[i][j].getColor();
                int r = (col.getRed()   % 4) * 64;
                int g = (col.getGreen() % 4) * 64;
                int b = (col.getBlue()  % 4) * 64;
                t[i][j].setColor(new Color(r, g, b));
            }
        return c;
    }

    public static boolean isSame(Picture p1, Picture p2) {
        if (p1.getWidth() != p2.getWidth() || p1.getHeight() != p2.getHeight())
            return false;
        Pixel[][] a = p1.getPixels2D(), b = p2.getPixels2D();
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                if (a[i][j].getRed()   != b[i][j].getRed()   ||
                    a[i][j].getGreen() != b[i][j].getGreen() ||
                    a[i][j].getBlue()  != b[i][j].getBlue())
                    return false;
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture p1, Picture p2) {
        ArrayList<Point> pts = new ArrayList<>();
        Pixel[][] a = p1.getPixels2D(), b = p2.getPixels2D();
        int rows = Math.min(a.length, b.length), cols = Math.min(a[0].length, b[0].length);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (a[i][j].getRed()   != b[i][j].getRed()   ||
                    a[i][j].getGreen() != b[i][j].getGreen() ||
                    a[i][j].getBlue()  != b[i][j].getBlue())
                    pts.add(new Point(i, j));
        return pts;
    }

    public static Picture showDifferentArea(Picture pic, ArrayList<Point> pts) {
        Picture r = new Picture(pic);
        int minR = pic.getHeight(), minC = pic.getWidth(), maxR = 0, maxC = 0;
        for (Point p : pts) {
            if (p.x < minR) minR = p.x;
            if (p.x > maxR) maxR = p.x;
            if (p.y < minC) minC = p.y;
            if (p.y > maxC) maxC = p.y;
        }
        for (int c = minC; c <= maxC; c++) {
            r.getPixel(minR, c).setColor(Color.RED);
            r.getPixel(maxR, c).setColor(Color.RED);
        }
        for (int rr = minR; rr <= maxR; rr++) {
            r.getPixel(rr, minC).setColor(Color.RED);
            r.getPixel(rr, maxC).setColor(Color.RED);
        }
        return r;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> out = new ArrayList<>();
        for (char ch : s.toCharArray())
            out.add(ch == ' ' ? 27 : alpha.indexOf(ch) + 1);
        out.add(0);
        return out;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        StringBuilder sb = new StringBuilder();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int code : codes) {
            if (code == 0) break;
            if (code == 27) sb.append(' ');
            else sb.append(alpha.charAt(code - 1));
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

    public static Picture hideText(Picture source, String s) {
        Picture pic = new Picture(source);
        Pixel[][] px = pic.getPixels2D();
        ArrayList<Integer> codes = encodeString(s);
        int idx = 0;
        for (int i = 0; i < px.length && idx < codes.size(); i++)
            for (int j = 0; j < px[0].length && idx < codes.size(); j++) {
                Pixel p = px[i][j];
                clearLow(p);
                Color c = p.getColor();
                int[] b = getBitPairs(codes.get(idx++));
                p.setColor(new Color(c.getRed()   + b[0],
                                     c.getGreen() + b[1],
                                     c.getBlue()  + b[2]));
            }
        return pic;
    }

    public static String revealText(Picture source) {
        Pixel[][] px = source.getPixels2D();
        ArrayList<Integer> codes = new ArrayList<>();
        for (int i = 0; i < px.length; i++)
            for (int j = 0; j < px[0].length; j++) {
                Color c = px[i][j].getColor();
                int v = (c.getRed()   % 4)
                      + (c.getGreen() % 4) * 4
                      + (c.getBlue()  % 4) * 16;
                codes.add(v);
                if (v == 0) return decodeString(codes);
            }
        return decodeString(codes);
    }

    public static void main(String[] args) {
        Picture beach = new Picture("beach.jpg");        beach.explore();
        Picture cleared = testClearLow(beach);           cleared.explore();

        Picture beach2 = new Picture("beach.jpg");       beach2.explore();
        Picture set = testSetLow(beach2, Color.PINK);    set.explore();

        Picture arch = new Picture("arch.jpg");          arch.explore();
        Picture robot = new Picture("robot.jpg");
        Picture flower = new Picture("flower1.jpg");
        Picture h1 = hidePicture(arch, robot, 65, 208);
        Picture h2 = hidePicture(h1, flower, 280, 110);
        h2.explore();
        Picture rev = revealPicture(h2);                 rev.explore();

        Picture swan1 = new Picture("swan.jpg");
        Picture swan2 = new Picture("swan.jpg");
        System.out.println("Swan and swan2 are the same: " + isSame(swan1, swan2));
        Picture swanCleared = testClearLow(swan1);
        System.out.println("After clearLow, swan vs swan2: " + isSame(swanCleared, swan2));

        Picture hall = new Picture("femaleLionAndHall.jpg");
        Picture hall2 = hidePicture(hall, robot, 50, 300);
        Picture hall3 = hidePicture(hall2, flower, 115, 275);
        hall3.explore();
        if (!isSame(hall, hall3)) {
            Picture hall4 = showDifferentArea(hall, findDifferences(hall, hall3));
            hall4.show();
            Picture uh = revealPicture(hall3);
            uh.show();
        }

        Picture msgPic = hideText(beach, "HELLO WORLD");
        msgPic.explore();
        System.out.println("Revealed text: " + revealText(msgPic));
    }
}