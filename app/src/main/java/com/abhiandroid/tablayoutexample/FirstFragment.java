package com.abhiandroid.tablayoutexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class FirstFragment extends Fragment {
    OnPauseListener pauseListener;
    EditText etxtMotherName;
    EditText etxFatherName;
    EditText etxHospitalName;
    EditText etxDoctorName;
    Registrant registrant;

    public interface OnPauseListener{
        void onFragmentOnePause(String motherName, String fatherName,
                                String hospitalName, String doctorName);
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

        etxtMotherName =  (EditText) RootView.findViewById(R.id.edtxtMotherName);
        etxFatherName = (EditText) RootView.findViewById(R.id.edtxtFatherName);
        etxHospitalName = (EditText) RootView.findViewById(R.id.edtxthospitalName);
        etxDoctorName = (EditText) RootView.findViewById(R.id.edtxtDoctorName);

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

        pauseListener.onFragmentOnePause(etxtMotherName.getText().toString(),etxFatherName.getText().toString()
                , etxHospitalName.getText().toString(), etxDoctorName.getText().toString());
    }
}