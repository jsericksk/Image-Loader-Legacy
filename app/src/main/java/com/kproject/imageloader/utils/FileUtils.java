package com.kproject.imageloader.utils;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.URLUtil;
import com.kproject.imageloader.application.MyApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;

public class FileUtils {

    public static String downloadImage(String downloadPath, String imageUrl, String imageName) throws IOException {
        byte[] bytes = new byte[2048];
        int size;
        InputStream input = null;
        OutputStream output = null;
        try {
            String fileName = "";
            boolean overwriteExistingFile = Utils.getPreferenceValue(Constants.PREF_OVERWRITE_EXISTING_FILE, false);
            if (overwriteExistingFile) {
                fileName = imageName.replaceAll("[^a-zA-Z0-9-_.\\s]", "-");
            } else {
                fileName = createValidFileName(downloadPath, imageName.replaceAll("[^a-zA-Z0-9-_.\\s]", "-"));
            }
            File fullDownloadPath = new File(downloadPath + "/" + fileName);
            input = new URL(imageUrl).openStream();
            output = new FileOutputStream(fullDownloadPath);
            while ((size = input.read(bytes)) != -1) {
                output.write(bytes, 0, size);
            }
            output.flush();
            scanSavedImage(fullDownloadPath);
            return fullDownloadPath.getPath();
        } finally {
            if (input != null) input.close();
            if (output != null) output.close();
        }
    }

    public static String createValidFileName(String downloadPath, String originalFileName) throws IOException {
        File originalDownloadPath = new File(downloadPath, originalFileName);
        if (!originalDownloadPath.exists()) {
            return originalDownloadPath.getName();
        } else {
            int number = 1;
            while (true) {
                String fileNameWithoutFormat = originalFileName.substring(0, originalFileName.lastIndexOf("."));
                String fileFormat = originalFileName.substring(originalFileName.lastIndexOf(".") + 1, originalFileName.length());
                String fileNameReplaced = String.format("%s (%d).%s", fileNameWithoutFormat, number, fileFormat);
                File newFileName = new File(downloadPath, fileNameReplaced);
                if (newFileName.exists()) {
                    number++;
                } else {
                    return newFileName.getName();
                }
            }
        }
    }

    // Escaneia a imagem para aparecer na galeria
    public static void scanSavedImage(File file) {
        MediaScannerConnection.scanFile(MyApplication.getContext(), new String[]{file.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                // Não faz nada
            }
        });
    }

    public static String formatFileSize(long size) {
        String fileSize;
        double b = size;
        double kb = size / 1024.0;
        double mb = ((size / 1024.0) / 1024.0);
        double gb = (((size / 1024.0) / 1024.0) / 1024.0);
        double tb = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (tb > 1) {
            fileSize = decimalFormat.format(tb).concat(" TB");
        } else if (gb > 1) {
            fileSize = decimalFormat.format(gb).concat(" GB");
        } else if (mb > 1) {
            fileSize = decimalFormat.format(mb).concat(" MB");
        } else if (kb > 1) {
            fileSize = decimalFormat.format(kb).concat(" KB");
        } else {
            fileSize = decimalFormat.format(b).concat(" bytes");
        }

        return fileSize;
    }

    public static String fileName(String fileUrl) {
        return URLUtil.guessFileName(fileUrl, null, null);
    }

    public static void createFolder(String folderName) {
        new File(folderName).mkdir();
    }

    public static String totalSize(String path) {
        File files = new File(path);
        long size = 0;
        for (File file : files.listFiles()) {
            size += file.length();
        }
        return formatFileSize(size);
    }

    public static String readTextFile(String filePath) throws IOException {
        BufferedReader reader = null;
        StringBuilder textFile = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                textFile.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return textFile.toString();
    }

}
