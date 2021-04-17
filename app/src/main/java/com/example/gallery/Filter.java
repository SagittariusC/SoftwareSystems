package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    Button searchButton, cancelButton;
    EditText startDateText, endDateText, cityName;
    Boolean startDateEdit = false, endDateEdit = false;
    Float latitudeFromCity;
    Float longitudeFromCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        searchButton = findViewById(R.id.searchButton);
        cancelButton = findViewById(R.id.cancelButton);

        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        cityName = findViewById(R.id.cityText);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel(v);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if(!cityName.getText().toString().isEmpty()) {
                    String address = cityName.getText().toString();
                    GeoLocation geoLocation = new GeoLocation();
                    geoLocation.getAddress(address, getApplicationContext(), new GeoHandler());
                }

               Handler handler = new Handler();
               handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    go(v);
                    }
                }, 500);
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
                if (startDateEdit) {
                    startDateEdit = false;
                    updateLabel(startDateText);
                }
                if (endDateEdit) {
                    endDateEdit = false;
                    updateLabel(endDateText);
                }
            }
        };

        startDateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startDateEdit = true;
                // TODO Auto-generated method stub
                new DatePickerDialog(Filter.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endDateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                endDateEdit = true;
                // TODO Auto-generated method stub
                new DatePickerDialog(Filter.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel(EditText edittext) {
        String myFormat = "YYYYMMdd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edittext.setText(sdf.format(myCalendar.getTime()));
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
        EditText radiusFromCity = (EditText) findViewById(R.id.radiusNumber);
        if(!cityName.getText().toString().isEmpty()) {
            float radiusInDeg = Float.parseFloat(radiusFromCity.getText().toString()) / 111;
            TopLeftLat.setText(String.valueOf(latitudeFromCity + radiusInDeg));
            BottomRightLat.setText(String.valueOf(latitudeFromCity - radiusInDeg));
            TopLeftLong.setText(String.valueOf(longitudeFromCity - radiusInDeg));
            BottomRightLong.setText(String.valueOf(longitudeFromCity + radiusInDeg));
        }
        if( fromDate.length() == 0 && toDate.length() == 0 && caption.length() == 0 && TopLeftLat.length() == 0 && TopLeftLong.length() == 0 && BottomRightLat.length() == 0 && BottomRightLong.length() == 0 ){
            Toast.makeText(this, "No filter criteria entered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            //if >1 field is full, get inputs or default
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
    private class GeoHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            String address;

            switch (msg.what) {
                case 1:
                    Bundle bundle = msg.getData();
                    address = bundle.getString("address");
                    break;
                default:
                    address = null;
            }
            Pattern pattern = Pattern.compile("\n *");
            Matcher matcher = pattern.matcher(address);
            if (matcher.find()) {
                latitudeFromCity = Float.parseFloat(address.substring(0, matcher.start()));
                longitudeFromCity = Float.parseFloat(address.substring(matcher.end()));
            }
        }
    }
}


