package com.example.calenderproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    String phoneNumber;
    List<String> list;
    ArrayAdapter<String> adapter;
    ListView listView;
    ScrollView sv;




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
        sv=findViewById(R.id.sv);

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

        listView =findViewById(R.id.list_view);
        list =new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,list);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(getApplicationContext(),list.get(i).toString() + " 삭제되었습니다.",Toast.LENGTH_SHORT).show();

                list.remove(i);
                adapter.notifyDataSetChanged();
                return true;
            }
        });



        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                    sv.requestDisallowInterceptTouchEvent(true);

            return  false;
            }
        });

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
                    String memo = edt_memo.getText().toString();
                    String time = edt_time.getText().toString();

                    int phoneSize=adapter.getCount();

                    for (int i=0; i<phoneSize;i++){
                        String num= list.get(i);

                        String message = "우리 이날 약속 해요 ! " +
                                "\n날짜 : " + date + "\n시간 : " + time + "\n내용 : " + memo + " 를 보냈습니다.";
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(num, null, message, null, null);
                        Toast.makeText(getApplicationContext(), date+" 날짜로 "+num+" 님 께 메세지를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                    }
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
                        phoneNumber = number.replace("+82", "0");
                    }

                    list.add(name + ":" + phoneNumber);
                    listView.setAdapter(adapter);
                    cursor.close();
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}


