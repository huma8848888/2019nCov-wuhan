package com.ncov.wuhan.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Description FileUtils
 * @Date 2019-05-31
 * @Author sz
 */
public class FileUtils {
    public static final String CACHE_PATH_SEGMENT_IMAGE = "image";
    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 清空文件
     *
     * @param fileName
     */
    public static boolean clearInfoForFile(String directory, String fileName) {
        File file = new File(directory, fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }


    /**
     * 保存内容到文件
     *
     * @param directory
     * @param fileName
     * @param content
     *
     * @return
     *
     * @throws IOException
     */
    public static String saveContentToFile(String directory, String fileName, String content)
            throws IOException {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        File file = null;
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileWriter output = null;
        file = new File(directory, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        String sb = null;
        try {
            output = new FileWriter(file, true);
            output.write(content);
            output.flush();
            sb = buf.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            FileUtils.close(output);
        }
        return sb;

    }

    /**
     * 根据文件路径，得到文件的内容
     *
     * @param directory
     * @param fileName
     *
     * @return
     *
     * @throws IOException
     */
    public static String getFileContent(String directory, String fileName) throws IOException {
        StringBuffer buf = new StringBuffer();
        File file = new File(directory, fileName);
        if (!file.exists()) {
            return "";
        }
        FileInputStream fileInputStream = new FileInputStream(new File(directory, fileName));
        InputStreamReader input = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader br = null;
        String sb = null;
        try {
            br = new BufferedReader(input);
            String str = null;
            while ((str = br.readLine()) != null) {
                buf.append(str);
            }
            sb = buf.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            FileUtils.close(br);
        }
        return sb;
    }

    /**
     * 保存数据
     *
     * @param outSream
     * @param content  输入流
     *
     * @throws
     */
    public static void save(OutputStream outSream, String content) throws Exception {
        outSream.write(content.getBytes());
        outSream.close();
    }

    /**
     * 根据URI得到真实的图片路径
     *
     * @param uri 图片URI
     *
     * @return SD卡中图片的路径
     */
    public static String getRealPathFromURI(@NonNull Context context, @NonNull Uri uri) {
        String[] as = new String[1];
        as[0] = "_data";
        Uri uri1 = uri;
        String[] as1 = null;
        String s = null;
        Cursor cursor = null;

        try {
            cursor = context.getApplicationContext().getContentResolver().query(uri1, as,
                    null, as1, s);
            if (cursor != null) {
                int i = cursor.getColumnIndexOrThrow("_data");
                cursor.moveToFirst();
                return cursor.getString(i);
            } else {
                String uriString = uri.toString();
                String pStr = "file://";
                int pos = uriString.lastIndexOf(pStr);
                uriString = uriString.substring(pStr.length() + pos);
                return uriString;
            }
        } finally {
            FileUtils.close(cursor);
        }
    }

    /**
     * 这里返回的单位为MB
     *
     * @param root
     *
     * @return
     */
    public static long getCapability(File root) {
        long result = 0;
        if (root != null && root.exists()) {
            StatFs sf = new StatFs(root.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();

            Log.d("" + root, "block大小:" + blockSize + ",block数目:" + blockCount
                    + ",总大小:" + blockSize * blockCount / 1024 / 1024 + "MB");
            Log.d("" + root, "可用的block数目：:" + availCount + ",可用大小:" + availCount * blockSize / 1024 / 1024 + "MB");
            result = availCount * blockSize / 1024 / 1024;
        }

        return result;
    }

    /**
     * 获取文件或文件夹大小
     *
     * @param file
     *
     * @return 单位"MB"
     */
    public static double getFileSizes(File file) {
        double size = 0;
        if (file == null) {
            return size;
        }
        File[] flist = file.listFiles();
        if (flist == null) {
            return size;
        }
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size / 1024 / 1024;
    }

    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (null != closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("FileUtils", "close is exception", e);
                }
            }
        }
    }

    public static File getFileDir(Context context, String dirName) {
        String directory =  context.getApplicationContext().getFilesDir().getAbsolutePath();

        File dir = new File(directory + "/" + dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String generateFileName() {
        return  ".test";
    }
}
