package com.kumaraswamy.cachemanager;

import android.app.Activity;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.*;

@DesignerComponent(version = 1,
        category = ComponentCategory.EXTENSION,
        description = "Save and read data from the cache memory",
        nonVisible = true,
        iconName = "")

@SimpleObject(external = true)

public class CacheManager extends AndroidNonvisibleComponent {

    private Activity activity;
    private File cacheTextMemory, cacheFileMemory;

    private int TEXT_TYPE_DATA = 1, FILE_TYPE_DATA = 2;

    public CacheManager(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        cacheTextMemory = new File(activity.getCacheDir(), "/textData/");
        cacheTextMemory.mkdirs();
        cacheFileMemory = new File(activity.getCacheDir(), "/fileData/");
        cacheFileMemory.mkdirs();
    }

    @SimpleFunction(
            description = "Save text data into the cache memory")
    public void SaveText(String text, String tag) {
        File path = new File(cacheTextMemory, tag + ".txt");
        if(path.exists()) path.delete();
        writeData(path, text.getBytes());
        SavedData(TEXT_TYPE_DATA, tag);
    }

    @SimpleFunction(
            description = "Read text data from the cache memory")
    public String ReadText(String tag) {
        File path = new File(cacheTextMemory, tag + ".txt");
        byte[] textData = readData(path);
        return new String(textData);
    }

    @SimpleFunction(
            description = "Delete the saved data from the cache memory")
    public boolean RemoveTextData(String tag) {
        File path = new File(cacheTextMemory, tag + ".txt");
        return path.delete();
    }

    @SimpleFunction(
            description = "Delete the saved file data from the cache memory")
    public boolean RemoveFileData(String tag) {
        File path = new File(cacheFileMemory, tag + ".txt");
        return path.delete();
    }

    @SimpleFunction(
            description = "Save file data to the cache memory")
    public void SaveFile(String fileName, String tag) {
        File path = new File(cacheFileMemory, tag + ".txt");
        if(path.exists()) path.delete();
        writeData(path, readData(new File(fileName)));
        SavedData(FILE_TYPE_DATA, tag);
    }

    @SimpleFunction(
            description = "Get the saved file data from the cache memory")
    public void ReadFile(String tag, String saveLocation) {
        File path = new File(cacheFileMemory, tag + ".txt");
        String name = path.getName();
        writeData(new File(saveLocation, name.substring(0, name.length() - 4)), readData(path));
        FileRead(tag, path.getAbsolutePath());
    }

    @SimpleFunction(
            description = "Delete the cache memory including all data")
    public boolean DeleteCacheMemory() {
        return deleteDir(activity.getCacheDir().getParentFile());
    }

    // THANKS TO : https://stackoverflow.com/a/58085806/14461795

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @SimpleProperty(
            description = "Text type data")
    public int TextTypeData() {
        return TEXT_TYPE_DATA;
    }

    @SimpleProperty(
            description = "File type data")
    public int FileTypeData() {
        return FILE_TYPE_DATA;
    }

    @SimpleEvent(
            description = "Raised when saved data to the cache memory")
    public void SavedData(int format, String tag) {
        EventDispatcher.dispatchEvent(this, "SavedData", format, tag);
    }

    @SimpleEvent(
            description = "Raised when saved data from the cache memory is saved to the location given")
    public void FileRead(String tag, String output) {
        EventDispatcher.dispatchEvent(this, "FileRead", tag, output);
    }

    @SimpleEvent(
            description = "Raised when operation failed")
    public void Error(String message) {
        EventDispatcher.dispatchEvent(this, "Error", message);
    }

    private byte[] readData(File path) {
        byte[] fileData = null;
        try {
            FileInputStream stream = new FileInputStream(path);
            fileData = new byte[stream.available()];
            stream.read(fileData);
            stream.close();
        } catch (IOException e) {
            Error(e.getMessage());
        }
        return fileData;
    }

    private void writeData(File path, byte[] data) {
        try {
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            Error(e.getMessage());
        }
    }
}
