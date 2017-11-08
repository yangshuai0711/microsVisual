package com.asprise.util.tiff;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class MyTIFFReader {
    private File a;
    private int b;
    private J c;

    static {
        a();
    }

    public MyTIFFReader(File var1) throws IOException {
        this.a = var1;
        this.c = aE.a("TIFF", var1, new o());
        this.b = this.c.a();
    }

    public RenderedImage getPage(int var1) throws IOException {
        if (var1 >= this.b) {
            throw new IndexOutOfBoundsException("Index: " + var1 + " >= " + this.b + " (total pages)!");
        } else {
            RenderedImage var2 = this.c.a(var1);
            BufferedImage var3 = new BufferedImage(var2.getColorModel(), var2.copyData((WritableRaster)null), false,
                (Hashtable)null);
            return var3;
        }
    }

    public int countPages() {
        return this.b;
    }

    public static void a(File var0) throws IOException {
        ObjectOutputStream var1 = new ObjectOutputStream(new FileOutputStream(var0));
        var1.writeObject(new GregorianCalendar());
        var1.close();
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            setFileHidden_Windows(var0, true);
            setFileReadOnly_Windows(var0, true);
        }

    }

    public static void a() {
        try {
            File var0 = new File(".atlc");
            if (!var0.exists()) {
                var0 = new File(System.getProperty("user.home"), ".atlc");
            }

            if (!var0.exists()) {
                if (var0.createNewFile()) {
                    a(var0);
                    return;
                }

                var0 = new File(".atlc");
                if (var0.createNewFile()) {
                    a(var0);
                }
            } else {
                try {
                    ObjectInputStream var1 = new ObjectInputStream(new FileInputStream(var0));
                    GregorianCalendar var2 = (GregorianCalendar)var1.readObject();
                    var2.add(6, 30);
                    if ((new GregorianCalendar()).after(var2)) {
                        throw new RuntimeException(
                            "\n\nTRIAL EXPIRED. VISIT http://asprise.com/ TO OBTAIN A PROPER LICENSE.\n\n");
                    }
                } catch (IOException var3) {
                    a(var0);
                } catch (ClassNotFoundException var4) {
                    a(var0);
                }
            }
        } catch (Exception var5) {
            ;
        }

    }

    private static boolean a(File var0, String var1) {
        try {
            Runtime.getRuntime().exec(new String[] {"ATTRIB", var1, var0.getAbsolutePath()});
            return true;
        } catch (IOException var3) {
            return false;
        }
    }

    public static boolean setFileHidden_Windows(File var0, boolean var1) {
        return a(var0, var1 ? "+H" : "-H");
    }

    public static boolean setFileReadOnly_Windows(File var0, boolean var1) {
        return a(var0, var1 ? "+R" : "-R");
    }
}
