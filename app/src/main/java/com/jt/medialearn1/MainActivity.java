package com.jt.medialearn1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jt.medialearn1.activity.primary.Lesson1Activity;
import com.jt.medialearn1.activity.primary.Lesson2And3Activity;
import com.jt.medialearn1.activity.primary.Lesson4Activity;
import com.jt.medialearn1.activity.primary.Lesson5Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.button1:
                startActivity(new Intent(this, Lesson1Activity.class));
                break;
            case R.id.button2:
                startActivity(new Intent(this, Lesson2And3Activity.class));
                break;
            case R.id.button4:
                startActivity(new Intent(this, Lesson4Activity.class));
                break;
            case R.id.button5:
                startActivity(new Intent(this, Lesson5Activity.class));
                break;
        }
    }
}