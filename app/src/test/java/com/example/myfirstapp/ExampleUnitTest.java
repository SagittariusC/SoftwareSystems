package com.example.myfirstapp;

import android.media.ExifInterface;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        File testfile = new File( getClass().getResource("/JPEG_20210206_185806_test1_1726492936613719614.jpg").getPath());

    }

}