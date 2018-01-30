package com.hrdym.exchangerates;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CurrentRatesActivity extends AppCompatActivity {

    //Data strings
    ArrayList<String> rawData;
    ArrayList<String> ratesList;
    ArrayList<String> currencyList;
    private final static String url = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    //Views and layouts
    private LinearLayout content;
    Spinner mFromCurrency;
    Spinner mToCurrency;
    Button mOKButton;
    SharedPreferences filterpreferences;
    SharedPreferences.Editor filtereditor;
    SharedPreferences currencypreferences;
    SharedPreferences.Editor currencyeditor;

    public CurrentRatesActivity() throws MalformedURLException {
    }

    public void saveFilters() {

        filtereditor.clear();

        for(int i=0; i<content.getChildCount(); i++) {

            View child = content.getChildAt(i);
            TextView from = child.findViewById(R.id.itemTextFrom);
            TextView to = child.findViewById(R.id.itemTextTo);

            filtereditor.putString(Integer.toString(i), from.getText()+" "+to.getText());
        }
        filtereditor.commit();
    }

    public void saveCurrency(){

        currencyeditor.clear();

        for(int i=0; i<currencyList.size(); i++) {
            currencyeditor.putString(currencyList.get(i),ratesList.get(i));
        }
        currencyeditor.commit();
    }


    @Override
    protected void onPause() {
        super.onPause();

        saveFilters();
        saveCurrency();
    }

    public void addSavedFilters(){

        Map<String, ?> filters = filterpreferences.getAll();
        int nFilters = filters.size();

        for(int i=0; i<nFilters; i++) {

            String currencies = (String) filters.get(Integer.toString(i));

            String[] splitted = currencies.split("\\s+");

            AddFilter(splitted[0],splitted[1]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rates);
        content = (LinearLayout) findViewById(R.id.currentContent);

        //loading of preferences
        filterpreferences = getSharedPreferences("filters", Context.MODE_PRIVATE);
        filtereditor = filterpreferences.edit();
        currencypreferences = getSharedPreferences("currency", Context.MODE_PRIVATE);
        currencyeditor = currencypreferences.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.buttonAddFilter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CurrentRatesActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_filter, null);

                //Getting reference
                mFromCurrency = (Spinner) mView.findViewById(R.id.spinnerFrom);
                mToCurrency = (Spinner) mView.findViewById(R.id.spinnerTo);
                mOKButton = (Button) mView.findViewById(R.id.buttonOk);

                //Setting spinner values
                ArrayAdapter<String> adapterCurrency = new ArrayAdapter<String>(mView.getContext(), R.layout.spinner_layout, currencyList);
                adapterCurrency.setDropDownViewResource(R.layout.spinner_layout);
                mFromCurrency.setAdapter(adapterCurrency);
                mToCurrency.setAdapter(adapterCurrency);

                //Adding filter by button
                mOKButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View rowView = getLayoutInflater().inflate(R.layout.converse_item, null);

                        TextView itemFrom = (TextView) mFromCurrency.getSelectedView();
                        TextView itemTo = (TextView) mToCurrency.getSelectedView();

                        TextView currencyFrom = rowView.findViewById(R.id.itemTextFrom);
                        TextView currencyTo = rowView.findViewById(R.id.itemTextTo);
                        TextView currencyVal = rowView.findViewById(R.id.unitValue);

                        float currency_value = getCurrencyValue(mFromCurrency.getSelectedItemPosition(),mToCurrency.getSelectedItemPosition());

                        currencyFrom.setText(itemFrom.getText());
                        currencyTo.setText(itemTo.getText());
                        currencyVal.setText(String.format("%.3f", currency_value));

                        content.addView(rowView, 0);

                        Toast.makeText(CurrentRatesActivity.this, "Filter Added", Toast.LENGTH_SHORT).show();
                    }
                });

                mBuilder.setView(mView);
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        new GetRatesDataTask(this, url).execute();
    }

    public void AddFilter(String from, String to)
    {
        View rowView = getLayoutInflater().inflate(R.layout.converse_item, null);

        TextView currencyFrom = rowView.findViewById(R.id.itemTextFrom);
        TextView currencyTo = rowView.findViewById(R.id.itemTextTo);
        TextView currencyVal = rowView.findViewById(R.id.unitValue);

        int index_from = currencyList.indexOf(from);
        int index_to = currencyList.indexOf(to);

        float currency_value = getCurrencyValue(index_from,index_to);

        currencyFrom.setText(from);
        currencyTo.setText(to);
        currencyVal.setText(String.format("%.3f", currency_value));

        content.addView(rowView, 0);
    }

    public void onDeleteFilter(View v) {
        content.removeView((View) v.getParent());
    }

    public void callBackData(String[] result) {
        if (result == null){
            Map<String, ?> currencymap = currencypreferences.getAll();

            currencyList = new ArrayList<String>(currencymap.keySet());
            ratesList = new ArrayList<String>((Collection<String>)currencymap.values());
        }
        else {
            rawData = new ArrayList<String>(Arrays.asList(result));
            rawData.add(0, "EUR");
            rawData.add((rawData.size() / 2) + 1, "1.0");

            currencyList = new ArrayList<String>(rawData.subList(0, (rawData.size() / 2)));
            ratesList = new ArrayList<String>(rawData.subList(rawData.size() / 2, rawData.size()));
        }
    }

    public float getCurrencyValue(int indexFrom, int indexTo) {
        return Float.parseFloat(ratesList.get(indexTo))/Float.parseFloat(ratesList.get(indexFrom));
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
            if(!isOnline()) return null;
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onPostExecute(String[] result) {
        //call back data to main thread
        activity.callBackData(result);
        activity.addSavedFilters();
        pDialog.dismiss();
    }
}