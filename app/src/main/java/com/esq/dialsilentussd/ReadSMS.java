package com.esq.dialsilentussd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ReadSMS extends AppCompatActivity {
    static TextView smsMessage;
    public static final String RECEIVED_SMS_MESSAGE = "receivedSmsMessage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_sms);
        smsMessage = findViewById(R.id.smsMessage);
        Intent intent = getIntent();
        String string = intent.getStringExtra(RECEIVED_SMS_MESSAGE);
        smsMessage.setText(string);
    }
}
