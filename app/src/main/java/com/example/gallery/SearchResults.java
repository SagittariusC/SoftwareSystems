package com.example.gallery;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchResults extends AppCompatActivity implements OnMapReadyCallback {

    private static int img_counter = 0;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LatLng ImageLocation = new LatLng(0, 0);
    private LatLngBounds mMapBoundary;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ImageView selectedImage;
    private ImageButton left, right;
    private TextView date_time, caption;
    private File[] files = null;
    private boolean newImage = false;
    private final List<Integer> ResultList = new ArrayList<>();

    private float maxLat = 0, minLat = 0, maxLong = 0, minLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        files = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        selectedImage = findViewById(R.id.searchImageView);
        left = findViewById(R.id.button_left);
        right = findViewById(R.id.button_right);
        caption = findViewById(R.id.view_caption);
        date_time = findViewById(R.id.timestamp2);

        mMapView = findViewById(R.id.idSearchLocationMap);
        mMapView.onCreate(savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null);
        mMapView.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 0);

        searchUpdate(files);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLeft();
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveRight();
            }
        });
        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SearchResults.this, MainActivity.class);
                i.putExtra("SELECTEDSEARCHIMAGE", ResultList.get(img_counter));
                startActivity(i);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                // Set a boundary to start
                Bundle extras = getIntent().getExtras();

                double bottomBoundary = minLat - (maxLat - minLat) * 0.3;
                double leftBoundary = minLong - (maxLong - minLong) * 0.3;
                double topBoundary = maxLat + (maxLat - minLat) * 0.3;
                double rightBoundary = maxLong + (maxLong - minLong) * 0.3;

                mMapBoundary = new LatLngBounds(
                        new LatLng(bottomBoundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );
                mGoogleMap.clear();
                float[] latLong = new float[2];
                ExifInterface Exif = null;

                if (files.length > 0) {
                    for (int i = 0; i < ResultList.size(); i++) {
                        try {
                            Exif = new ExifInterface(files[ResultList.get(i)].getPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Exif.getLatLong(latLong);
                        ImageLocation = new LatLng(latLong[0], latLong[1]);
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(ImageLocation)
                                .title(files[ResultList.get(i)].getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    }
                    try {
                        Exif = new ExifInterface(files[ResultList.get(img_counter)].getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Exif.getLatLong(latLong);
                    ImageLocation = new LatLng(latLong[0], latLong[1]);
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(ImageLocation)
                            .title(files[ResultList.get(img_counter)].getName()));

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
                }


                mMapView.onResume();
            }
        });

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private void moveRight() {
        newImage = false;
        if (files.length > 1 && img_counter > 0) {
            img_counter--;
            if (img_counter < 0) {
                img_counter = 0;
            }
            updateCaption(files[ResultList.get(img_counter)]);

        } else if (img_counter == 0) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        newImage = false;
        if (files.length > 1 && img_counter < ResultList.size() - 1) {
            img_counter++;
            if (img_counter >= ResultList.size()) {
                img_counter = img_counter - 1;
            }
            updateCaption(files[ResultList.get(img_counter)]);
        } else if (img_counter == ResultList.size() - 1) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void searchUpdate(File[] files) {

        Debug.startMethodTracing("search.trace");

        float[] latLong = new float[2];
        ExifInterface Exif = null;

        int index = 0;
        for (File f : files) {
            String path = f.getPath();
            String[] attr = path.split("_");
            try {
                Exif = new ExifInterface(f.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Exif.getLatLong(latLong);

            Bundle extras = getIntent().getExtras();

            if (attr[3].contains(extras.getString("CAPTION")) || extras.getString("CAPTION").length() == 0) {
                if ((Integer.parseInt(attr[1]) >= Integer.parseInt(extras.getString("STARTTIMESTAMP"))) && Integer.parseInt(attr[1]) <= Integer.parseInt(extras.getString("ENDTIMESTAMP"))) {
                    if (latLong[0] < extras.getFloat("TOPLEFTLAT") && latLong[0] > extras.getFloat("BOTTOMRIGHTLAT")) {
                        if (latLong[1] > extras.getFloat("TOPLEFTLONG") && latLong[1] < extras.getFloat("BOTTOMRIGHTLONG")) {
                            ResultList.add(index);
                            if (ResultList.size() == 1) {
                                maxLat = latLong[0];
                                minLat = latLong[0];
                                maxLong = latLong[1];
                                minLong = latLong[1];
                            } else {
                                if (latLong[0] > maxLat) {
                                    maxLat = latLong[0];
                                } else {
                                    minLat = latLong[0];
                                }
                                if (latLong[1] > maxLong) {
                                    maxLong = latLong[1];
                                } else {
                                    minLong = latLong[1];
                                }
                            }
                        }
                    }
                }
            }
            index++;
        }

        if (ResultList.size() == 0) {
            Toast.makeText(this, "0 pictures found", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(SearchResults.this, MainActivity.class);
            startActivity(i);
        } else if (ResultList.size() == 1) {
            Toast.makeText(this, "1 picture found", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(SearchResults.this, MainActivity.class);
            i.putExtra("SELECTEDSEARCHIMAGE", ResultList.get(0));
            startActivity(i);
        } else {
            Toast.makeText(this, ResultList.size() + " pictures found", Toast.LENGTH_SHORT).show();
            updateCaption(files[ResultList.get(0)]);
        }

        Debug.stopMethodTracing();
    }

    public void updateCaption(File f) {
        String path_str = f.getPath();
        String date;

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(f.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[] latLong = new float[2];
        ei.getLatLong(latLong);

        ImageLocation = new LatLng(latLong[0], latLong[1]);
        mMapView.getMapAsync(this);

        if (path_str == null || path_str == "") {
            selectedImage.setImageResource(R.mipmap.ic_launcher);
            date_time.setText("");
            caption.setText("");
        } else {
            String[] attr = path_str.split("_");
            selectedImage.setImageBitmap(BitmapFactory.decodeFile(path_str));
            if (attr.length > 4) {
                caption.setText(attr[3]);
                date = attr[1];
            } else {
                caption.setText("");
                date = attr[1];
            }
            if (date.length() == 8) {
                String date_format = date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6, 8);
                date_time.setText(date_format);
            } else {
                date_time.setText(date);
            }
        }
    }
}