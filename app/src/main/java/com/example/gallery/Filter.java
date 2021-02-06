package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Filter extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    Button searchButton, cancelButton;
    EditText startDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        searchButton = findViewById(R.id.searchButton);
        cancelButton = findViewById(R.id.cancelButton);
        startDateText= (EditText) findViewById(R.id.startDateText);

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

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        startDateText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(Filter.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        }

    private void updateLabel() {
        String myFormat = "YYYYMMDD"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        startDateText.setText(sdf.format(myCalendar.getTime()));
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

        if( fromDate.length() == 0 && toDate.length() == 0 && caption.length() == 0 && TopLeftLat.length() == 0 && TopLeftLong.length() == 0 && BottomRightLat.length() == 0 && BottomRightLong.length() == 0 ){
            Toast.makeText(this, "No filter criteria entered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            i.putExtra("STARTTIMESTAMP", fromDate.length() != 0 ? fromDate.getText().toString() : "0");
            i.putExtra("ENDTIMESTAMP", toDate.length() != 0 ? toDate.getText().toString() : "30000000");
            i.putExtra("CAPTION", caption.length() != 0 ? caption.getText().toString() : "");
            i.putExtra("TOPLEFTLAT", TopLeftLat.length() != 0 ? Float.parseFloat(TopLeftLat.getText().toString()) : 1000);
            i.putExtra("TOPLEFTLONG", TopLeftLong.length() != 0 ? Float.parseFloat(TopLeftLong.getText().toString()) : -1000);
            i.putExtra("BOTTOMRIGHTLAT", BottomRightLat.length() != 0 ? Float.parseFloat(BottomRightLat.getText().toString()) : -1000);
            i.putExtra("BOTTOMRIGHTLONG", BottomRightLong.length() != 0 ? Float.parseFloat(BottomRightLong.getText().toString()) : 1000);

            startActivity(i);
        }
    }
}

