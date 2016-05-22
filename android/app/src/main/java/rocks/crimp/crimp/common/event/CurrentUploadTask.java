package rocks.crimp.crimp.common.event;

import rocks.crimp.crimp.network.model.RequestBean;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CurrentUploadTask {
    public final int taskCountLeft;
    public final RequestBean currentTask;
    public final String status;

    public CurrentUploadTask(int taskCountLeft, RequestBean currentTask, String status){
        this.taskCountLeft = taskCountLeft;
        this.currentTask = currentTask;
        this.status = status;
    }
}
