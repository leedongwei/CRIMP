package com.nusclimb.live.crimp.common;

import com.nusclimb.live.crimp.common.json.SessionUpload;
import com.nusclimb.live.crimp.common.spicerequest.UploadScoreRequest;
import com.nusclimb.live.crimp.common.spicerequest.UploadScoreSubmit;

public class QueueObject {
    private UploadScoreRequest request;
    private UploadScoreSubmit submit;
    private UploadStatus status;
    private String timeStamp;

    public QueueObject(String j_name, String auth_code,
                       String r_id, String c_id, String currentScore, int msgId){
        // Time stamp
        this.timeStamp = Helper.getCurrentTimeStamp();

        // Status
        this.status = UploadStatus.PAUSED;

        // Score request
        this.request = new UploadScoreRequest(msgId, c_id, r_id);
        //this.request.setRetryPolicy(new UploadRetryPolicy());

        // Score submit
        // Make POJO.
        SessionUpload uploadPOJO = new SessionUpload();
        uploadPOJO.setAll_current(j_name, auth_code,
                r_id, c_id, currentScore);
        // Prepare request.
        this.submit = new UploadScoreSubmit(uploadPOJO, msgId);
        //this.submit.setRetryPolicy(new UploadRetryPolicy());
    }

    public UploadScoreRequest getRequest(){
        return request;
    }

    public UploadScoreSubmit getSubmit(){
        return submit;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public UploadStatus getStatus(){
        return status;
    }

    public void setStatus(UploadStatus status){
        this.status = status;
    }
}
