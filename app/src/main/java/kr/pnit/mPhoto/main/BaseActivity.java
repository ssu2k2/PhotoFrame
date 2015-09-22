package kr.pnit.mPhoto.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import kr.pnit.mPhoto.Network.HttpManager;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;


import java.util.ArrayList;

/**
 * Created by macmini on 15. 2. 4..
 */
public class BaseActivity extends Activity {
    private final String TAG = "BaseActivity";
    // ------------------------ Preference ---------------------------------------//
    private final String Pref = "Pnit";

    public void saveStringPreference(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(Pref, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringPreference(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Pref, MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
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
            //Log.d(TAG, "prepareNetworking :" + URL);
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
