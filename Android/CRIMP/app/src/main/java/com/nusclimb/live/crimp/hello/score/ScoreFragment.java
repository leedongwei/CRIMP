package com.nusclimb.live.crimp.hello.Score;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreFragment extends Fragment {
    public static final String TAG = "ScoreFragment";
    public static final boolean DEBUG = true;

    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    public static ScoreFragment newInstance(int position, String title){
        ScoreFragment f = new ScoreFragment();
        // TODO set arguments
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_stub, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText(getArguments().getString(ARGS_TITLE));
    }
}
