package com.test.h5forcamerademo.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.h5forcamerademo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("xxx","onCreate");
        initView();
        initData();

    }
    private void initData() {
        mBtn.setOnClickListener(this);
    }

    private void initView() {
        mBtn = (Button) findViewById(R.id.btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn:
                startActivity(new Intent(this, UploadImgForH5Activity.class));
                break;
        }
    }
}
