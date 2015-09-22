package kr.pnit.mPhoto.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.order.OrderList;
import kr.pnit.mPhoto.order.OrderMain;

import java.io.File;

/**
 * Created by macmini on 14. 11. 28..
 */
public class Main extends Activity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    Button btnList;
    Button btnOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().hide();
        init();
        getDataDir();
        deleteTempFile();
    }

    /**
     * 레이아웃 초기화
     */
    private void init() {
        btnList  = (Button)findViewById(R.id.btnOrdList);
        btnOrder = (Button)findViewById(R.id.btnOrder);

        btnList.setOnClickListener(this);
        btnOrder.setOnClickListener(this);
    }

    /**
     * 임시 파일 정리
     * @return
     */
    public boolean deleteTempFile() {
        File[] tempFiles = new File(Environment.getExternalStorageDirectory(), Define.FOLDER_TEMP).listFiles();
        for(int i = 0; i < tempFiles.length; i++) {
            boolean result = tempFiles[i].delete();
        }
        tempFiles = new File(Environment.getExternalStorageDirectory(), Define.FOLDER_ALBUM).listFiles();
        for(int i = 0; i < tempFiles.length; i++) {
            boolean result = tempFiles[i].delete();
        }
        tempFiles = new File(Environment.getExternalStorageDirectory(), Define.FOLDER_GRID).listFiles();
        for(int i = 0; i < tempFiles.length; i++) {
            boolean result = tempFiles[i].delete();
        }

        return true;
    }

    /**
     * 앱 폴더 생성
     */
    public boolean getDataDir() {
        File sdcard = Environment.getExternalStorageDirectory();
        if( sdcard == null || !sdcard.isDirectory() ) {
            // TODO: warning popup
            Log.w(TAG, "Storage card not found " + sdcard);
            return false;
        }
        if(!confirmDir(new File(sdcard, Define.FOLDER)) ) {
            // TODO: warning popup
            Log.e(TAG, "Unable to create " + Define.FOLDER);
            return false;
        }
        if(!confirmDir(new File(sdcard, Define.FOLDER_ALBUM)) ) {
            // TODO: warning popup
            Log.e(TAG, "Unable to create " + Define.FOLDER_ALBUM);
            return false;
        }
        if(!confirmDir(new File(sdcard, Define.FOLDER_GRID)) ) {
            // TODO: warning popup
            Log.e(TAG, "Unable to create " + Define.FOLDER_GRID);
            return false;
        }
        if(!confirmDir(new File(sdcard, Define.FOLDER_TEMP)) ) {
            // TODO: warning popup
            Log.e(TAG, "Unable to create " + Define.FOLDER_TEMP);
            return false;
        }
        return true;
    }

    private boolean confirmDir(File dir) {
        if (dir.isDirectory()) return true;  // already exists
        if (dir.exists()) return false;      // already exists, but is not a directory
        return dir.mkdirs();                 // create it
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnOrdList:
                goToOrderList();
                break;
            case R.id.btnOrder:
                goToOrderMain();
                break;
        }
    }

    /**
     * 주문 리스트로 이동
     */
    private void goToOrderList() {
        Intent intent = new Intent(this, OrderList.class);
        startActivity(intent);
    }

    /**
     * 앨범 주문으로 이동
     */
    private void goToOrderMain() {
        Intent intent = new Intent(this, OrderMain.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Main.this);
        alert_confirm.setMessage("프로그램을 종료 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'YES'
                        finish();
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'No'
                        return;
                    }
                });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }
}
