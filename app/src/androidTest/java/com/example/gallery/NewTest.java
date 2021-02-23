package com.example.gallery;

import android.media.ExifInterface;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class NewTest {
    @Test
    public void Test_Exif() throws IOException {

        File testfile = new File(getClass().getResource("/JPEG_20210206_185806_test1_1726492936613719614.jpg").getPath());

        ExifInterface Exif = null;
        float[] latLong = new float[2];

        Exif = new ExifInterface(testfile.getPath());
        Exif.getLatLong(latLong);

    }
}
