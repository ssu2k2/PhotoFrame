package kr.pnit.mPhotoManager.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.pnit.mPhotoManager.Define.Define;
import kr.pnit.mPhotoManager.Network.ParamVO;
import kr.pnit.mPhotoManager.R;

/**
 * Created by macmini on 15. 7. 30..
 */
public class Loading extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Loading";

    LinearLayout llLoading;
    LinearLayout llLogin;

    EditText etId;
    EditText etPasswd;
    CheckBox chkSave;
    Button btnOk;
    RadioGroup rgGubun;

    String id;
    String passwd;
    String gubun;
    boolean onoff;

    private final String PREF_ID = "pref";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "1015190224749";

    private GoogleCloudMessaging _gcm;
    private String _regId;


    private final static int MSG_SHOW_LOGIN =  0x100;

    int mode = MODE_REQ_LOGIN;
    private final static int MODE_REQ_LOGIN = 0x100;
    private final static int MODE_REG_PUSH = 0x101;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_SHOW_LOGIN:
                    llLoading.setVisibility(View.GONE);
                    llLogin.setVisibility(View.VISIBLE);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        initLayout();

        // google play service가 사용가능한가
        if (checkPlayServices())
        {
            _gcm = GoogleCloudMessaging.getInstance(this);
            _regId = getRegistrationId();
            if (TextUtils.isEmpty(_regId))
                registerInBackground();
            else {

            }
        }
        else
        {
            //
        }

    }

    void initLayout(){
        llLoading = (LinearLayout)findViewById(R.id.llLoading);
        llLogin = (LinearLayout)findViewById(R.id.llLogin);

        etId = (EditText)findViewById(R.id.etId);
        etPasswd = (EditText)findViewById(R.id.etPasswd);

        rgGubun = (RadioGroup)findViewById(R.id.rgGubun);
        chkSave = (CheckBox)findViewById(R.id.chkSave);

        btnOk = (Button)findViewById(R.id.btOk);

        btnOk.setOnClickListener(this);

        llLoading.setVisibility(View.VISIBLE);
        llLogin.setVisibility(View.GONE);


        id = getStringPreference(Define.PARAM_ID);
        if(id.trim().length() > 0) {
            etId.setText(id);
        }

        gubun = getStringPreference(Define.PARAM_USER_GUBUN);
        if(gubun.trim().length() > 0){
            if(gubun.toLowerCase().equals("agent")){
                rgGubun.check(R.id.rbAgent);
            } else {
                rgGubun.check(R.id.rbPrs);
            }
        }

        if(getBooleanPreference(Define.PARAM_SAVE_ID)){
            chkSave.setChecked(true);
            passwd = getStringPreference(Define.PARAM_PASSWD);
            if(passwd.trim().length() > 0) {
                etPasswd.setText(passwd);
            }
        } else {
            chkSave.setChecked(false);
        }

        onoff = getBooleanPreference(Define.PARAM_PUSH_ONOFF);

        mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOGIN, 2000);
    }
    void gotoMain(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btOk:
                id = etId.getText().toString().trim();
                passwd = etPasswd.getText().toString().trim();

                if(rgGubun.getCheckedRadioButtonId() == R.id.rbAgent) {
                    gubun = "agent";
                } else {
                    gubun = "prs";
                }

                if((id.length() > 0) & (passwd.length() > 0))
                    prepareNetworking(Define.HTTP_LOGIN, "GET");
                else {
                    Toast.makeText(this, "아이디와 암호를 넣어주세요.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // registration  id를 가져온다.
    private String getRegistrationId()
    {
        String registrationId = getSavedRegID();
        if (TextUtils.isEmpty(registrationId))
        {
            return "";
        }
        int registeredVersion = getSavedAppVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion)
        {
            return "";
        }
        return registrationId;
    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (_gcm == null)
                    {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + _regId;
                    Log.d(TAG, "Reg :" + msg);
                    storeRegistrationId(_regId);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {

            }
        }.execute(null, null, null);
    }

    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    // registraion id를 preference에 저장한다.
    private void storeRegistrationId(String regId)
    {
        int appVersion = getAppVersion();

        setSaveGCMInfo(regId, appVersion);
        setSavePushOnOff(onoff);

    }
    private void setSaveGCMInfo(String id, int AppVersion) {
        SharedPreferences shared = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(Define.PARAM_PUSH_ID, id);
        editor.putInt(Define.PARAM_APP_VERSION, AppVersion);
        editor.commit();
    }
    private void setSavePushOnOff(boolean status) {
        SharedPreferences shared = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(Define.PARAM_PUSH_ONOFF, status);
        editor.commit();
    }
    private int getSavedAppVersion() {
        SharedPreferences shared = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        return shared.getInt(Define.PARAM_APP_VERSION, 1);
    }
    private String getSavedRegID() {
        SharedPreferences shared = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        return shared.getString(Define.PARAM_PUSH_ID, "");
    }

    // google play service가 사용가능한가
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                finish();
            }
            return false;
        }
        return true;
    }


    private boolean parseJson(String json) {
        try{
            JSONObject j = new JSONObject(json);
            if(mode == MODE_REQ_LOGIN){
                if(j.getString("RESULT").equals("SUCCESS")) {

                    saveStringPreference(Define.PARAM_ID,       j.getString(Define.PARAM_ID));
                    saveStringPreference(Define.PARAM_NAME, j.getString(Define.PARAM_NAME));
                    saveStringPreference(Define.PARAM_OW_NAME, j.getString(Define.PARAM_OW_NAME));
                    saveStringPreference(Define.PARAM_USER_GUBUN, j.getString(Define.PARAM_USER_GUBUN));

                    if(chkSave.isChecked()) {
                        saveStringPreference(Define.PARAM_PASSWD, etPasswd.getText().toString());
                    }else {
                        saveStringPreference(Define.PARAM_PASSWD, "");
                    }
                    saveBooleanPreference(Define.PARAM_SAVE_ID, chkSave.isChecked());

                    mode = MODE_REG_PUSH;
                    prepareNetworking(Define.HTTP_SET_PUSH, "GET");
                    return true;
                } else {
                    return false;
                }
            } else if(mode == MODE_REG_PUSH) {
                if(j.getString("RESULT").equals("SUCCESS")) {
                    gotoMain();
                    return true;
                }
            }

        }catch(JSONException j) {
            Log.d(TAG, "JSON Exception " + j.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public void makeParams() {
        alParam.clear();
        if(mode == MODE_REQ_LOGIN){
            alParam.add(new ParamVO("UserGubun", gubun));
            alParam.add(new ParamVO("UserID",id));
            alParam.add(new ParamVO("UserPSWD",passwd));
        } else if(mode == MODE_REG_PUSH) {
            alParam.add(new ParamVO("UserGubun", gubun));
            alParam.add(new ParamVO("UserID",id));
            alParam.add(new ParamVO("RegId",getSavedRegID()));
            alParam.add(new ParamVO("Push_OnOff",(onoff?"On":"Off")));
        }
    }

    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(mode == MODE_REG_PUSH){
                gotoMain();
            }else {
                if(result.indexOf("{") > 0) {
                    String r = result.substring(result.indexOf("{"));
                    if( r.length() > 0) {
                        //Log.d(TAG, "Result(JSON):" + r);
                        if(parseJson(r)){
                            return;
                        }
                    }
                }
            }
        }

    }
}
