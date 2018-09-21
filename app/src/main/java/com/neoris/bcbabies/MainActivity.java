package com.neoris.bcbabies;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity implements FirstFragment.OnPauseListener,
        SecondFragment.OnPauseListener, ThirdFragment.OnPauseListener, FourFragment.OnSaveListener{

    FrameLayout simpleFrameLayout;
    TabLayout tabLayout;
    Registrant registrant;
    Fragment fragment;
    RequestQueue requestQueue;
    Fragment fragmentOne;
    Fragment fragmentTwo;
    Fragment fragmentThree;
    Fragment fragmentFour;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        registrant = new Registrant();
        requestQueue = Volley.newRequestQueue(this);
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

        // perform setOnTabSelectedListener event on TabLayout
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // get the current selected tab's position and replace the fragment accordingly
                switch (tab.getPosition()) {
                    case 0:
                        fragment = fragmentOne;
                        break;
                    case 1:
                        fragment = fragmentTwo;
                        break;
                    case 2:
                        fragment = fragmentThree;
                        break;
                    case 3:
                        fragment = fragmentFour;
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
        fragmentOne = new FirstFragment();
        fragmentTwo = new SecondFragment();
        fragmentThree = new ThirdFragment();
        fragmentFour = new FourFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleFrameLayout, fragmentOne);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onFirstFragmentPause(String hospitalName, String doctorName, String date, String hour,
                                   String country, String address) {
        registrant.setStep1_hospital(hospitalName);
        registrant.setStep1_doctor(doctorName);
        registrant.setStep1_date(date);
        registrant.setStep1_hour(hour);
        registrant.setStep1_country(country);
        registrant.setStep1_address(address);
    }

    @Override
    public void onSecondFragmentPause(String newBornName, String fingerPrintHash, Integer gender) {
        registrant.setStep2_newBornName(newBornName);
        registrant.setStep2_gender(gender);
        registrant.setStep2_newbornFinger(fingerPrintHash);
    }

    @Override
    public void onThirdFragmentPause(String motherName, String fingerPrintHash,
                                     String motherIneFront, String motherIneBack) {
        registrant.setStep3_motherName(motherName);
        registrant.setStep3_motherFinger(fingerPrintHash);
        registrant.setStep3_motherIneBackB64(motherIneBack);
        registrant.setStep3_motherIneFrontB64(motherIneBack);
    }

    @Override
    public void saveInfo(String fatherName, String fingerPrintHash, String ineFrontB64, String ineBackB64) {
        registrant.setStep4_fatherName(fatherName);
        registrant.setStep4_fatherFinger(fingerPrintHash);
        registrant.setStep4_fatherIneFrontB64(ineFrontB64);
        registrant.setStep4_fatherIneBackB64(ineBackB64);

        final String url = "http://10.15.29.164:3000/API/babies/registerV2";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("registeredName", registrant.getStep2_newBornName());
                params.put("babyHashFingerprint", registrant.getStep2_newbornFinger());
                params.put("motherHashFingerprint", registrant.getStep3_motherFinger());
                params.put("motherName", registrant.getStep3_motherName());
                params.put("fatherHashFingerprint", registrant.getStep4_fatherFinger());
                params.put("fatherName", registrant.getStep4_fatherName());
                params.put("doctorName", registrant.getStep1_doctor());
                params.put("countryCode", registrant.getStep1_country());
                params.put("hospitalAddress", registrant.getStep1_hospital());
                params.put("birthDay", registrant.getStep1_date());
                params.put("genero", registrant.getStep2_gender().toString());
                params.put("imgMotherFront", registrant.getStep3_motherIneFrontB64());
                params.put("imgFatherFront", registrant.getStep4_fatherIneFrontB64());
                params.put("imgMotherBack", registrant.getStep3_motherIneBackB64());
                params.put("imgFatherBack", registrant.getStep4_fatherIneBackB64());

                return params;
            }
        };

        // add it to the RequestQueue
        requestQueue.add(postRequest);
    }
}
