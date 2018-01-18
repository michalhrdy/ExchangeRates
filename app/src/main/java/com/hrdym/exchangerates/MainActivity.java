package com.hrdym.exchangerates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCurrentRatesButton();
        //setHistoricalRatesButton();
    }

    private void setCurrentRatesButton() {
        Button currentER = (Button) findViewById(R.id.buttonCurrent);
        currentER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CurrentRatesActivity.class));
            }
        });
    }
/*
    private void setHistoricalRatesButton() {
        Button currentER = (Button) findViewById(R.id.buttonHistoric);

    }
*/

}
