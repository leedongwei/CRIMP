package com.nusclimb.live.crimp.hello.route;

import android.view.View;
import android.widget.AdapterView;

import com.nusclimb.live.crimp.common.Action;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class SpinnerListener implements AdapterView.OnItemSelectedListener{
    private Action itemSelectedAction;
    private Action nothingSelectedAction;

    public SpinnerListener(Action itemSelectedAction, Action nothingSelectedAction){
        this.itemSelectedAction = itemSelectedAction;
        this.nothingSelectedAction = nothingSelectedAction;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(itemSelectedAction != null){
            itemSelectedAction.act();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(nothingSelectedAction != null){
            nothingSelectedAction.act();
        }
    }
}
