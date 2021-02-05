package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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


        i.putExtra("STARTTIMESTAMP", fromDate.getText() != null ? fromDate.getText().toString() : "");
        i.putExtra("ENDTIMESTAMP", toDate.getText() != null ? toDate.getText().toString() : "");
        i.putExtra("CAPTION", caption.getText() != null ? caption.getText().toString() : "");
        i.putExtra("TOPLEFTLAT", TopLeftLat.getText() != null ? TopLeftLat.getText().toString() : "");
        i.putExtra("TOPLEFTLONG", TopLeftLong.getText() != null ? TopLeftLong.getText().toString() : "");
        i.putExtra("BOTTOMRIGHTLAT", BottomRightLat.getText() != null ? BottomRightLat.getText().toString() : "");
        i.putExtra("BOTTOMRIGHTLONG", BottomRightLong.getText() != null ? BottomRightLong.getText().toString() : "");

        startActivity(i);

    }
}

