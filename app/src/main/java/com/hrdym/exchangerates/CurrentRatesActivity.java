package com.hrdym.exchangerates;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrentRatesActivity extends AppCompatActivity {

    //String urlBaseString = new String("https://api.fixer.io/latest?base=");
    ArrayList<String> currencyList;
    private final static String url = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    public CurrentRatesActivity() throws MalformedURLException {
    }
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rates);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.buttonAddFilter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add currency filter here", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        new GetRatesDataTask(this, url).execute();
    }

    public void callBackData(String[] result) {

        String[] test = result;

        /*
        Spinner spinner = (Spinner) findViewById(R.id.spinnerFrom);
        currencies.add("jedna");
        currencies.add("dve");
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
         */
    }
}

class GetRatesDataTask extends AsyncTask<String[], Void, String[]> {

    private CurrentRatesActivity activity;
    private String url;
    private XmlPullParserFactory xmlFactoryObject;
    private ProgressDialog pDialog;

    public GetRatesDataTask(CurrentRatesActivity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(activity);
        pDialog.setTitle("Getting Information from WEB");
        pDialog.setMessage("Loading...");
        pDialog.show();
    }

    @Override
    protected String[] doInBackground(String[]... params) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            InputStream stream = connection.getInputStream();

            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myParser.setInput(stream, null);
            String[] result = parseXML(myParser);
            stream.close();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] parseXML(XmlPullParser myParser) {

        int event;
        int count=0;
        String text = null;
        ArrayList<String> currencyList = new ArrayList<String>();
        ArrayList<String> rateList = new ArrayList<String>();

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        //text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("Cube")) {
                            if (myParser.getAttributeCount() == -1) break;
                            currencyList.add(myParser.getAttributeValue(0));
                            rateList.add(myParser.getAttributeValue(1));
                        }
                        break;
                }
                event = myParser.next();
            }
            //join and return string array
            currencyList.addAll(rateList);
            String[] result = new String[currencyList.size()];
            result = currencyList.toArray(result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        //call back data to main thread
        pDialog.dismiss();
        activity.callBackData(result);
    }
}