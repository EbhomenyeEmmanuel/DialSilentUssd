package com.esq.dialsilentussd;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText editText;
    Button createTask;
    TextView sim1TextView;
    TextView sim2TextView;
    RadioButton radioButton1;
    RadioButton radioButton2;
    private static final String READ_SMS =  Manifest.permission.READ_SMS;
    private static final String RECEIVE_SMS =  Manifest.permission.RECEIVE_SMS;
    private static final String READ_PHONE_STATE =  Manifest.permission.READ_PHONE_STATE;
    private static String colName;
    boolean permissionGranted;
    private static final String TAG = "MainActivity";
    TelephonyManager telephonyManager;
    List<String> carrierNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Using the telephony manager
        String srvcName = Context.TELEPHONY_SERVICE;
        telephonyManager = (TelephonyManager) getSystemService(srvcName);
        editText = findViewById(R.id.editTextCodeInput);
        sim1TextView = findViewById(R.id.Sim1TextView);
        sim2TextView = findViewById(R.id.Sim2TextView);
        radioButton1 = findViewById(R.id.radioBtnSim1);
        radioButton1.setOnClickListener(this);
        radioButton2 = findViewById(R.id.radioBtnSim2);
        radioButton2.setOnClickListener(this);
        readMobileNetwork();
        createTask = findViewById(R.id.btnCreate);
        createTask.setOnClickListener(this);
        permissions();

    }

    @Override
    public void onClick(View v) {
        if (v == createTask){
            refreshSmsInbox();
            if(TextUtils.isEmpty(editText.getText())){
                editText.setError("Please enter a text");
                editText.requestFocus();
                return;
            }else if(!colName.startsWith(editText.getText().toString())){
                    editText.setError(editText.getText() + " not found");
                    //Toast.makeText(this, colName, Toast.LENGTH_SHORT).show();
                    editText.requestFocus();
                    return;
            }else if (colName.startsWith(editText.getText().toString()) && permissionGranted){//EditText is not empty and permission has been granted
              showSms();
            }
        }else if (v == radioButton1){
            radioButton2.setChecked(false);
            radioButton1.setChecked(true);

        }else if (v == radioButton2){
            radioButton1.setChecked(false);
            radioButton2.setChecked(true);
        }
    }

    public void readMobileNetwork(){
        List<String> e = new ArrayList<>();
        e.addAll(getNetworkOperator(this));
        if (e.size() > 1){
            //Set the value of the network if the phone is a dual Sim for diferent networks
            sim1TextView.setText(e.get(0));
            sim2TextView.setText(e.get(1));
        }else{
            //Set the value of the network if the phone is not a dual Sim and remove the second radio button
            sim1TextView.setText(e.get(0));
           radioButton2.setVisibility(View.INVISIBLE);
        }
    }

    //Method for reading Mobile NetworkOperator
    private List <String> getNetworkOperator(final Context context){
        //Get System TELEPHONY service reference
        List<String> carrierNames = new ArrayList<>();
        try {
            final String permission = Manifest.permission.READ_PHONE_STATE;
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ) && (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)){
                final List<SubscriptionInfo> subscriptionInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
                for (int i= 0; i< subscriptionInfos.size(); i++){
                    carrierNames.add(subscriptionInfos.get(i).getCarrierName().toString());
                }
            }else {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                //Get Carrier name (NetWork Operator Name)
                carrierNames.add(telephonyManager.getNetworkOperatorName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return carrierNames;
    }

    //Add SMS Read Permission At Runtime
    public void permissions (){
        String [] permissions ={READ_SMS, RECEIVE_SMS, READ_PHONE_STATE};
        final int REQUEST_CODE_ASK_PERMISSIONS = 123;
        //If permission is Not GRANTED
        if(ContextCompat.checkSelfPermission(getBaseContext(), READ_SMS) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getBaseContext(), RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(getBaseContext(), READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                    //If Permission Granted Then Show SMS
                    permissionGranted = true;
                }else {
                    //Then set Permission
                    Log.e(TAG,"permissions: Permissions has not been granted");
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
                }
            }
        }else {
            Log.e(TAG,"permissions: Permissions has not been granted");
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public void refreshSmsInbox(){
        ArrayList<String> sms = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor smsInboxCursor = contentResolver.query(uri, new String[]{"_id", "address", "date", "body"},null, null,null);
      //  int senderIndex = smsInboxCursor.getColumnIndex("address");
        int messageIndex = smsInboxCursor.getColumnIndex("body");
        int dateIndex = smsInboxCursor.getColumnIndex("date");

        //Move cursor to first column
        if(smsInboxCursor.moveToFirst()){
            do{
                // colName = smsInboxCursor.getString(messageIndex);
                // colName += " Date is " + dateIndex;
                for (int i =0; i <smsInboxCursor.getColumnCount(); i++){
                    sms.add(smsInboxCursor.getString(messageIndex));
                    colName = sms.get(0);
                    Log.i(TAG, "refreshSmsInbox: colName is " + colName);
                }
            }while (smsInboxCursor.moveToNext());
        }else {
            Toast.makeText(this, "No sms in inbox", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "refreshSmsInbox: No sms in inbox ");
        }

    }
    //If Sms has been read succesfully, start next activity
    public void showSms(){
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "showSms: Sms has been read succesfully");
        Intent intent = new Intent(this, ReadSMS.class);
        intent.putExtra(ReadSMS.RECEIVED_SMS_MESSAGE, colName);
        startActivity(intent);
    }

}
;