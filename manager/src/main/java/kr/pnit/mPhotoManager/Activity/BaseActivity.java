package kr.pnit.mPhotoManager.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import kr.pnit.mPhotoManager.Define.Define;
import kr.pnit.mPhotoManager.Network.HttpManager;
import kr.pnit.mPhotoManager.Network.ParamVO;
import kr.pnit.mPhotoManager.R;

/**
 * Created by macmini on 15. 2. 4..
 */
public class BaseActivity extends Activity {
    private final String TAG = "BaseActivity";
    // ------------------------ Preference ---------------------------------------//

    public void saveStringPreference(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public void saveBooleanPreference(String key, Boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public String getStringPreference(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
    public Boolean getBooleanPreference(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, true);
    }
    // ------------------------ Network ------------------------------------------//
    public ArrayList<ParamVO> alParam = new ArrayList<ParamVO>();
    ProgressDialog progressDialog;
    public boolean prepareNetworking(String URL, String method) {
        if(URL.length() > 5) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.net_dialog_title));
            progressDialog.setMessage(getResources().getString(R.string.net_dialog_context));
            progressDialog.show();
            makeParams();
            NetTask loginTask = new NetTask();
            Log.d(TAG, "prepareNetworking :" + URL);
            loginTask.execute(URL, method);
            return true;
        }
        return false;
    }

    public void makeParams() {

    }

    public void parseResult(String result) {

    }
    private class NetTask extends AsyncTask<String , Integer , String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpManager httpManager = new HttpManager();
            try {
                result = httpManager.executeHttp(params[0], params[1], alParam);
            } catch (Exception e) {
                e.printStackTrace();
                result = "ERROR:";
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            //try{
                //parseResult(new String(s.getBytes("iso-8859-1"), "euc-kr"));
                parseResult(s);
//            } catch(UnsupportedEncodingException e){
                //parseResult(s);
            //}

        }
    }

}
