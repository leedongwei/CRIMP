package com.nusclimb.live.crimp.persistence;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LocalModelImpl implements LocalModel {
    private static final String TAG = "LocalModelImpl";
    private static final boolean DEBUG = true;

    private static final String DISK_CACHE_SUBDIR = null;

    private Context mContext;
    private static LocalModel instance = null;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 1; // 1MB
    private File cacheFile;

    private LocalModelImpl(Context context) {
        this.mContext = context;

        // Initialize disk cache on background thread
        cacheFile = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
    }

    public static LocalModel getInstance(Context context){
        if(instance == null){
            instance = new LocalModelImpl(context);
        }

        return instance;
    }

    public <T> T fetch(String txId, Class<T> valueType){
        //TODO
        //getFromDiskCache(T)
        return null;
    }

    @Override
    public boolean isDataExist(String key){
        // TODO STUB
        return false;
    }

    public boolean putData(String key, Serializable value){
        //TODO STUB
        return false;
    }


    private boolean initDiskCache(){
        synchronized (mDiskCacheLock) {
            try {
                mDiskLruCache = DiskLruCache.open(cacheFile, 1, 1, DISK_CACHE_SIZE);
                return true;
            } catch (IOException e) {
                if(DEBUG) Log.d(TAG, "IOException while opening DiskLruCache. Exception: "
                        + e.getMessage());
            }
            mDiskCacheLock.notifyAll(); // Wake any waiting threads
        }

        return false;
    }

    private Object getFromDiskCache(@NonNull String key){
        if(mDiskLruCache == null){
            if(DEBUG) Log.d(TAG, "mDiskLruCache not instantiated.");
            return null;
        }

        synchronized (mDiskCacheLock) {
            DiskLruCache.Snapshot ss = null;
            try {
                ss = mDiskLruCache.get(key);
            } catch (IOException e) {
                if(DEBUG) Log.d(TAG, "IOException trying to query disk cache for key: " + key);
                return null;
            }

            if(ss == null){
                if(DEBUG) Log.d(TAG, "No entry found in disk cache for key: " + key);
                return null;
            }

            InputStream cacheInputStream = ss.getInputStream(0);
            Object result = null;
            try {
                ObjectInputStream objInputStream =
                        new ObjectInputStream(new BufferedInputStream(cacheInputStream));

                result = objInputStream.readObject();
            } catch (IOException e) {
                if(DEBUG) Log.d(TAG, "IOException instantiating inputStream");
            } catch (ClassNotFoundException e) {
                if(DEBUG) Log.d(TAG, "ClassNotFoundException deserializing stream");
            } finally {
                ss.close();
            }

            mDiskCacheLock.notifyAll();
            return result;
        }
    }

    /**
     *
     * @param key
     * @return true if entry is removed.
     */
    private boolean removeFromDiskCache(@NonNull String key){
        if(mDiskLruCache == null){
            if(DEBUG) Log.d(TAG, "mDiskLruCache not instantiated.");
            return false;
        }

        synchronized (mDiskCacheLock) {
            boolean result = false;
            try {
                result = mDiskLruCache.remove(key);
            } catch (IOException e) {
                if(DEBUG) Log.d(TAG, "IOException removing key: " + key);
            }

            if(DEBUG) Log.d(TAG, "Remove [key:"+key+"]? "+result);

            mDiskCacheLock.notifyAll();
            return result;
        }
    }

    private boolean putInDiskCache(@NonNull String key, @NonNull Serializable obj){
        if(mDiskLruCache == null){
            if(DEBUG) Log.d(TAG, "mDiskLruCache not instantiated.");
            return false;
        }

        boolean removeResult = removeFromDiskCache(key);

        synchronized (mDiskCacheLock){
            DiskLruCache.Editor editor = null;
            try {
                editor = mDiskLruCache.edit(key);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(editor == null){
                if(DEBUG) Log.d(TAG, "Unable to edit [key:"+key+"] because another edit is in progress");
                return false;
            }

            OutputStream cacheOut = null;
            BufferedOutputStream bufferedOut = null;
            ObjectOutputStream objectOut = null;

            boolean writeResult = false;
            try {
                cacheOut = editor.newOutputStream(0);
                bufferedOut = new BufferedOutputStream(cacheOut);
                objectOut = new ObjectOutputStream(bufferedOut);
                objectOut.writeObject(obj);
                writeResult = true;
            } catch (IOException e){
                if(DEBUG) Log.d(TAG, "IOException trying to write [key:"+key+"]. e:"+e.getMessage());
            } finally {
                if(objectOut != null) try {
                    objectOut.close();
                } catch (IOException e) {
                }

                if(bufferedOut != null) try {
                    bufferedOut.close();
                } catch (IOException e) {
                }

                if(cacheOut != null) try {
                    cacheOut.close();
                } catch (IOException e) {
                }
            }

            if(writeResult){
                try {
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    editor.abort();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mDiskCacheLock.notifyAll();
            return writeResult;
        }
    }

    private static File getDiskCacheDir(Context context, String subDir){
        File cacheFile;

        if(DISK_CACHE_SUBDIR == null){
            cacheFile = context.getCacheDir();
        }
        else{
            cacheFile = new File(context.getCacheDir() + File.separator + subDir);
        }

        return cacheFile;
    }

}
