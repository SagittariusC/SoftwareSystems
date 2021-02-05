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

        Intent i = new Intent(this, MainActivity.class);
        EditText from = (EditText) findViewById(R.id.startDateText);
        EditText to = (EditText) findViewById(R.id.endDateText);
        EditText caption = (EditText) findViewById(R.id.captionText);
        i.putExtra("STARTTIMESTAMP", from.getText() != null ? from.getText().toString() : "");
        i.putExtra("ENDTIMESTAMP", to.getText() != null ? to.getText().toString() : "");
        i.putExtra("CAPTION", caption.getText() != null ? caption.getText().toString() : "");
        startActivity(i);

    }
}

