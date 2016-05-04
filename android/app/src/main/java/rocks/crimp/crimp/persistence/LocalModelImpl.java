package rocks.crimp.crimp.persistence;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.network.model.CategoriesJs;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LocalModelImpl implements LocalModel {
    public static final String DISK_CACHE_FILE_DIR = "DiskLruCache";
    public static final String CATEGORIES_FILE = "categories_file";
    private static final int MEGABYTE = 1024 * 1024;
    private static final int DISK_CACHE_SIZE = 2 * MEGABYTE; // 2MB

    private static LocalModel instance = null;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private File mCacheFileDir;

    private LocalModelImpl() {}

    public static LocalModel getInstance(){
        if(instance == null){
            instance = new LocalModelImpl();
        }

        return instance;
    }

    @Override
    public boolean setupModel(File directory){
        this.mCacheFileDir = directory;
        boolean isSucceed = initDiskCache();
        return isSucceed;
    }

    @Override
    public <T> T fetch(String key, Class<T> valueType){
        Object obj = getFromDiskCache(key);
        T result = null;
        try{
            result = valueType.cast(obj);
        } catch(ClassCastException e){
            result = null;
        }
        return result;
    }

    @Override
    public boolean isDataExist(String key){
        boolean result = getFromDiskCache(key)!=null;
        return result;
    }

    @Override
    public boolean putData(String key, Serializable value){
        boolean result = putInDiskCache(key, value);
        return result;
    }

    @Override
    public boolean deleteModel() {
        try {
            mDiskLruCache.delete();
            return mCacheFileDir.delete();
        } catch (IOException e) {
            Timber.e(e, "IOException trying to delete DiskLruCache");
        }

        return false;
    }

    @Override
    public boolean saveCategoriesAndCloseStream(ObjectOutputStream outputStream, CategoriesJs categoriesJs){
        boolean result = false;

        try {
            outputStream.writeObject(categoriesJs);
            result = true;
        } catch (IOException e) {
            Timber.e(e, "IOException trying to write categories");
            result = false;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close stream. Nothing we can do.");
            }
        }

        return result;
    }

    @Override
    public CategoriesJs loadCategoriesAndCloseStream(ObjectInputStream inputStream){
        if(inputStream == null){
            return null;
        }

        CategoriesJs result = null;

        try {
            Object obj = inputStream.readObject();
            if(obj instanceof CategoriesJs){
                result = (CategoriesJs) obj;
            }
        } catch (ClassNotFoundException e) {
            Timber.e(e, "ClassNotFoundException trying to read categories");
        } catch (IOException e) {
            Timber.e(e, "IOException trying to read categories");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Timber.e(e, "IOException closing stream. Nothing we can do.");
            }
        }

        return result;
    }

    private boolean initDiskCache(){
        synchronized (mDiskCacheLock) {
            try {
                mDiskLruCache = DiskLruCache.open(mCacheFileDir, 1, 1, DISK_CACHE_SIZE);
                return true;
            } catch (IOException e) {
                Timber.e(e, "IOException while opening DiskLruCache. Exception");
            }
            mDiskCacheLock.notifyAll(); // Wake any waiting threads
        }

        return false;
    }

    @Nullable
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
                Timber.e(e, "IOException instantiating inputStream");
            } catch (ClassNotFoundException e) {
                Timber.e(e, "ClassNotFoundException deserializing stream");
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
                Timber.e(e, "IOException removing key: %s", key);
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

        synchronized (mDiskCacheLock){
            DiskLruCache.Editor editor = null;
            try {
                editor = mDiskLruCache.edit(key);
            } catch (IOException e) {
                Timber.e(e, "IOException trying to get an editor for key:%s", key);
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
                    Timber.e(e, "IOException trying to close ObjectOutputStream. Nothing we can do");
                }

                if(bufferedOut != null) try {
                    bufferedOut.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException trying to close BufferedOutputStream. Nothing we can do");
                }

                if(cacheOut != null) try {
                    cacheOut.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException trying to close OutputStream. Nothing we can do");
                }
            }

            if(writeResult){
                try {
                    editor.commit();
                } catch (IOException e) {
                    Timber.e(e, "IOException trying to commit our edit for key:%s", key);
                    return false;
                }
            }
            else{
                try {
                    editor.abort();
                } catch (IOException e) {
                    Timber.e(e, "IOException trying to abort our edit for key:%s", key);
                    return false;
                }
            }

            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to flush DiskLruCache for key:%s", key);
                return false;
            }

            mDiskCacheLock.notifyAll();
            return writeResult;
        }
    }

    public static File getLocalModelDir(Context context){
        File cacheDir = context.getCacheDir();
        File localModelDir;
        if(cacheDir == null){
            localModelDir =  new File(DISK_CACHE_FILE_DIR);
        }
        else{
            localModelDir = new File(context.getCacheDir() + File.separator + DISK_CACHE_FILE_DIR);
        }
        return localModelDir;
    }

    public static ObjectOutputStream getOutputStream(Context context){
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(CATEGORIES_FILE, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
        } catch (FileNotFoundException e1) {
            Timber.e(e1, "FileNotFoundException trying to open '%s' for writing categories",
                    CATEGORIES_FILE);
            if(oos != null) try {
                oos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close ObjectOutputStream. Nothing we can do");
            }
            oos = null;

            if(bos != null) try {
                bos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close BufferedOutputStream. Nothing we can do");
            }
            bos = null;

            if(fos != null) try {
                fos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close FileOutputStream. Nothing we can do");
            }
            fos = null;
        } catch (IOException e1) {
            Timber.e(e1, "IOException trying to instantiate ObjectOutputStream");
            if(oos != null) try {
                oos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close ObjectOutputStream. Nothing we can do");
            }
            oos = null;

            if(bos != null) try {
                bos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close BufferedOutputStream. Nothing we can do");
            }
            bos = null;

            if(fos != null) try {
                fos.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close FileOutputStream. Nothing we can do");
            }
            fos = null;
        }

        return oos;
    }

    public static ObjectInputStream getInputStream(Context context){
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(CATEGORIES_FILE);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
        } catch (FileNotFoundException e1) {
            Timber.e(e1, "FileNotFoundException trying to open '%s' for reading categories",
                    CATEGORIES_FILE);
            if(ois != null) try {
                ois.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close ObjectInputStream. Nothing we can do");
            }
            ois = null;

            if(bis != null) try {
                bis.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close BufferedInputStream. Nothing we can do");
            }
            bis = null;

            if(fis != null) try {
                fis.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close FileInputStream. Nothing we can do");
            }
            fis = null;
        } catch (IOException e1) {
            Timber.e(e1, "IOException trying to instantiate ObjectInputStream");
            if(ois != null) try {
                ois.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close ObjectInputStream. Nothing we can do");
            }
            ois = null;

            if(bis != null) try {
                bis.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close BufferedInputStream. Nothing we can do");
            }
            bis = null;

            if(fis != null) try {
                fis.close();
            } catch (IOException e) {
                Timber.e(e, "IOException trying to close FileInputStream. Nothing we can do");
            }
            fis = null;
        }

        return ois;
    }
}
