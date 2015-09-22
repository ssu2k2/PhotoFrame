package kr.pnit.mPhoto.Dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yongsucho on 15. 3. 25..
 */
public class AddressActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "AddressActivity";

    Button btnComplete;

    Button btnAddress01;
    Button btnAddress02;
    Button btnAddress03;
    Button btnAddress04;

    EditText edtAddressDetail;

    ArrayList<String> alAddressString01;
    ArrayList<String> alAddressString02;
    ArrayList<String> alAddressString03;
    ArrayList<String> alAddressString04;

    int stage = 0;

    boolean isZipComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        getActionBar().hide();
        initLayout();
    }

    private void initLayout() {

        alAddressString01 = new ArrayList<String>();
        alAddressString02 = new ArrayList<String>();
        alAddressString03 = new ArrayList<String>();
        alAddressString04 = new ArrayList<String>();

        btnAddress01 = (Button)findViewById(R.id.btnAddress01);
        btnAddress02 = (Button)findViewById(R.id.btnAddress02);
        btnAddress03 = (Button)findViewById(R.id.btnAddress03);
        btnAddress04 = (Button)findViewById(R.id.btnAddress04);

        btnAddress01.setText(getResources().getText(R.string.address01));
        btnAddress02.setText(getResources().getText(R.string.address02));
        btnAddress03.setText(getResources().getText(R.string.address03));
        btnAddress04.setText(getResources().getText(R.string.address04));

        btnAddress01.setOnClickListener(this);
        btnAddress02.setOnClickListener(this);
        btnAddress03.setOnClickListener(this);
        btnAddress04.setOnClickListener(this);

        btnComplete = (Button)findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isZipComplete &&
                    edtAddressDetail.getText().toString().trim().length() >  0) {

                    String Address = btnAddress01.getText().toString() + " "
                             + btnAddress02.getText().toString() + " "
                             + btnAddress03.getText().toString() + " "
                             + edtAddressDetail.getText().toString();
                    String zipCode = btnAddress04.getText().toString();
                    Log.d(TAG, "RETURN:" + Address + " " + zipCode);
                    Intent intent = getIntent();
                    intent.putExtra("ADDRESS", Address);
                    intent.putExtra("ZIP_CODE", zipCode);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    Toast.makeText(AddressActivity.this, "세부 주소를 적어주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        edtAddressDetail = (EditText)findViewById(R.id.etAddressDetail);

        stage = 1;
        prepareNetworking(Define.HTTP_ZIP_CODE_01, "GET");
    }
    private void CreateListDialog(final ArrayList<String> list) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("지역 선택").setAdapter(
                new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, list),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(stage) {
                    case 1:
                        btnAddress01.setText(list.get(i));
                        stage = 2;
                        prepareNetworking(Define.HTTP_ZIP_CODE_02, "GET");
                        break;
                    case 2:
                        btnAddress02.setText(list.get(i));
                        stage = 3;
                        prepareNetworking(Define.HTTP_ZIP_CODE_03, "GET");
                        break;
                    case 3:
                        btnAddress03.setText(list.get(i));
                        stage = 4;
                        prepareNetworking(Define.HTTP_ZIP_CODE_04, "GET");
                        break;
                    case 4:
                        btnAddress04.setText(list.get(i));
                        isZipComplete = true;
                        break;
                }
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
    @Override
    public void onClick(View view) {
        isZipComplete = false;
        switch(view.getId()) {
            case R.id.btnAddress01:
                stage = 1;
                CreateListDialog(alAddressString01);
                break;
            case R.id.btnAddress02:
                stage = 2;
                prepareNetworking(Define.HTTP_ZIP_CODE_02, "GET");
                break;
            case R.id.btnAddress03:
                stage = 3;
                prepareNetworking(Define.HTTP_ZIP_CODE_03, "GET");
                break;
            case R.id.btnAddress04:
                stage = 4;
                prepareNetworking(Define.HTTP_ZIP_CODE_04, "GET");
                break;
        }
    }
    private void setAddressArray(JSONArray jArray, ArrayList<String> arrayList) throws JSONException{
        arrayList.clear();
        for(int i = 0; i < jArray.length(); i++){
            JSONObject object = jArray.getJSONObject(i);
            switch (stage) {
                case 1:
                    arrayList.add(object.getString("SIDO"));
                    break;
                case 2:
                    arrayList.add(object.getString("GUGUN"));
                    break;
                case 3:
                    String bunji = object.getString("BUNJI");
                    if(bunji.equals("null"))
                        arrayList.add(object.getString("DONG"));
                    else
                        arrayList.add(object.getString("DONG") + "," + bunji);
                    break;
                case 4:
                    arrayList.add(object.getString("ZIPCODE"));
                    break;
                default:
                    break;

            }
        }
    }
    private void parseAddress(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String result = jsonObject.getString("RESULT");
            if(result.equals("SUCCESS")){
                JSONArray jArray = jsonObject.getJSONArray("product");
                //Log.d(TAG, "Result Array : " + jArray.length());
                switch (stage) {
                    case 1:
                        setAddressArray(jArray, alAddressString01);
                        break;
                    case 2:
                        setAddressArray(jArray, alAddressString02);
                        CreateListDialog(alAddressString02);
                        break;
                    case 3:
                        setAddressArray(jArray, alAddressString03);
                        CreateListDialog(alAddressString03);
                        break;
                    case 4:
                        setAddressArray(jArray, alAddressString04);
                        CreateListDialog(alAddressString04);
                        break;
                    default:
                        break;
                }
            }
        }catch(JSONException je) {

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void makeParams() {
        alParam.clear();
        switch(stage) {
            case 1:
                break;
            case 2:
                alParam.add(new ParamVO("SIDO",btnAddress01.getText().toString()));
                break;
            case 3:
                alParam.add(new ParamVO("SIDO",btnAddress01.getText().toString()));
                alParam.add(new ParamVO("GUGUN",btnAddress02.getText().toString()));
                break;
            case 4:
                alParam.add(new ParamVO("SIDO" ,btnAddress01.getText().toString()));
                alParam.add(new ParamVO("GUGUN",btnAddress02.getText().toString()));
                String sDong[] = btnAddress03.getText().toString().split(",");
                if(sDong.length == 1){
                    alParam.add(new ParamVO("DONG" ,btnAddress03.getText().toString()));
                } else {
                    alParam.add(new ParamVO("DONG" ,sDong[0]));
                    alParam.add(new ParamVO("BUNJI" ,sDong[1]));
                }
                break;
            default:
                stage = 0;
                break;
        }
    }

    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    //Log.d(TAG, "Result(JSON):" + r);
                    parseAddress(r);
                    return;
                }
            }
        }
        Toast.makeText(this, "정보 요청에 실패하였습니다.", Toast.LENGTH_LONG).show();
    }
}
