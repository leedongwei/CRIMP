package rocks.crimp.crimp.persistence;

import android.support.annotation.NonNull;

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

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LocalModelImpl implements LocalModel {
    public static final String DISK_CACHE_FILE_DIR = "DiskLruCache";
    private static final int MEGABYTE = 1024 * 1024;

    private static LocalModel instance = null;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_SIZE = 2 * MEGABYTE; // 2MB
    private File mCacheFile;

    private LocalModelImpl(File path) {
        this.mCacheFile = path;

        //TODO Initialize disk cache on background thread
        initDiskCache();
    }

    public static LocalModel getInstance(File path){
        if(instance == null){
            instance = new LocalModelImpl(path);
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
        boolean result = getFromDiskCache(key)!=null;
        return result;
    }

    public boolean putData(String key, Serializable value){
        //TODO STUB
        putInDiskCache(key, value);
        return false;
    }

    public static void deleteLocalModel(File path){
        /*
        boolean deleteSucceed = true;
        if (path.isDirectory()) {
            String[] children = path.list();
            for (int i = 0; i < children.length; i++) {
                File childFile = new File(path, children[i]);
                deleteSucceed = deleteSucceed & childFile.delete();
            }
        }
        deleteSucceed = deleteSucceed & path.delete();

        path = null;
        */
    }

    public void deleteModel() {
        try {
            mDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initDiskCache(){
        synchronized (mDiskCacheLock) {
            try {
                mDiskLruCache = DiskLruCache.open(mCacheFile, 1, 1, DISK_CACHE_SIZE);
                return true;
            } catch (IOException e) {
                Timber.e(e, "IOException while opening DiskLruCache. Exception");
            }
            mDiskCacheLock.notifyAll(); // Wake any waiting threads
        }

        return false;
    }

    private Object getFromDiskCache(@NonNull String key){
        if(mDiskLruCache == null){
            Timber.d("mDiskLruCache not instantiated.");
            return null;
        }

        synchronized (mDiskCacheLock) {
            DiskLruCache.Snapshot ss = null;
            try {
                ss = mDiskLruCache.get(key);
            } catch (IOException e) {
                Timber.d("IOException trying to query disk cache for key: %s", key);
                return null;
            }

            if(ss == null){
                Timber.d("No entry found in disk cache for key: %s", key);
                return null;
            }

            InputStream cacheInputStream = ss.getInputStream(0);
            Object result = null;
            try {
                ObjectInputStream objInputStream =
                        new ObjectInputStream(new BufferedInputStream(cacheInputStream));

                result = objInputStream.readObject();
            } catch (IOException e) {
                Timber.d("IOException instantiating inputStream");
            } catch (ClassNotFoundException e) {
                Timber.d("ClassNotFoundException deserializing stream");
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
            Timber.d("mDiskLruCache not instantiated.");
            return false;
        }

        synchronized (mDiskCacheLock) {
            boolean result = false;
            try {
                result = mDiskLruCache.remove(key);
            } catch (IOException e) {
                Timber.d("IOException removing key: %s", key);
            }

            Timber.d("Remove [key:%s]? %s", key, result);

            mDiskCacheLock.notifyAll();
            return result;
        }
    }

    private boolean putInDiskCache(@NonNull String key, @NonNull Serializable obj){
        if(mDiskLruCache == null){
            Timber.d("mDiskLruCache not instantiated.");
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
                Timber.d("Unable to edit [key:%s] because another edit is in progress", key);
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
                Timber.e(e, "IOException trying to write [key:%s]", key);
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

}
