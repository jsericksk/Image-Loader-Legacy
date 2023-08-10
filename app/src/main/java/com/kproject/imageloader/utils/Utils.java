package com.kproject.imageloader.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;
import com.kproject.imageloader.R;
import com.kproject.imageloader.application.MyApplication;

import java.io.File;

public class Utils {

    public static void showToast(String str) {
        Toast.makeText(MyApplication.getContext(), str, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String str) {
        Toast.makeText(MyApplication.getContext(), str, Toast.LENGTH_LONG).show();
    }

    public static void showToast(int resId) {
        Toast.makeText(MyApplication.getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(int resId) {
        Toast.makeText(MyApplication.getContext(), resId, Toast.LENGTH_LONG).show();
    }

    private static void setThemeDialog(Activity activity) {
        activity.setTheme(setThemeForDialog());
    }

    public static String getThemeSelected() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String themeSelected = prefs.getString(Constants.PREF_APP_THEME, Constants.THEME_DARK);
        return themeSelected;
    }

    public static int setThemeForActivity() {
        String theme = getThemeSelected();
        switch (theme) {
            case Constants.THEME_RED:
                return R.style.ThemeRed;
            case Constants.THEME_GREEN:
                return R.style.ThemeGreen;
            case Constants.THEME_BLUE:
                return R.style.ThemeBlue;
            case Constants.THEME_DARK:
                return R.style.ThemeDark;
            case Constants.THEME_PINK:
                return R.style.ThemePink;
            case Constants.THEME_PURPLE:
                return R.style.ThemePurple;
        }
        return R.style.ThemeDark;
    }

    public static int setThemeForToolbar() {
        String theme = getThemeSelected();
        switch (theme) {
            case Constants.THEME_DARK:
                return R.style.ThemeToolbarDark;
            case Constants.THEME_RED:
                return R.style.ThemeToolbarRed;
            case Constants.THEME_GREEN:
                return R.style.ThemeToolbarGreen;
            case Constants.THEME_BLUE:
                return R.style.ThemeToolbarBlue;
            case Constants.THEME_PINK:
                return R.style.ThemeToolbarPink;
            case Constants.THEME_PURPLE:
                return R.style.ThemeToolbarPurple;
        }
        return R.style.ThemeToolbarDark;
    }

    public static int setThemeForDialog() {
        String theme = getThemeSelected();
        switch (theme) {
            case Constants.THEME_DARK:
                return R.style.ThemeDialogDark;
            case Constants.THEME_RED:
                return R.style.ThemeDialogRed;
            case Constants.THEME_GREEN:
                return R.style.ThemeDialogGreen;
            case Constants.THEME_BLUE:
                return R.style.ThemeDialogBlue;
            case Constants.THEME_PINK:
                return R.style.ThemeDialogPink;
            case Constants.THEME_PURPLE:
                return R.style.ThemeDialogPurple;
        }
        return R.style.ThemeDialogDark;
    }

    public static String getBackgroundColor() {
        String color = getPreferenceValue(Constants.PREF_BACKGROUND_COLOR, Constants.COLOR_GREY);
        String currentColor = "";
        switch (color) {
            case Constants.COLOR_RED:
                currentColor = "#C62828";
                break;
            case Constants.COLOR_GREEN:
                currentColor = "#2E7D32";
                break;
            case Constants.COLOR_BLUE:
                currentColor = "#1976D2";
                break;
            case Constants.COLOR_BLACK:
                currentColor = "#000000";
                break;
            case Constants.COLOR_WHITE:
                currentColor = "#FFFFFF";
                break;
            case Constants.COLOR_GREY:
                currentColor = "#383838";
                break;
            case Constants.COLOR_PINK:
                currentColor = "#F06292";
                break;
            case Constants.COLOR_PURPLE:
                currentColor = "#8E24AA";
                break;

        }
        return currentColor;
    }

    public static float getZoomLevel() {
        int zoom = Integer.parseInt(getPreferenceValue(Constants.PREF_ZOOM_LEVEL, "0"));
        float currentZoomLevel = 0f;
        switch (zoom) {
            case Constants.ZOOM_LEVEL_DEFAULT:
                currentZoomLevel = 3f;
                break;
            case Constants.ZOOM_LEVEL_SMALL:
                currentZoomLevel = 2f;
                break;
            case Constants.ZOOM_LEVEL_MEDIUM:
                currentZoomLevel = 5f;
                break;
            case Constants.ZOOM_LEVEL_BIG:
                currentZoomLevel = 7f;
                break;

        }
        return currentZoomLevel;
    }

    // Site com UAs: https://developers.whatismybrowser.com/useragents/explore
    public static String getUserAgent() {
        int ua = Integer.parseInt(getPreferenceValue(Constants.PREF_USER_AGENT, "2"));
        String currentUserAgent = "";
        switch (ua) {
            case Constants.UA_CHROME:
                currentUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36";
                break;
            case Constants.UA_OPERA:
                currentUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36 OPR/56.0.3051.52";
                break;
            case Constants.UA_OFF:
                currentUserAgent = "OFF";
                break;
            case Constants.UA_FIREFOX:
                currentUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";
                break;
            case Constants.UA_INTERNET_EXPLORER:
                currentUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
                break;

        }
        return currentUserAgent;
    }

    public static String colorPrimary() {
        String theme = getPreferenceValue(Constants.PREF_APP_THEME, Constants.THEME_DARK);
        switch (theme) {
            case Constants.THEME_DARK:
                return "#B9252525";
            case Constants.THEME_RED:
                return "#B9C62828";
            case Constants.THEME_GREEN:
                return "#B92E7D32";
            case Constants.THEME_BLUE:
                return "#B91976D2";
            case Constants.THEME_PINK:
                return "#B9F06292";
            case Constants.THEME_PURPLE:
                return "#B98E24AA";
            default:
                return "#B9424242";
        }
    }

    public static String getDownloadPath() {
        File defaultPath = new File(Environment.getExternalStorageDirectory().toString() + "/Image Loader");
        defaultPath.mkdirs();
        return getPreferenceValue(Constants.PREF_DOWNLOAD_PATH, defaultPath.getPath());
    }

    public static String getPreferenceValue(String prefKey, String prefDefaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        return prefs.getString(prefKey, prefDefaultValue);
    }

    public static boolean getPreferenceValue(String prefKey, boolean prefDefaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        return prefs.getBoolean(prefKey, prefDefaultValue);
    }

    public static void setPreferenceValue(String prefKey, String prefValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        prefs.edit().putString(prefKey, prefValue).commit();
    }

}
