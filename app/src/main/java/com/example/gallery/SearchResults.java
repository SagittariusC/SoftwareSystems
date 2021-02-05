package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;

public class SearchResults extends AppCompatActivity implements OnMapReadyCallback {
    private static int img_counter = 0;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LatLng ImageLocation = new LatLng(0, 0);
    private LatLngBounds mMapBoundary;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    ImageView selectedImage;
    Button camera;
    ImageButton left, right;
    String currentPhotoPath;
    TextView date_time, caption;
    File files[] = null;
    boolean newImage = false;
    File newImageFile = null;
    int[] searchList = null;

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

        mMapView = findViewById(R.id.idLocationMap);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync((OnMapReadyCallback) this);

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 0);

        if (files.length > 0) {
            img_counter = files.length - 1;
            updateCaption(files[img_counter]);
        }

        String[] searchParams = new String[7];
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                searchParams = null;
            } else {
                searchParams[0] = extras.getString("STARTTIMESTAMP");
                searchParams[1] = extras.getString("ENDTIMESTAMP");
                searchParams[2] = extras.getString("CAPTION");
                searchParams[3] = extras.getString("TOPLEFTLAT");
                searchParams[4] = extras.getString("TOPLEFTLONG");
                searchParams[5] = extras.getString("BOTTOMRIGHTLAT");
                searchParams[6] = extras.getString("BOTTOMRIGHTLONG");

                searchUpdate(files, searchParams);
            }
        } else {
            searchParams[0] = (String) savedInstanceState.getSerializable("CAPTION");
        }

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
                double bottomBoundary = ImageLocation.latitude - .01;
                double leftBoundary = ImageLocation.longitude - .01;
                double topBoundary = ImageLocation.latitude + .01;
                double rightBoundary = ImageLocation.longitude + .01;

                mMapBoundary = new LatLngBounds(
                        new LatLng(bottomBoundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );
                mGoogleMap.clear();
                if(files.length > 0){
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(ImageLocation)
                            .title(files[img_counter].getName()));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
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
            updateCaption(files[img_counter]);

        } else if (img_counter == 0) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        newImage = false;
        if (files.length > 1 && img_counter < files.length - 1) {
            img_counter++;
            updateCaption(files[img_counter]);
        } else if (img_counter == files.length - 1) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    public void searchUpdate (File[] files, String[] param_list){

        int index = 0;
        int foundIndex = -1;
        for (File f : files){
            String path = f.getPath();
            String[] attr = path.split("_");
            if((param_list[0].length() == 0) && (param_list[1].length() == 0)) {
                if (attr[3].equals(param_list[2])) {
                    foundIndex = index;
                    break;
                }
            }else if ((Integer.parseInt(attr[1]) >= Integer.parseInt(param_list[0]) && Integer.parseInt(attr[1]) <= Integer.parseInt(param_list[1]))
                    && (param_list[2].equals("") || attr[3].equals(param_list[2]))) {
                foundIndex = index;
                break;
            }
            index++;
        }

        if(foundIndex == -1){
            Toast.makeText(this, "No pictures found", Toast.LENGTH_SHORT).show();
        }else {
            updateCaption(files[foundIndex]);
        }

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
        mMapView.getMapAsync((OnMapReadyCallback) this);

        if (path_str == null || path_str =="") {
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
                String date_format = date.substring(0,4) + "/" + date.substring(4,6) + "/" + date.substring(6,8);
                date_time.setText(date_format);
            } else {
                date_time.setText(date);
            }
        }
    }
}