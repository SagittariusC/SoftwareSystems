package com.example.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallery.BuildConfig;
import com.example.gallery.Filter;
import com.example.gallery.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static int img_counter = 0;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LatLng ImageLocation = new LatLng(49, -122);
    private LatLngBounds mMapBoundary;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    ImageView selectedImage;
    Button camera;
    ImageButton left, right;
    String currentPhotoPath;
    TextView date_time;
    EditText caption;
    File files[] = null;
    boolean newImage = false;
    File newImageFile = null;
    int[] searchList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        files = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        selectedImage = findViewById(R.id.displayImageView);
        camera = findViewById(R.id.snap);
        left = findViewById(R.id.left_button);
        right = findViewById(R.id.right_button);
        caption = findViewById(R.id.edit_caption);
        date_time = findViewById(R.id.timestamp);

        mMapView = findViewById(R.id.idLocationMap);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 0);

        if (files.length > 0) {
            img_counter = files.length - 1;
            updateCaption(files[img_counter]);
        }

        String[] searchParams = new String[3];
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                searchParams = null;
            } else {
                searchParams[0] = extras.getString("STARTTIMESTAMP");
                searchParams[1] = extras.getString("ENDTIMESTAMP");
                searchParams[2] = extras.getString("CAPTION");
                searchUpdate(files, searchParams);
            }
        } else {
            searchParams[0] = (String) savedInstanceState.getSerializable("CAPTION");
        }

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });

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

        caption.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String cap = caption.getText().toString();
                    if(newImage){
                        updatePhoto(newImageFile.getPath(), cap);
                        newImage = false;
                    }else{
                        updatePhoto(files[img_counter].getPath(), cap);
                    }
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(caption.getWindowToken(), 0);
                }
                return false;
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
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(ImageLocation)
                        .title(files[img_counter].getName()));

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
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

    public void filter(View view) {
        Intent intent = new Intent(this, Filter.class);
        startActivity(intent);
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera Permission is Required to use the Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void geoTag(File imageFile){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = null;
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = (Location) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null) {
                return;
            }

            try {
                ExifInterface exif = new ExifInterface(imageFile.getPath());
                //String latitudeStr = "90/1,12/1,30/1";
                double lat = location.getLatitude();
                double alat = Math.abs(lat);
                String dms = Location.convert(alat, Location.FORMAT_SECONDS);
                String[] splits = dms.split(":");
                String[] secnds = (splits[2]).split("\\.");
                String seconds;
                if(secnds.length==0)
                {
                    seconds = splits[2];
                }
                else
                {
                    seconds = secnds[0];
                }

                String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat>0?"N":"S");

                double lon = location.getLongitude();
                double alon = Math.abs(lon);


                dms = Location.convert(alon, Location.FORMAT_SECONDS);
                splits = dms.split(":");
                secnds = (splits[2]).split("\\.");

                if(secnds.length==0)
                {
                    seconds = splits[2];
                }
                else
                {
                    seconds = secnds[0];
                }
                String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";


                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lon>0?"E":"W");

                exif.saveAttributes();
            } catch (IOException e) {
                Log.e("PictureActivity", e.getLocalizedMessage());
            }

        }
    }

    private File rotateImage(File oldImage, int degree) throws IOException {
        String imageFileName = oldImage.getName();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        ExifInterface Exif = new ExifInterface(oldImage.getPath());
        Bitmap loadedImg = BitmapFactory.decodeFile(oldImage.getPath());
        Bitmap rotatedImg = Bitmap.createBitmap(loadedImg, 0, 0, loadedImg.getWidth(), loadedImg.getHeight(), matrix, true);
        oldImage.delete();

        File newImage = new File(storageDir, imageFileName);
        if (!newImage.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(newImage);
                rotatedImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Exif.saveAttributes();
        loadedImg.recycle();
        rotatedImg.recycle();
        return newImage;

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                newImage = true;
                newImageFile = new File(currentPhotoPath);

                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(currentPhotoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);


                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    try {
                        newImageFile = rotateImage(newImageFile, 90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    try {
                        newImageFile = rotateImage(newImageFile, 180);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    try {
                        newImageFile = rotateImage(newImageFile, 270);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                files = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
                geoTag(newImageFile);
                updateCaption(newImageFile);
            }
        }
    }

    public void updatePhoto(String path, String caption) {
        String[] attr = path.split("_");
        if (attr.length > 4) {
            File to = new File(attr[0] + "_" + attr[1] + "_" + attr[2] + "_" + caption +  "_" + attr[4]);
            File from = new File(path);
            from.renameTo(to);
        }else{
            File to = new File(attr[0] + "_" + attr[1] + "_" + attr[2] + "_" + caption + "_" + attr[3]);
            File from = new File(path);
            from.renameTo(to);
        }

        files = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();


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



}