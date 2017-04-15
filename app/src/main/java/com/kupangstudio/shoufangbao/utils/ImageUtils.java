package com.kupangstudio.shoufangbao.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 图片转换缩放相关工具
 * <ul>
 * convert between Bitmap, byte array, Drawable
 * <li>{@link #bitmapToByte(Bitmap)}</li>
 * <li>{@link #bitmapToDrawable(Bitmap)}</li>
 * <li>{@link #byteToBitmap(byte[])}</li>
 * <li>{@link #byteToDrawable(byte[])}</li>
 * <li>{@link #drawableToBitmap(Drawable)}</li>
 * <li>{@link #drawableToByte(Drawable)}</li>
 * </ul>
 * <ul>
 * scale image
 * <li>{@link #scaleImageTo(Bitmap, int, int)}</li>
 * <li>{@link #scaleImage(Bitmap, float, float)}</li>
 * </ul>
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-6-27
 */
public class ImageUtils {

    private ImageUtils() {
        throw new AssertionError();
    }

    /**
     * convert Bitmap to byte array
     *
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * convert byte array to Bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * convert Drawable to Bitmap
     *
     * @param d
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable d) {
        return d == null ? null : ((BitmapDrawable) d).getBitmap();
    }

    /**
     * convert Bitmap to Drawable
     *
     * @param b
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap b) {
        return b == null ? null : new BitmapDrawable(b);
    }

    /**
     * convert Drawable to byte array
     *
     * @param d
     * @return
     */
    public static byte[] drawableToByte(Drawable d) {
        return bitmapToByte(drawableToBitmap(d));
    }

    /**
     * convert byte array to Drawable
     *
     * @param b
     * @return
     */
    public static Drawable byteToDrawable(byte[] b) {
        return bitmapToDrawable(byteToBitmap(b));
    }

    /**
     * scale image
     *
     * @param org
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        return scaleImage(org, (float) newWidth / org.getWidth(), (float) newHeight / org.getHeight());
    }

    /**
     * scale image
     *
     * @param org
     * @param scaleWidth  sacle of width
     * @param scaleHeight scale of height
     * @return
     */
    public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
        if (org == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
    }


    /**
     * 压缩并且保存图片
     *
     * @param file
     * @param context
     * @param ww
     * @param hh
     */
    public static void scaleSaveFile(File file, Context context, float ww, float hh) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(),
                    options);
            int width = options.outWidth;
            int height = options.outHeight;
            int be = 1;
            if (width > height && width > ww) {
                be = (int) (options.outWidth / ww);
            } else if (width < height && height > hh) {
                be = (int) (options.outHeight / hh);
            }
            if (be < 0) {
                be = 1;
            }
            options.inSampleSize = be;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            if (bitmap != null) {
                // 保存图片
                FileOutputStream fos = null;
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 压缩并且保存图片
     *
     * @param file
     * @param context
     * @param ww
     * @param hh
     */
    public static void scaleSaveBmFile(File file, Context context, float ww, float hh) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(),
                    options);
            int width = options.outWidth;
            int height = options.outHeight;
            int be = 1;
            if (width > height && width > ww) {
                be = (int) (options.outWidth / ww);
            } else if (width < height && height > hh) {
                be = (int) (options.outHeight / hh);
            }
            if (be < 0) {
                be = 1;
            }
            options.inSampleSize = be;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            if (bitmap != null) {
                // 保存图片
                FileOutputStream fos1 = null;
                fos1 = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos1);
                fos1.flush();
                fos1.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show();
        }
    }

    //对Bitmap按照宽度进行缩放
    public static Drawable resizeImage(byte[] bytes, int w, int h) {
        if (bytes == null) {
            return null;
        }
        Bitmap BitmapOrg = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapOrg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        int width = options.outWidth;
        int height = options.outHeight;
        float size = (height * 1.0f) / (width * 1.0f);
        int newWidth = w;
        int newHeight = (int) (w * size);
        int be = 1;
        be = (int) (options.outWidth / newWidth);
        if ((options.outWidth * 1.0) / (newWidth * 1.0) < 1) {
            be = 1;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            options.inSampleSize = be;
            options.inJustDecodeBounds = false;
            BitmapOrg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            BitmapOrg = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
        } else {
            if (be < 0) {
                be = 1;
            }
            options.inSampleSize = be;
            options.inJustDecodeBounds = false;
            BitmapOrg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
        return new BitmapDrawable(BitmapOrg);
    }

    /**
     * @param urlpath
     * @return Bitmap
     * 根据图片url获取图片对象
     */
    public static byte[] getBitMBitmap(String urlpath) {
        byte[] bytes = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = in.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            bytes = swapStream.toByteArray();
        } catch (IOException e) {
            bytes = null;
        }
        return bytes;
    }
    // 图片质量默认值为100，表示不压缩
    //int quality = 100;
    // PNG是无损的，将会忽略质量设置。因此，这里设置为JPEG
    //BitmapOrg.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
    // 判断压缩后图片的大小是否大于100KB，大于则继续压缩
    //while (outStream.toByteArray().length / 1024 > 100) {
    //outStream.reset();
    // 压缩quality%，把压缩后的数据存放到baos中
    //BitmapOrg.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
    //quality -= 10;
    //}
}