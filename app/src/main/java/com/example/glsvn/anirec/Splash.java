package com.example.glsvn.anirec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
  Created by utkuglsvn
  utku.glsvn@gmail.com 
*/
public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Splash.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
