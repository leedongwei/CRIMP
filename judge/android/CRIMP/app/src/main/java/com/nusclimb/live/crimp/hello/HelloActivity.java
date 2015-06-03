package com.nusclimb.live.crimp.hello;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelloActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = HelloActivity.class.getSimpleName();

    private String sessionToken;
    private String mText;

    // UI references
    private TextView mQuestionView;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private Spinner mDummyRouteSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_hello);

        sessionToken = getIntent().getStringExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_sessiontoken));

        mText = getString(R.string.hello_activity_greeting)+
            Profile.getCurrentProfile().getName()+
            getString(R.string.hello_activity_question);

        mQuestionView = (TextView) findViewById(R.id.hello_question);
        mCategorySpinner = (Spinner) findViewById(R.id.hello_category_spinner);
        mRouteSpinner = (Spinner) findViewById(R.id.hello_route_spinner);
        mDummyRouteSpinner = (Spinner) findViewById(R.id.hello_dummy_route_spinner);

        Log.d(TAG, "In onCreate. [category:"+R.id.hello_category_spinner+"] [route:"+
                R.id.hello_route_spinner+"] [dummy:"+R.id.hello_dummy_route_spinner+
                "] [sessionToken:"+sessionToken+"]");

        mQuestionView.setText(mText);
        setupCategorySpinner();
        setupDummySpinner();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupCategorySpinner(){
        // Create a list of categories
        List<String> categoryTempList = Arrays.asList(getResources().getStringArray(R.array.categories));
        List<SpinnerItem> mCategoryList = new ArrayList();
        for(String s:categoryTempList){
            mCategoryList.add(new SpinnerItem(s, false));
        }
        // Add hint to the list of categories
        mCategoryList.add(new SpinnerItem(getResources().getString(R.string.hello_activity_category_hint), true));

        // Create adapter using the list of categories
        SpinnerAdapterWithHint categoryAdapter= new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, mCategoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner, initialize selection to hint and attach listener.
        mCategorySpinner.setAdapter(categoryAdapter);
        mCategorySpinner.setSelection(categoryAdapter.getLastPosition());
        mCategorySpinner.setOnItemSelectedListener(this);
    }

    private void setupDummySpinner(){
        // Create an empty adapter
        ArrayAdapter<String> dummyAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item);
        dummyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Add hint to adapter
        dummyAdapter.add(getResources().getString(R.string.hello_activity_route_hint));

        // Apply the adapter to the spinner, initialize selection to hint and attach listener.
        mDummyRouteSpinner.setAdapter(dummyAdapter);
        mDummyRouteSpinner.setEnabled(false);
    }

    private void updateRouteSpinner(String category){
        // Find resource id of route list based on category
        String name = Helper.toAlphaNumeric(category);
        String defPackage = this.getPackageName();
        int id = getResources().getIdentifier(name, "array", defPackage);

        // Create a list of routes
        List<String> routeTempList = Arrays.asList(getResources().getStringArray(id));
        List<SpinnerItem> mRouteList = new ArrayList();
        for(String s:routeTempList){
            mRouteList.add(new SpinnerItem(s, false));
        }
        // Add hint to the list of routes
        mRouteList.add(new SpinnerItem(getResources().getString(R.string.hello_activity_route_hint), true));

        // Create adapter using the list of routes
        SpinnerAdapterWithHint routeAdapter= new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, mRouteList);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner, initialize selection to hint and attach listener.
        mRouteSpinner.setAdapter(routeAdapter);
        mRouteSpinner.setSelection(routeAdapter.getLastPosition());
        mRouteSpinner.setOnItemSelectedListener(this);
        mRouteSpinner.setVisibility(View.VISIBLE);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d(TAG, "OnItemSelected raw: "+parent.getId()+", "+view.getId()+", "+pos+", "+id);

        // Called from category spinner
        if(parent.getId() == mCategorySpinner.getId()){
            SpinnerItem selectedItem = (SpinnerItem)parent.getSelectedItem();
            if(selectedItem.isHint()){
                Log.d(TAG, "OnItemSelected. Category. Hint.");
                ((TextView)view).setTextColor(getResources().getColor(R.color.hint_color));
            }
            else{
                Log.d(TAG, "OnItemSelected. Category. Not hint.");
                if(mDummyRouteSpinner != null) {
                    ((LinearLayout)mDummyRouteSpinner.getParent()).removeView(mDummyRouteSpinner);
                    mDummyRouteSpinner = null;
                }
                updateRouteSpinner(selectedItem.getItemString());
            }
        }

        // Called from route spinner
        if(parent.getId() == mRouteSpinner.getId()){
            SpinnerItem selectedItem = (SpinnerItem)parent.getSelectedItem();
            if(selectedItem.isHint()) {
                Log.d(TAG, "OnItemSelected. Route. Hint.");
                ((TextView) view).setTextColor(getResources().getColor(R.color.hint_color));
            }
            else{
                Log.d(TAG, "OnItemSelected. Route. Not hint.");
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.d(TAG, "parent:"+parent.getId());
    }


}
