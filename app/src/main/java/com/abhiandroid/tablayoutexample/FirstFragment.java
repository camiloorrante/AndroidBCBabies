package com.abhiandroid.tablayoutexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.content.Context;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class FirstFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    public interface OnPauseListener{
        void onFragmentOnePause(String hospitalName, String doctorName, String date, String Time,
                                String country, String address);
    }

    OnPauseListener pauseListener;
    EditText etxHospitalName;
    EditText etxDoctorName;
    EditText etxDate;
    EditText etxTime;
    Spinner sprCountry;
    EditText etxAddress;
    Registrant registrant;

    Button button;
    Button button2;

    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";
    private static final String BARRA = "/";

    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    //Hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);

    //Widgets
    ImageButton ibObtenerFecha, ibObtenerHora;


    public FirstFragment() {
        // Required empty public constructor
    }

    public void setRegistrant(Registrant registrant) {
        this.registrant = registrant;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //etFecha = (EditText) getView().findViewById(R.id.et_mostrar_fecha_picker);
        /*
        etHora = (EditText) getView().findViewById(R.id.et_mostrar_hora_picker);

        ibObtenerFecha = (ImageButton) getView().findViewById(R.id.ib_obtener_fecha);
        ibObtenerHora = (ImageButton) getView().findViewById(R.id.ib_obtener_hora);

        ibObtenerFecha.setOnClickListener(this);
        ibObtenerHora.setOnClickListener(this);
        */

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //return inflater.inflate(R.layout.fragment_second, container, false);
        View RootView = inflater.inflate(R.layout.fragment_first, container, false);

        etxHospitalName = (EditText) RootView.findViewById(R.id.hospital);
        etxDoctorName = (EditText) RootView.findViewById(R.id.doctor);
        etxDate = (EditText) RootView.findViewById(R.id.date);
        etxTime = (EditText) RootView.findViewById(R.id.time);
        sprCountry= (Spinner) RootView.findViewById(R.id.sprCountry);
        etxAddress  = (EditText) RootView.findViewById(R.id.eaddress);

        ibObtenerFecha = (ImageButton) RootView.findViewById(R.id.ib_obtener_fecha);
        ibObtenerHora = (ImageButton) RootView.findViewById(R.id.ib_obtener_hora);

        ibObtenerFecha.setOnClickListener(this);
        ibObtenerHora.setOnClickListener(this);

        // Spinner element
        Spinner spinner = (Spinner) RootView.findViewById(R.id.sprCountry);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select a Country");
        categories.add("Italia");
        categories.add("Argentina");
        categories.add("México");
        categories.add("Uruguay");
        categories.add("Colombia");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        return RootView;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_obtener_fecha:
                obtenerFecha();
                break;
            case R.id.ib_obtener_hora:
                obtenerHora();
                break;
        }
    }

    private void obtenerFecha(){
        DatePickerDialog recogerFecha = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                final int mesActual = month + 1;

                String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);

                etxDate.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);

        recogerFecha.show();

    }

    private void obtenerHora(){
        TimePickerDialog recogerHora = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                String horaFormateada =  (hourOfDay < 9)? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                String minutoFormateado = (minute < 9)? String.valueOf(CERO + minute):String.valueOf(minute);

                String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                 etxTime.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, false);

        recogerHora.show();
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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