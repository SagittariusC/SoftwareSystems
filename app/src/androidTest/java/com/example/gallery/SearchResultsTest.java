package com.example.gallery;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;

public class SearchResultsTest extends TestCase {

    File test = new File(getClass().getResource("/JPEG_20210206_155313_dog_4964385422027460385.jpg").getPath());
    String[] attr = test.getPath().split("_");

    @Test
    public void captiontest() {


    }

}