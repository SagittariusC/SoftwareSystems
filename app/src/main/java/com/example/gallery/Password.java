package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Password extends AppCompatActivity {

    File[] documents;
    String path;
    boolean pw = false;
    boolean settings = false;
    Button submit;
    EditText pass;
    TextView message;
    TextView up, low, number, char_len;
    FloatingActionButton change_pw;
    String password;
    int count = 5;
    boolean strength;
    static final String TAG = "SymmetricAlgorithmAES";
    String secr="k";
    String secr2="d";
    SecretKeySpec sks;
    byte[] encodedBytes;
    byte[] decodedBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        change_pw = findViewById(R.id.settings);
        submit =findViewById(R.id.checkpw);
        pass = findViewById(R.id.password_field);
        password = pass.getText().toString();
        message = findViewById(R.id.msg);
        up = findViewById(R.id.upper);
        low = findViewById(R.id.lower);
        number = findViewById(R.id.num);
        char_len = findViewById(R.id.num_char);
        documents = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).listFiles();
        path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/pw.txt";

        byte[] key = new byte[0];
        try {
            key = (secr+secr2).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit

        sks = new SecretKeySpec(key, "AES");

        if (new File(path).isFile()) pw = true;

        if (!pw) {
            low.setText("");
            up.setText("");
            number.setText("");
            char_len.setText("");
            message.setText("No password has been set for this gallery");
            change_pw.setAlpha(0.0f);

            pass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    password = pass.getText().toString();
                    char_len.setText("" + password.length());
                    low.setText("a-z");
                    up.setText("A-Z");
                    number.setText("0-9");
                    strength = validate(password);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        } else {
            low.setText("");
            up.setText("");
            number.setText("");
            char_len.setText("");
            message.setText("The gallery is locked");

            change_pw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        changePassword();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(pass.getWindowToken(), 0);
                }
                return false;
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!settings) {
                        Intent intent = new Intent(Password.this, MainActivity.class);
                        if (checkPassword()) {
                            startActivity(intent);
                        }
                    } else {
                        if (checkPassword() && pw) {
                            message.setText("Correct! Choose a new password");
                            pw = false;
                            File f = new File(path);
                            f.delete();
                            Intent intent = new Intent(Password.this, Password.class);
                            startActivity(intent);
                        } else if (checkPassword() && !pw) {
                            Intent intent = new Intent(Password.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changePassword() throws IOException {
        settings = true;
        message.setText("Enter the old password for this gallery");
        submit.setText("Change Password");
    }

    public boolean validate(String password) {
        Pattern uppercase = Pattern.compile("[A-Z]");
        Pattern lowercase = Pattern.compile("[a-z]");
        Pattern numeric = Pattern.compile("[0-9]");
        int count = 0;

        if (!lowercase.matcher(password).find()) {
            low.setTextColor(Color.GRAY);
        } else {
            low.setTextColor(Color.GREEN);
            count++;
        }

        if (!uppercase.matcher(password).find()) {
            up.setTextColor(Color.GRAY);
        } else {
            up.setTextColor(Color.GREEN);
            count++;
        }

        if (!numeric.matcher(password).find()) {
            number.setTextColor(Color.GRAY);
        } else {
            number.setTextColor(Color.GREEN);
            count++;
        }

        if (password.length() < 8) {
            char_len.setTextColor(Color.RED);
        } else if (password.length() > 7) {
            char_len.setTextColor(Color.GREEN);
            count++;
        }

        if (count == 4) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPassword() throws IOException {

        File stored_password = new File(path);
        password = pass.getText().toString();
        if (!pw) {
            try {
                if (strength) {
                    encodedBytes = null;

                    try {
                        Cipher c = Cipher.getInstance("AES");
                        c.init(Cipher.ENCRYPT_MODE, sks);
                        encodedBytes = c.doFinal(password.getBytes());

                    } catch (Exception e) {
                        Log.e(TAG, "AES encryption error");
                    }

                    FileOutputStream fOut = new FileOutputStream(stored_password);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(Base64.encodeToString(encodedBytes, Base64.DEFAULT));
                    myOutWriter.close();
                    fOut.close();
                    Toast.makeText(this, "Password set!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(this, MainActivity.class);
//                    startActivity(intent);
                    return true;

                } else {
                    Toast.makeText(this, "Password isn't strong enough!", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                BufferedReader read_pw = new BufferedReader(new FileReader(stored_password));
                String stored = read_pw.readLine();
                decodedBytes = null;
                encodedBytes = stored.getBytes();

                try {
                    Cipher c = Cipher.getInstance("AES");
                    c.init(Cipher.DECRYPT_MODE, sks);
                    decodedBytes = Base64.decode(stored.getBytes(), Base64.DEFAULT);
                    decodedBytes = c.doFinal(decodedBytes);

                } catch (Exception e) {
                    Log.e(TAG, "AES decryption error");
                }

                String decoded = new String(decodedBytes,"UTF-8");

                if (decoded.equals(password)) {
                    Toast.makeText(this, "Password correct!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(this, MainActivity.class);
//                    startActivity(intent);
                    return true;

                } else {
                    if (--count == 0) {
                        throw new RuntimeException("you idiot");
                    }

                    if (count == 1) {
                        Toast.makeText(this, "Wrong password! 1 try left...", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "Wrong password! " + count + " tries left...", Toast.LENGTH_SHORT).show();
                    }

                    pass.getText().clear();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}