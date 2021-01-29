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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static int img_counter = 0;
    ImageView selectedImage;
    Button camera;
    ImageButton left, right;
    String currentPhotoPath;
    TextView date_time;
    EditText caption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedImage = findViewById(R.id.displayImageView);
        camera = findViewById(R.id.snap);
        left = findViewById(R.id.left_button);
        right = findViewById(R.id.right_button);
        caption = findViewById(R.id.edit_caption);
        date_time = findViewById(R.id.timestamp);
        File files[] = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();



        if (files.length > 0) {
            img_counter = files.length - 1;
            updateCaption(files[img_counter]);
        }

        String[] searchResults = new String[3];
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                searchResults = null;
            } else {
                searchResults[0] = extras.getString("STARTTIMESTAMP");
                searchResults[1] = extras.getString("ENDTIMESTAMP");
                searchResults[2] = extras.getString("CAPTION");
                searchUpdate(files, searchResults);
            }
        } else {
            searchResults[0] = (String) savedInstanceState.getSerializable("CAPTION");
        }

        camera.setOnClickListener (new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick (View v) {
                askCameraPermissions();
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                moveLeft();
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                moveRight();
            }
        });

        caption.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE) {
                    img_counter = 0;
                    String cap = caption.getText().toString();
                    File files[] = (getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles());
                    updatePhoto(files[img_counter].getPath(), cap);
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(caption.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void moveRight() {
        File files[] = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
        if (files.length > 1 && img_counter > 0) {
            img_counter--;
            updateCaption(files[img_counter]);

        } else if (img_counter == 0) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        File files[] = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this,"Camera Permission is Required to use the Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File rotateImage(File oldImage, int degree) throws IOException {
        String imageFileName = oldImage.getName();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap loadedImg = BitmapFactory.decodeFile(oldImage.getPath());
        Bitmap rotatedImg = Bitmap.createBitmap(loadedImg, 0, 0, loadedImg.getWidth(), loadedImg.getHeight(), matrix, true);
        loadedImg.recycle();
        oldImage.delete();

        File newImage = new File(storageDir, imageFileName);
        if (!newImage.exists()) {
            Log.d("path", newImage.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(newImage);
                rotatedImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        return newImage;

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {}

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(currentPhotoPath);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                File f = new File(currentPhotoPath);

                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    try {
                        f = rotateImage(f, 90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    try {
                        f = rotateImage(f, 180);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    try {
                        f = rotateImage(f, 270);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                updateCaption(f);
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

    }

    public void updateCaption(File path) {
        String path_str = path.getPath();
        String date;

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