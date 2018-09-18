package com.abhiandroid.tablayoutexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class FirstFragment extends Fragment {
    OnPauseListener pauseListener;
    EditText etxHospitalName;
    EditText etxDoctorName;
    EditText etxDate;
    EditText etxTime;
    Spinner sprCountry;
    EditText etxAddress;
    Registrant registrant;

    public interface OnPauseListener{
        void onFragmentOnePause(String hospitalName, String doctorName, String date, String Time,
                                String country, String address);
    }

    public FirstFragment() {
        // Required empty public constructor
    }

    public void setRegistrant(Registrant registrant) {
        this.registrant = registrant;
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View RootView = inflater.inflate(R.layout.fragment_first, container, false);
        etxHospitalName = (EditText) RootView.findViewById(R.id.hospital);
        etxDoctorName = (EditText) RootView.findViewById(R.id.doctor);
        etxDate = (EditText) RootView.findViewById(R.id.date);
        etxTime = (EditText) RootView.findViewById(R.id.time);
        sprCountry= (Spinner) RootView.findViewById(R.id.sprCountry);
        etxAddress  = (EditText) RootView.findViewById(R.id.eaddress);

        return RootView;

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity)
            activity = (Activity) context;
        else
            activity = null;

        try{
            pauseListener = (OnPauseListener)activity;
        }
        catch (ClassCastException ex){
            throw new ClassCastException(activity.toString() + "must implement OnPauseListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        pauseListener.onFragmentOnePause(etxHospitalName.getText().toString(), etxDoctorName.getText().toString(),
                                        etxDate.getText().toString(), etxTime.getText().toString(),
                                        /*sprCountry.getSelectedItem().toString()*/"", etxAddress.getText().toString());
    }
}