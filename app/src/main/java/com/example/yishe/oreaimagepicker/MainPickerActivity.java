package com.example.yishe.oreaimagepicker;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;



public class MainPickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_picker);
    }
}
