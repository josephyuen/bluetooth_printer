package com.will.bluetoothprinterdemo.print;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static Bitmap compressBitmap(Bitmap srcBitmap, int maxLength) {
        try {
            double ratio;
            int destWidth;
            int destHeight;
            Options opts = new Options();
            byte[] srcBytes = bitmap2Bytes(srcBitmap);
            BitmapFactory.decodeByteArray(srcBytes, 0, srcBytes.length, opts);
            int srcWidth = opts.outWidth;
            int srcHeight = opts.outHeight;
            if (srcWidth > srcHeight) {
                ratio = (double) (srcWidth / maxLength);
                destWidth = maxLength;
                destHeight = (int) (((double) srcHeight) / ratio);
            } else {
                ratio = (double) (srcHeight / maxLength);
                destHeight = maxLength;
                destWidth = (int) (((double) srcWidth) / ratio);
            }
            Options newOpts = new Options();
            newOpts.inSampleSize = ((int) ratio) + 1;
            newOpts.inJustDecodeBounds = false;
            newOpts.outHeight = destHeight;
            newOpts.outWidth = destWidth;
            return BitmapFactory.decodeByteArray(srcBytes, 0, srcBytes.length, newOpts);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while (true) {
            int len = inStream.read(buffer);
            if (len == -1) {
                byte[] data = outStream.toByteArray();
                outStream.close();
                inStream.close();
                return data;
            }
            outStream.write(buffer, 0, len);
        }
    }

    public static Bitmap getImageFromBytes(byte[] bytes, Options opts) {
        if (bytes == null) {
            return null;
        }
        if (opts != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float) w) / ((float) width), ((float) h) / ((float) height));
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static File saveFileFromBytes(byte[] b, String outputFile) throws Throwable {
        Exception e;
        Throwable th;
        BufferedOutputStream stream = null;
        File file = null;
        try {
            BufferedOutputStream stream2;
            File file2 = new File(outputFile);
            try {
                stream2 = new BufferedOutputStream(new FileOutputStream(file2));
            } catch (Exception e2) {
                e = e2;
                file = file2;
                try {
                    e.printStackTrace();
                    if (stream != null) {
                        return file;
                    }
                    try {
                        stream.close();
                        return file;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return file;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e12) {
                            e12.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                file = file2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e122) {
                        e122.printStackTrace();
                    }
                }
                throw th;
            }
            try {
                stream2.write(b);
                if (stream2 != null) {
                    try {
                        stream2.close();
                        stream = stream2;
                        return file2;
                    } catch (IOException e1222) {
                        e1222.printStackTrace();
                    }
                }
                stream = stream2;
                return file2;
            } catch (Exception e3) {
                e = e3;
                file = file2;
                stream = stream2;
                e.printStackTrace();
                if (stream != null) {
                    return file;
                }
                try {
                    stream.close();
                    return file;
                } catch (IOException e12222) {
                    e12222.printStackTrace();
                    return file;
                }
            } catch (Throwable th4) {
                th = th4;
                file = file2;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e122222) {
                        e122222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (stream != null) {
                return file;
            }
            try {
                stream.close();
                return file;
            } catch (IOException e1222222) {
                e1222222.printStackTrace();
                return file;
            }
        }
    }

    public static int printBitmap2File(Bitmap bitmap, String filePath) {
        File file;
        if (filePath.endsWith(".png")) {
            file = new File(filePath);
        } else {
            file = new File(new StringBuilder(String.valueOf(filePath)).append(".png").toString());
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, fos);
            fos.close();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static byte[] bitmap2PrinterBytes(Bitmap bitmap, int left) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] imgbuf = new byte[((((width / 8) + left) + 4) * height)];
        byte[] bitbuf = new byte[(width / 8)];
        int[] p = new int[8];
        int s = 0;
        System.out.println("+++++++++++++++ Total Bytes: " + (((width / 8) + 4) * height));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width / 8; x++) {
                for (int m = 0; m < 8; m++) {
                    if (bitmap.getPixel((x * 8) + m, y) == -1) {
                        p[m] = 0;
                    } else {
                        p[m] = 1;
                    }
                }
                bitbuf[x] = (byte) ((((((((p[0] * 128) + (p[1] * 64)) + (p[2] * 32)) + (p[3] * 16)) + (p[4] * 8)) + (p[5] * 4)) + (p[6] * 2)) + p[7]);
            }
            if (y != 0) {
                s++;
                imgbuf[s] = (byte) 22;
            } else {
                imgbuf[s] = (byte) 22;
            }
            s++;
            imgbuf[s] = (byte) ((width / 8) + left);
            for (int j = 0; j < left; j++) {
                s++;
                imgbuf[s] = (byte) 0;
            }
            for (int n = 0; n < width / 8; n++) {
                s++;
                imgbuf[s] = bitbuf[n];
            }
            s++;
            imgbuf[s] = (byte) 21;
            s++;
            imgbuf[s] = (byte) 1;
        }
        return imgbuf;
    }

    public static byte[] bitmap2PrinterBytes_stylus(Bitmap bitmap, int multiple, int left) {
        byte[] imgBuf;
        int i;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth() + left;
        boolean need_0a = false;
        if (width < 240) {
            imgBuf = new byte[(((height / 8) + 1) * (width + 6))];
            need_0a = true;
        } else {
            imgBuf = new byte[((((height / 8) + 1) * (width + 5)) + 2)];
        }
        byte[] tmpBuf = new byte[(width + 5)];
        int[] p = new int[8];
        int s = 0;
        for (int y = 0; y < (height / 8) + 1; y++) {
            tmpBuf[0] = (byte) 27;
            int t = 0 + 1;
            tmpBuf[t] = (byte) 42;
            t++;
            tmpBuf[t] = (byte) multiple;
            t++;
            tmpBuf[t] = (byte) (width % 240);
            t++;
            tmpBuf[t] = (byte) (width / 240 > 0 ? 1 : 0);
            boolean allZERO = true;
            int x = 0;
            while (x < width) {
                for (int m = 0; m < 8; m++) {
                    if ((y * 8) + m >= height || x < left) {
                        p[m] = 0;
                    } else {
                        p[m] = bitmap.getPixel(x - left, (y * 8) + m) == -1 ? 0 : 1;
                    }
                }
                int value = (((((((p[0] * 128) + (p[1] * 64)) + (p[2] * 32)) + (p[3] * 16)) + (p[4] * 8)) + (p[5] * 4)) + (p[6] * 2)) + p[7];
                t++;
                tmpBuf[t] = (byte) value;
                if (value != 0) {
                    allZERO = false;
                }
                x++;
            }
            if (allZERO) {
                if (s == 0) {
                    imgBuf[s] = (byte) 27;
                } else {
                    s++;
                    imgBuf[s] = (byte) 27;
                }
                s++;
                imgBuf[s] = (byte) 74;
                s++;
                imgBuf[s] = (byte) 8;
            } else {
                for (i = 0; i < t + 1; i++) {
                    if (i == 0 && s == 0) {
                        imgBuf[s] = tmpBuf[i];
                    } else {
                        s++;
                        imgBuf[s] = tmpBuf[i];
                    }
                }
                if (need_0a) {
                    s++;
                    imgBuf[s] = (byte) 10;
                }
            }
        }
        if (!need_0a) {
            s++;
            imgBuf[s] = (byte) 13;
            s++;
            imgBuf[s] = (byte) 10;
        }
        byte[] realBuf = new byte[(s + 1)];
        for (i = 0; i < s + 1; i++) {
            realBuf[i] = imgBuf[i];
        }
        StringBuffer sb = new StringBuffer();
        i = 0;
        while (i < realBuf.length) {
            String temp = Integer.toHexString(realBuf[i] & 255);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(new StringBuilder(String.valueOf(temp)).append(" ").toString());
            if ((i != 0 && i % 100 == 0) || i == realBuf.length - 1) {
                Log.e("12345", sb.toString());
                sb = new StringBuffer();
            }
            i++;
        }
        return realBuf;
    }

    public static int getStringCharacterLength(String line) {
        int length = 0;
        for (int j = 0; j < line.length(); j++) {
            if (line.charAt(j) > 256) {
                length += 2;
            } else {
                length++;
            }
        }
        return length;
    }

    public static int getSubLength(String line, int width) {
        int length = 0;
        for (int j = 0; j < line.length(); j++) {
            if (line.charAt(j) > 256) {
                length += 2;
            } else {
                length++;
            }
            if (length > width) {
                int temp = line.substring(0, j - 1).lastIndexOf(" ");
                if (temp != -1) {
                    return temp;
                }
                int i;
                if (j - 1 == 0) {
                    i = 1;
                } else {
                    i = j - 1;
                }
                return i;
            }
        }
        return line.length();
    }

    public static boolean isNum(byte temp) {
        return temp >= (byte) 48 && temp <= (byte) 57;
    }

    public static void Log(String tag, String msg) {
        if (PrinterInstance.DEBUG) {
            Log.i(tag, msg);
        }
    }
}