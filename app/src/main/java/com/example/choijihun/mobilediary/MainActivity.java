package com.example.choijihun.mobilediary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String FOLDER_NAME = "/mydiary";
    private static final int TEXT_SIZE_BIG = 50;
    private static final int TEXT_SIZE_MID = 30;
    private static final int TEXT_SIZE_SMALL = 20;
    private enum ReadCase {INIT_READ, NORMAL_READ, RE_READ};

    TextView dateView;
    Button btnSave;
    EditText edtDiary;
    DatePicker datePicker;

    // 현재 선택된 다이어리의 날짜정보
    int year, month, day;

    String strSDpath;
    File myDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datepick_dlg);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        setContentView(R.layout.activity_main);
        dateView = (TextView) findViewById(R.id.dateView);
        btnSave = (Button) findViewById(R.id.btnSave);
        edtDiary = (EditText) findViewById(R.id.edtDiary);

        // 다이어리 저장 파일 설정
        strSDpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        myDir = new File(strSDpath, FOLDER_NAME);

        // FOLDER_NAME 이 존재 하지 않은 경우에만 폴더 생성.
        if (!myDir.exists()) myDir.mkdirs();

        // 저장 버튼을 누르면 일기 저장.
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeDiary();
            }
        });

        // 현재 표시된 날짜를 누르면 다른 날짜로 변경, 날짜에 해당하는 일기가 있으면 불러옴.
        dateView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View dialogView = View.inflate(MainActivity.this, R.layout.datepick_dlg, null);
                datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("날짜 선택");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDate();
                        // 변경한 날짜의 다이어리를 불러오면서 메세지를 띄운다.
                        readMassage(ReadCase.NORMAL_READ, readDiary());
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setView(dialogView);
                dlg.show();
                return false;
            }
        });
        //  오늘 날짜로 초기화 (사용자가 datePicker 날짜를 선택하지 않은 상태에서 값을 가져오면 현재 날짜)
        getDate();
        // 오늘 날짜의 다이어리를 불러오면서 메세지를 띄운다.
        readMassage(ReadCase.INIT_READ, readDiary());
    }

    // 현재 선택된 날짜를 년_월_일.txt 형태로 반환
    private String getFileName() {
        return Integer.toString(year) + "_" + Integer.toString(month) + "_" + Integer.toString(day) + ".txt";
    }

    // 현재 선택된 날짜를 0000년 00월 00일 형태로 반환
    private String getDateStr() {
        return Integer.toString(year) + "년 " + Integer.toString(month) + "월 " + Integer.toString(day) + "일";
    }

    // 현재 선택된 날짜를 저장, dateView에 보여줌
    private void getDate() {
        year = datePicker.getYear();
        month = datePicker.getMonth() + 1;
        day = datePicker.getDayOfMonth();
        dateView.setText(getDateStr());
    }

    // 일기를 파일에서 읽어옴 ( 일기가 있으면 true, 없으면 false )
    private boolean readDiary() {
        File file = new File(myDir, getFileName());
        try {
            FileInputStream inFs = new FileInputStream(file);
            byte[] txt = new byte[inFs.available()];
            inFs.read(txt);
            edtDiary.setText(new String(txt));
            inFs.close();
            return true;
        } catch (IOException e) {
            edtDiary.setText(null);
            return false;
        }
    }

    // 일기를 읽어올 때 경우에 따라 Toast 메세지를 다르게 출력
    private void readMassage(ReadCase readCase, boolean fileExist) {
        switch(readCase) {
            case INIT_READ:
                if(fileExist) Toast.makeText(getApplicationContext(), "오늘의 일기를 불러옵니다.", Toast.LENGTH_SHORT).show();
                else Toast.makeText(getApplicationContext(), "새로운 일기를 작성하세요", Toast.LENGTH_SHORT).show();
                break;
            case NORMAL_READ:
                if(fileExist) Toast.makeText(getApplicationContext(), getFileName() + " 일기를 불러옵니다.", Toast.LENGTH_SHORT).show();
                else Toast.makeText(getApplicationContext(), "일기가 없습니다.", Toast.LENGTH_SHORT).show();
                break;
            case RE_READ:
                if(fileExist) Toast.makeText(getApplicationContext(), "일기를 다시 불러옵니다.", Toast.LENGTH_SHORT).show();
                else Toast.makeText(getApplicationContext(), "불러올 일기가 없습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    // 일기를 파일에 저장
    private void writeDiary() {
        File file = new File(myDir, getFileName());
        try {
            FileOutputStream outFs = new FileOutputStream(file);
            String str = edtDiary.getText().toString();
            outFs.write(str.getBytes());
            outFs.close();
            Toast.makeText(getApplicationContext(), getFileName() + "이 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getFileName() + " 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 일기 파일을 삭제
    private void deleteDiary() {
        File file = new File(myDir, getFileName());
        if(file.delete()) {
            edtDiary.setText(null);
            Toast.makeText(getApplicationContext(), getFileName() + "이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), getFileName() + "이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // 일기를 다시 읽어옴
            case R.id.menuReread:
                readMassage(ReadCase.RE_READ, readDiary());
                break;
            // 일기를 삭제
            case R.id.menuRemove:
                removeMenu();
                break;
            // 일기의 글씨 크기 변경
            case R.id.submenuTextSmall:
                edtDiary.setTextSize(TEXT_SIZE_SMALL);
                break;
            case R.id.submenuTextMid:
                edtDiary.setTextSize(TEXT_SIZE_MID);
                break;
            case R.id.submenuTextBig:
                edtDiary.setTextSize(TEXT_SIZE_BIG);
                break;
        }
        return true;
    }

    // 삭제 메뉴
    public void removeMenu() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("삭제");
        dlg.setMessage(getDateStr() + " 일기를 삭제하시겠습니까?");
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDiary();
            }
        });
        dlg.setNegativeButton("취소", null);
        dlg.show();
    }
}
