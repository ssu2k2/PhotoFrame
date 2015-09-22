package kr.pnit.mPhoto.loading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import kr.pnit.mPhoto.R;


/**
 * Created by macmini on 14. 11. 28..
 */
public class Loading extends Activity {
    private final String TAG = getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        getActionBar().hide();

    }
    private void init() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToJoin();
            }
        }, 2000);
    }
    private void goToJoin() {
        Intent intent = new Intent(this, Join.class);
        startActivity(intent);
        finish();
    }

}
