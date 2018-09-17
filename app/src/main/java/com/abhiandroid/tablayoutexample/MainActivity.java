package com.abhiandroid.tablayoutexample;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FirstFragment.OnPauseListener {

    FrameLayout simpleFrameLayout;
    TabLayout tabLayout;
    Registrant registrant;
    FirstFragment firstFragment;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        registrant = new Registrant();

        initialize();
        // get the reference of FrameLayout and TabLayout
        simpleFrameLayout = (FrameLayout) findViewById(R.id.simpleFrameLayout);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        // Create a new Tab named "First"
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("Step1"); // set the Text for the first Tab
        // first tab
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout
        // Create a new Tab named "Second"
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Step2"); // set the Text for the second Tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout
        // Create a new Tab named "Third"
        TabLayout.Tab thirdTab = tabLayout.newTab();
        thirdTab.setText("Step3"); // set the Text for the first Tab
        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout
        // Create a new Tab named "Third"
        TabLayout.Tab fourTab = tabLayout.newTab();
        fourTab.setText("Step4"); // set the Text for the first Tab
        tabLayout.addTab(fourTab); // add  the tab at in the TabLayout

        firstFragment = new FirstFragment();
        firstFragment.setRegistrant(registrant);

        // perform setOnTabSelectedListener event on TabLayout
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // get the current selected tab's position and replace the fragment accordingly
                 fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        //firstFragment = new FirstFragment();
                        //firstFragment.setRegistrant(registrant);
                        fragment = firstFragment;
                        break;
                    case 1:
                        fragment = new SecondFragment();
                        break;
                    case 2:
                        fragment = new ThirdFragment();
                        break;
                    case 3:
                        fragment = new FourFragment();
                        break;
                }
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.simpleFrameLayout, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initialize() {

        fragment = new FirstFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleFrameLayout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onFragmentOnePause(String motherName, String fatherName, String hospitalName, String doctorName) {
        registrant.setStep1_motherName(motherName);
        registrant.setStep1_fatherName(fatherName);
        registrant.setStep1_hospital(hospitalName);
        registrant.setStep1_doctor(doctorName);
    }
}
