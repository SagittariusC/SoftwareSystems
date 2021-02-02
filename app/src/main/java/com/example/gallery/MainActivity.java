package com.example.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
            updatePhoto(files[img_counter]);
        }

        camera.setOnClickListener (new View.OnClickListener() {
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

        caption.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String cap = caption.getText().toString();
                updateFile(files[img_counter].getPath(), cap);
            }
        });
    }

    private void moveRight() {
        File files[] = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        if (files.length > 1 && img_counter > 0) {
            img_counter--;
            updatePhoto(files[img_counter]);

        } else if (img_counter == 0) {
            Toast.makeText(this, "No more pictures!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        File files[] = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        if (files.length > 1 && img_counter < files.length - 1) {
            img_counter++;
            updatePhoto(files[img_counter]);

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
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                updatePhoto(f);
           }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

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

    public void updateFile(String path, String caption) {
        String[] attr = path.split("_");
        if (attr.length >= 3) {
            File to = new File(attr[0] + "_" + caption + "_" + attr[1] + "_" + attr[2] + "_" + attr[3]);
            File from = new File(path);
            from.renameTo(to);
        }
    }

    private void updatePhoto(File path) {
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
                caption.setText(attr[1]);
                date = attr[2];
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