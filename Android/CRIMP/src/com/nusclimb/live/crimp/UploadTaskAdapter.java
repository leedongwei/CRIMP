package com.nusclimb.live.crimp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Subclass of ArrayAdapter to be used to feed list of upload tasks to an 
 * activity.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadTaskAdapter extends ArrayAdapter<QueueObject>{
	private final Context context;
	private final List<QueueObject> dataList;
	
	public UploadTaskAdapter(Context context, List<QueueObject> dataList){
		super(context, R.layout.uploadlist_row, dataList);
	    this.context = context;
	    this.dataList = dataList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.uploadlist_row, parent, false);
	    
	    TextView cIdView = (TextView) rowView.findViewById(R.id.row_cid);
	    TextView rIdView = (TextView) rowView.findViewById(R.id.row_rid);
	    TextView statusView = (TextView) rowView.findViewById(R.id.row_status);
	    TextView timeView = (TextView) rowView.findViewById(R.id.row_time);
	    ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.row_progress_bar);
	    
	    cIdView.setText(context.getString(R.string.UI_climber) + 
	    		dataList.get(position).getRequest().getClimberId());
	    rIdView.setText(context.getString(R.string.UI_route) + 
	    		dataList.get(position).getRequest().getR_id());
	    statusView.setText(context.getString(R.string.UI_status) + 
	    		dataList.get(position).getStatus().toString());
	    timeView.setText(dataList.get(position).getTimeStamp());
	    progressBar.setIndeterminate(true);
	    if(position != 0){
	    	progressBar.setVisibility(View.GONE);
	    }
	    else if(dataList.get(position).getStatus() != UploadStatus.DOWNLOAD &&
	    		dataList.get(position).getStatus() != UploadStatus.DOWNLOAD_WAITING &&
	    		dataList.get(position).getStatus() != UploadStatus.UPLOAD &&
	    		dataList.get(position).getStatus() != UploadStatus.UPLOAD_WAITING ){
	    	progressBar.setVisibility(View.INVISIBLE);
	    }
	    else{
	    	progressBar.setVisibility(View.VISIBLE);
	    }
	    
	    return rowView;
	}
}
