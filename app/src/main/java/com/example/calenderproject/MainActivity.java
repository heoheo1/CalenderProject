package com.example.calenderproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    String pfnumber,pfname;
    String number;
    String name;
    CalendarView calendar_View;
    String date;
    Button btn_phone,btn_Ok;
    TextView txt_date;
    EditText edt_time,edt_memo;
    Cursor cursor;
    SharedPreferences sharedPreferences ;
    Boolean cad_click = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_date =findViewById(R.id.txt_date);
        edt_time=findViewById(R.id.edt_time);
        edt_memo=findViewById(R.id.edt_memo);
        btn_Ok=findViewById(R.id.btn_Ok);
        btn_phone=findViewById(R.id.btn_phone);
        calendar_View=findViewById(R.id.calendar_View);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS,Manifest.permission.SEND_SMS
            },0);
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS,Manifest.permission.SEND_SMS
            },0);
        }

        calendar_View.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int monthOfYear, int dayOfMonth) {
                date = Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth);
                txt_date.setText("날짜 : " + date);
                cad_click=true;

            }
        });
        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent,0);

                }
        });
        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cad_click==true) {
                    sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
                    pfnumber = sharedPreferences.getString("number", "");
                    pfname = sharedPreferences.getString("name", "");
                    String memo = edt_memo.getText().toString();
                    String time = edt_time.getText().toString();
                    Log.d("ddd", pfnumber);
                    String num = pfnumber.replaceAll("-", "");
                    Log.d("ddd", num);

                    String message = "우리 이날 약속 해요 ! " +
                            "\n날짜 : " + date + "\n시간 : " + time + "\n내용 : " + memo + " 를 보냈습니다.";

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(pfnumber, null, message, null, null);
                    Toast.makeText(getApplicationContext(), date+"날짜로 "+ pfname + "님 께 메세지를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                cursor = null;

                if (data != null) {
                    cursor = getContentResolver().query(data.getData(),
                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        name = cursor.getString(0);
                        number = cursor.getString(1);
                        btn_phone.setText(number);
                        sharedPreferences =getSharedPreferences("pref",MODE_PRIVATE);
                        SharedPreferences.Editor editor= sharedPreferences.edit();
                        editor.putString("number",number);
                        editor.putString("name",name);
                        editor.commit();
                        cursor.close();
                    }
                }
            }
        }
    }

}


