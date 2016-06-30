package rocks.crimp.crimp.common.event;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.network.model.RequestBean;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CurrentUploadTask {
    public static final int IDLE = 1;
    public static final int UPLOADING = 2;
    public static final int ERROR_HTTP_STATUS = 3;
    public static final int ERROR_EXCEPTION = 4;
    public static final int ERROR_NO_NETWORK = 5;
    public static final int UPLOAD_SUCCEED = 6;
    public static final int DROP_TASK = 7;

    //public final int taskCountLeft;
    public final RequestBean currentTask;
    public final int status;
    public final int httpStatusCode;
    public final String httpMessage;
    public final Exception exception;

    public CurrentUploadTask(RequestBean currentTask, int status){
        //this.taskCountLeft = taskCountLeft;
        this.currentTask = currentTask;
        this.status = status;
        this.httpStatusCode = 0;
        this.httpMessage = null;
        this.exception = null;
    }

    public CurrentUploadTask(RequestBean currentTask, int httpStatusCode,
                             String httpMessage){
        //this.taskCountLeft = taskCountLeft;
        this.currentTask = currentTask;
        this.status = ERROR_HTTP_STATUS;
        this.httpStatusCode = httpStatusCode;
        this.httpMessage = httpMessage;
        this.exception = null;
    }

    public CurrentUploadTask(RequestBean currentTask, Exception exception){
        //this.taskCountLeft = taskCountLeft;
        this.currentTask = currentTask;
        this.status = ERROR_EXCEPTION;
        this.httpStatusCode = 0;
        this.httpMessage = null;
        this.exception = exception;
    }
}
