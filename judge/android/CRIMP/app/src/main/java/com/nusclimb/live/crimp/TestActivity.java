package com.nusclimb.live.crimp;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.SpinnerAdapterWithHint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private final String TAG = TestActivity.class.getSimpleName();

    private Spinner mSpinner1;
    private Spinner mSpinner2;
    private Spinner mSpinner3;
    private Spinner mSpinner4;
    private Spinner mSpinner5;
    private Spinner mSpinner6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
/*
        mSpinner1 = (Spinner) findViewById(R.id.spinner1);
        mSpinner2 = (Spinner) findViewById(R.id.spinner2);
        mSpinner3 = (Spinner) findViewById(R.id.spinner3);
        mSpinner4 = (Spinner) findViewById(R.id.spinner4);
        mSpinner5 = (Spinner) findViewById(R.id.spinner5);
        mSpinner6 = (Spinner) findViewById(R.id.spinner6);

        // Create an ArrayAdapter using the string array and a default spinner layout

        List<String> temp1 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list1 = new ArrayList(temp1);
        List<String> temp2 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list2 = new ArrayList(temp2);
        List<String> temp3 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list3 = new ArrayList(temp3);
        List<String> temp4 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list4 = new ArrayList(temp4);
        List<String> temp5 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list5 = new ArrayList(temp5);
        List<String> temp6 = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<String> list6 = new ArrayList(temp6);

        list1.add(getResources().getString(R.string.hello_activity_category_hint));
        list2.add(getResources().getString(R.string.hello_activity_category_hint));
        list3.add(getResources().getString(R.string.hello_activity_category_hint));
        list4.add(getResources().getString(R.string.hello_activity_category_hint));
        list5.add(getResources().getString(R.string.hello_activity_category_hint));
        list6.add(getResources().getString(R.string.hello_activity_category_hint));

        SpinnerAdapterWithHint adapter1 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list1);
        SpinnerAdapterWithHint adapter2 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list2);
        SpinnerAdapterWithHint adapter3 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list3);
        SpinnerAdapterWithHint adapter4 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list4);
        SpinnerAdapterWithHint adapter5 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list5);
        SpinnerAdapterWithHint adapter6 = new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, list6);

        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mSpinner1.setAdapter(adapter1);
        mSpinner2.setAdapter(adapter2);
        mSpinner3.setAdapter(adapter3);
        mSpinner4.setAdapter(adapter4);
        mSpinner5.setAdapter(adapter5);
        mSpinner6.setAdapter(adapter6);

        mSpinner1.setSelection(adapter1.getCount());
        mSpinner2.setSelection(adapter2.getCount());
        mSpinner3.setSelection(adapter3.getCount());
        mSpinner4.setSelection(adapter4.getCount());
        mSpinner5.setSelection(adapter5.getCount());
        mSpinner6.setSelection(adapter6.getCount());

        mSpinner1.setOnItemSelectedListener(this);
        mSpinner2.setOnItemSelectedListener(this);
        mSpinner3.setOnItemSelectedListener(this);
        mSpinner4.setOnItemSelectedListener(this);
        mSpinner5.setOnItemSelectedListener(this);
        mSpinner6.setOnItemSelectedListener(this);
        */
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d(TAG, "parent:" + parent.getId() + " view:" + view.getId() + " pos:" + pos + " id:" + id);
        view.setBackgroundColor(Color.CYAN);

        if(pos == ((SpinnerAdapterWithHint)parent.getAdapter()).getLastPosition()){
            // ((TextView)parent.getChildAt(0)).setTextColor(Color.rgb(148, 150, 148));
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.d(TAG, "parent:"+parent.getId());
    }


}
