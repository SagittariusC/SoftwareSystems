package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class Filter extends AppCompatActivity {

    Button searchButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        searchButton = findViewById(R.id.searchButton);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                cancel(v);
            }
        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                go(v);
            }
        });

        }


    public void cancel(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void go(final View v) {



        Intent i = new Intent(this, SearchResults.class);
        EditText fromDate = (EditText) findViewById(R.id.startDateText);
        EditText toDate = (EditText) findViewById(R.id.endDateText);
        EditText caption = (EditText) findViewById(R.id.captionText);
        EditText TopLeftLat = (EditText) findViewById(R.id.TopLeftLat);
        EditText TopLeftLong = (EditText) findViewById(R.id.TopLeftLong);
        EditText BottomRightLat = (EditText) findViewById(R.id.BottomRightLat);
        EditText BottomRightLong = (EditText) findViewById(R.id.BottomRightLong);

        i.putExtra("STARTTIMESTAMP", fromDate.length() != 0 ? fromDate.getText().toString() : "0");
        i.putExtra("ENDTIMESTAMP", toDate.length() != 0 ? toDate.getText().toString() : "30000000");
        i.putExtra("CAPTION", caption.length() != 0 ? caption.getText().toString() : "");
        i.putExtra("TOPLEFTLAT", TopLeftLat.length() != 0 ? Float.parseFloat(TopLeftLat.getText().toString()) : "1000");
        i.putExtra("TOPLEFTLONG", TopLeftLong.length() != 0 ? Float.parseFloat(TopLeftLong.getText().toString()) : "-1000");
        i.putExtra("BOTTOMRIGHTLAT", BottomRightLat.length() != 0 ? Float.parseFloat(BottomRightLat.getText().toString()) : "-1000");
        i.putExtra("BOTTOMRIGHTLONG", BottomRightLong.length() != 0 ? Float.parseFloat(BottomRightLong.getText().toString()) : "1000");

        startActivity(i);

    }
}

