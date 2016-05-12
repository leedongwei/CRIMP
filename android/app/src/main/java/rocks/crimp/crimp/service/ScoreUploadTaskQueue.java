package rocks.crimp.crimp.service;

import android.content.Context;

import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.FileObjectQueue.Converter;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreUploadTaskQueue extends TaskQueue<ScoreUploadTask> {
    private static final String FILENAME = "upload_task_queue";

    private final Context context;

    private ScoreUploadTaskQueue(ObjectQueue<ScoreUploadTask> delegate, Context context) {
        super(delegate);
        this.context = context;
    }

    public static ScoreUploadTaskQueue create(Context context) {
        Gson gson = new Gson();
        Converter<ScoreUploadTask> converter =
                new GsonConverter<>(gson, ScoreUploadTask.class);
        Timber.d("Created tape file at dir: %s, name: %s", context.getFilesDir(), FILENAME);
        File queueFile = new File(context.getFilesDir(), FILENAME);
        FileObjectQueue<ScoreUploadTask> delegate;
        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file queue.", e);
        }
        return new ScoreUploadTaskQueue(delegate, context);
    }
}