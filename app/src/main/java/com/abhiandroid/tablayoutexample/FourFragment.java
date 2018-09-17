package com.abhiandroid.tablayoutexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.System.in;

public class FourFragment extends Fragment{
    public FourFragment() {
        // Required empty public constructor
    }

    Button botonSave;
    public void onCreate(Bundle savedInstanceState,LayoutInflater inflater, ViewGroup container) {
        super.onCreate(savedInstanceState);
        View rootView =  inflater.inflate(R.layout.fragment_four, container, false);

        botonSave = (Button)rootView.findViewById(R.id.save);
        botonSave.setOnClickListener(new View.OnClickListener() {

               public void onClick(View view) {
                  HttpURLConnection urlConnection = null;
                   try {
                       URL url = new URL("http://10.15.29.121:3000/API/babies/sample/2&test");
                       urlConnection = (HttpURLConnection) url.openConnection();
                       InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                   }  catch (IOException e) {
                       e.printStackTrace();
                   } finally {
                           urlConnection.disconnect();
                   }
                   BufferedReader r = new BufferedReader(new InputStreamReader(in));
                   StringBuilder total = new StringBuilder();
                   String line;
                   try{
                       while ((line = r.readLine()) != null) {
                           total.append(line).append('\n');

                       }
                       Log.d("Hola", String.valueOf(total));
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_four, container, false);
    }

}
