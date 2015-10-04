package kr.pnit.mPhoto.loading;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;
import kr.pnit.mPhoto.main.Main;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by macmini on 14. 11. 28..
 */
public class Join extends BaseActivity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    private final int MODE_GET_MSG  = 0x100;
    private final int MODE_LOG_IN = 0x101;

    private final int REQ_ID  = 0x102;
    private final int REQ_CUP = 0x103;
    Button btnOk;
    Button btnCupon;
    Button btnSendSMS;

    EditText etShopCode;
    EditText etCupon;

    String id;
    String cupon;
    String SMS_Msg = "";
    String appVersion = "";

    int req = REQ_ID;
    int mode = MODE_GET_MSG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        getActionBar().hide();
        init();
    }

    private void init() {
        btnOk = (Button)findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        btnSendSMS = (Button)findViewById(R.id.btnSendSMS);
        btnSendSMS.setOnClickListener(this);


        etShopCode = (EditText)findViewById(R.id.etShop);
        etCupon = (EditText)findViewById(R.id.etCupon);

        btnCupon = (Button)findViewById(R.id.btnCuponInput);
        btnCupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etCupon.getVisibility() == View.VISIBLE){
                    etCupon.setVisibility(View.INVISIBLE);
                } else {
                    etCupon.setVisibility(View.VISIBLE);
                }
            }
        });

        saveStringPreference(Define.KEY_CUPON,   "");       // Clear Cupon Number
        saveStringPreference(Define.KEY_CUPON_PRODUCT,   "");   // Clear Cupon Number

        id = getStringPreference(Define.KEY_PRE_ID);
        if(id.trim().length() > 0) {
            etShopCode.setText(id);
            etShopCode.setVisibility(View.GONE);
            etCupon.setVisibility(View.INVISIBLE);
        } else {
            etShopCode.setVisibility(View.VISIBLE);
            etCupon.setVisibility(View.GONE);
        }

        String version = "";
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = i.versionName;
        } catch(PackageManager.NameNotFoundException e) {

        }
        Log.d(TAG, "App Version : " + version);
        prepareNetworking(Define.HTTP_GET_SMS, "GET");

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnOk:

                id = etShopCode.getText().toString().trim();
                cupon = etCupon.getText().toString().trim();

                if(id.length() == 0) {
                    id = "";
                    req = REQ_CUP;
                }
                if(cupon.length() == 0) {
                    cupon = "";
                    req = REQ_ID;
                }

                if((id.length() > 0)||(cupon.length() > 0)) {
                    prepareNetworking(Define.HTTP_SHOP_ID, "GET");
                }else {
                    Toast.makeText(Join.this, "실행코드나 쿠폰코드를 넣어주세요.", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.btnSendSMS:
                if(SMS_Msg.length() > 0) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    PackageManager pm = getPackageManager();
                    List<ResolveInfo> activityList = pm
                            .queryIntentActivities(sendIntent, 0);


                    for (ResolveInfo app : activityList) {
                        if ((app.activityInfo.name).contains("kakao.talk")) {
                            ActivityInfo activity = app.activityInfo;
                            ComponentName name = new ComponentName(
                                    activity.applicationInfo.packageName, activity.name);
                            sendIntent.setComponent(name);
                            sendIntent.addCategory(Intent.CATEGORY_DEFAULT);

                            String text;
                            if(id.length() > 0) {
                                text =  "어플설치주소:" + SMS_Msg + "\n" + "어플실행코드:" + id;
                            } else {
                                text = "어플설치주소:" + SMS_Msg + "\n" + "어플실행코드:저장된실행코드없음";
                            }
                            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                        }
                    }
                }
                break;
        }
    }
    private boolean parseGetMsg(String json) {
        try {
            JSONObject j = new JSONObject(json);
            if (j.getString("RESULT").equals("SUCCESS")) {
                SMS_Msg = j.getString("SNS_url");
                appVersion = j.getString("Ver_Num");
            } else {
                return false;
            }
        }catch(JSONException j) {
            Log.d(TAG, "JSON Exception " + j.getMessage());
        }
        return true;
    }

    private boolean parseLogin(String json) {
        try{
            JSONObject j = new JSONObject(json);
            if(j.getString("RESULT").equals("SUCCESS")) {
                saveStringPreference(Define.KEY_PRE_ID,   j.getString(Define.KEY_PRE_ID));
                saveStringPreference(Define.KEY_A_CODE,   j.getString(Define.KEY_A_CODE));
                saveStringPreference(Define.KEY_PRE_NAME, j.getString(Define.KEY_PRE_NAME));
                saveStringPreference(Define.KEY_OW_TEL,   j.getString(Define.KEY_OW_TEL));
                saveStringPreference(Define.KEY_OW_ZIP,   j.getString(Define.KEY_OW_ZIP));
                saveStringPreference(Define.KEY_OW_ADDR,   j.getString(Define.KEY_OW_ADDR));

                if(j.getString(Define.KEY_CUPON) != null)
                    saveStringPreference(Define.KEY_CUPON,   j.getString(Define.KEY_CUPON));
                else
                    saveStringPreference(Define.KEY_CUPON,   "");

                if(j.getString(Define.KEY_CUPON_PRODUCT) != null)
                    saveStringPreference(Define.KEY_CUPON_PRODUCT,   j.getString(Define.KEY_CUPON_PRODUCT));
                else
                    saveStringPreference(Define.KEY_CUPON_PRODUCT,   "");
                return true;
            } else {
                return false;
            }
        }catch(JSONException j) {
            Log.d(TAG, "JSON Exception " + j.getMessage());
            return true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    private void goToMain() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void makeParams() {
        alParam.clear();
        if(mode == MODE_LOG_IN){
            alParam.add(new ParamVO("UserID",id));
            alParam.add(new ParamVO("CupunNo", cupon));
        }
    }

    @Override
    public void parseResult(String result) {
        //Log.d(TAG, "Result:" + result);
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    Log.d(TAG, "Result(JSON):" + r);
                    if(mode == MODE_GET_MSG){
                        parseGetMsg(r);
                        mode = MODE_LOG_IN;
                    } else{
                        if(parseLogin(r)){
                            goToMain();
                            return;
                        } else {
                            if(req == REQ_ID){
                                Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            } else if(req == REQ_CUP) {
                                Toast.makeText(this, "이미 사용된 쿠폰이거나, 입력하신 쿠폰번호을 다시 확인해주세요.", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }
            }
        }


    }
}
