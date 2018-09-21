package com.example.admin.nuschedule.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.admin.nuschedule.R;

public class FileUploadActivity extends AppCompatActivity {
    Button uploadFile, already;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);
        uploadFile = (Button) findViewById(R.id.file_upload_btn);
        already = (Button) findViewById(R.id.has_account_btn);
        already.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileUploadActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}