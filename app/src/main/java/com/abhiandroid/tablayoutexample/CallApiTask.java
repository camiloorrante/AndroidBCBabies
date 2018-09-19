package com.abhiandroid.tablayoutexample;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CallApiTask extends AsyncTask<Void, String, String>{
    private Exception ex;

    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder total = new StringBuilder();
        try {
            URL url = new URL("http://10.15.29.164:3000/API/babies/sample/2&test");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            String line;
            try{
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');

                }
                Log.d("Hola", String.valueOf(total));
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
        }  catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
