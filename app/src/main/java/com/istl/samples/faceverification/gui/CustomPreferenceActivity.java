package com.istl.samples.faceverification.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class CustomPreferenceActivity extends AppCompatActivity  {

    CustomPreferenceFragment mFragment = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        mFragment = new CustomPreferenceFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();
    }



}
