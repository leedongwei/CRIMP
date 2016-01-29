package com.nusclimb.live.crimp.common;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.spicerequest.PostScoreRequest;
import com.nusclimb.live.crimp.uploadlist.UploadStatus;

/**
 * This class encapsulate each score submission to server containing information such as
 * upload status and timestamp. {@link com.nusclimb.live.crimp.CrimpApplication CrimpApplication}
 * maintain a queue of QueueObject representing each score submission.
 */
public class QueueObject {
    private PostScoreRequest request;
    private UploadStatus status;
    private String timeStamp;

    public QueueObject(String xUserId, String xAuthToken, String categoryId,
                       String routeId, String climberId, String currentScore, Context context){
        // Time stamp
        this.timeStamp = Helper.getCurrentTimeStamp();

        // Status
        this.status = UploadStatus.PAUSED;

        // Score request
        String url = String.format("%s%s%s/%s%s",context.getString(R.string.crimp_base_url),
                context.getString(R.string.post_score_api), categoryId, routeId, climberId);
        this.request = new PostScoreRequest(xUserId, xAuthToken, categoryId,
                routeId, climberId, currentScore, url);
    }

    public PostScoreRequest getRequest(){
        return request;
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
