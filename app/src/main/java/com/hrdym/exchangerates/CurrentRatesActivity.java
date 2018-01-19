package com.hrdym.exchangerates;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CurrentRatesActivity extends AppCompatActivity {

    //
    String urlBaseString = new String("https://api.fixer.io/latest?base=");
    ArrayList<String> currencies;

    //

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rates);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.buttonAddFilter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add currency filter here", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


/*
        // parse url to get currencies  for spinner
        try {
            URL url = new URL("https://api.fixer.io/latest");

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(getInputStream(url), "UTF_8");

            boolean insideRates = false;

            // Returns the type of current event
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (xpp.getName().equalsIgnoreCase("base")) currencies.add(xpp.getText());

                //if (xpp.getName().equalsIgnoreCase("rates")) currencies.add(xpp.);

                eventType = xpp.next(); //move to next element
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
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
