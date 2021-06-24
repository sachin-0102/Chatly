package com.sachin0102.projects.chatly.Activties;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sachin0102.projects.chatly.R;

public class PhoneNumberActivity extends AppCompatActivity {

    private Button continueButton;
    private EditText phoneNumber;

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        auth= FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent= new Intent(PhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        continueButton = (Button) findViewById(R.id.continue_button);
        phoneNumber = (EditText) findViewById(R.id.phone_number_text_view);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                if(TextUtils.isEmpty(phone)||phone.length()<10)
                {
                    Toast.makeText(PhoneNumberActivity.this, "Enter a valid Phone Number", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                    intent.putExtra("phoneNumber", phone);
                    startActivity(intent);
                }
            }
        });
    }
}