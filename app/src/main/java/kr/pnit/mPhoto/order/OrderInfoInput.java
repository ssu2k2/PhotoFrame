package kr.pnit.mPhoto.order;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.DTO.OrderInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Dialog.AddressActivity;
import kr.pnit.mPhoto.Dialog.SendImageActivity;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by macmini on 14. 11. 30..
 */
public class OrderInfoInput extends BaseActivity  implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();

    private final int GOTO_PAYPROCESS = 0x10;
    private final int GOTO_ZIPCODE = 0x11;
    private final int GOTO_SEND_IMAGE = 0x12;

    ImageButton iBtnPre;
    ImageButton iBtnPay;

    String Shop_Name;
    String Shop_Code;
    String Shop_Id;
    String Shop_Tel;

    String Shop_Zip;
    String Shop_Addr;

    String Album_Title;
    String Album_code;
    String Album_sub_code;
    int albumDeliverNum;

    int Album_type;
    int Album_num;
    int Cupon_num;
    String zip_code;
    String orderNumber;
    String inwha_yn;
    int Album_price;

    String user_name = "";
    String user_phone = "";
    String user_addr = "";


    Button btnOrderLow;

    EditText edtAgentName;
    EditText edtAgentPhone;
    EditText edtGoods;
    EditText edtGoodsNum;

    EditText edtCustomerName;
    EditText edtCustomerPhone;

    TextView tvCustomerAddress;
    TextView tvTotalPrice;

    RadioGroup radioGroup;

    boolean isAddressComplete = false;
    ArrayList<FrameInfo> alFrameList;

    OrderInfo mOrderInfo = null;
    String cupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_info_input);
        getActionBar().hide();


        Shop_Id =  getStringPreference(Define.KEY_PRE_ID);
        Shop_Name = getStringPreference(Define.KEY_PRE_NAME);
        Shop_Code = getStringPreference(Define.KEY_A_CODE);
        Shop_Tel = getStringPreference(Define.KEY_OW_TEL);

        Shop_Zip = getStringPreference(Define.KEY_OW_ZIP);
        Shop_Addr = getStringPreference(Define.KEY_OW_ADDR);



        inwha_yn = getIntent().getStringExtra("inwha_yn");
        Album_Title = getIntent().getStringExtra("AlbumTitle");
        Album_code = getIntent().getStringExtra("AlbumCode");
        Album_sub_code = getIntent().getStringExtra("AlbumSubCode");
        Album_price = getIntent().getIntExtra("AlbumPrice", 0);

        albumDeliverNum = Integer.parseInt(getIntent().getStringExtra("AlbumDeliverNum"));

        Album_type = getIntent().getIntExtra("AlbumType", Define.TYPE_NORMAL);


        Serializable intentListData = getIntent().getSerializableExtra("ALFrameInfo");
        alFrameList = (ArrayList<FrameInfo>) intentListData;

        cupon = getStringPreference(Define.KEY_CUPON_PRODUCT);


        mOrderInfo = new OrderInfo();
        mOrderInfo.good_price = Album_price;
        mOrderInfo.good_name = Album_Title;
        mOrderInfo.agent_name = Shop_Name;
        mOrderInfo.agent_phone = Shop_Tel;
        mOrderInfo.delevery_price = 2500;

//        Log.d(TAG, "Album Info : " + Album_Title + " Code:" + Album_code);
//        Log.d(TAG, "Album Info : " + mOrderInfo.good_name + " " + mOrderInfo.user_name);

        initLayout();
    }
    private void initLayout() {
        iBtnPre = (ImageButton)findViewById(R.id.btnPre);
        iBtnPre.setOnClickListener(this);
        iBtnPay = (ImageButton)findViewById(R.id.btnOrder);
        iBtnPay.setOnClickListener(this);

        edtAgentName = (EditText)findViewById(R.id.edtAgentName);
        edtAgentPhone = (EditText)findViewById(R.id.edtPhone);

        edtCustomerName = (EditText)findViewById(R.id.edtCustomerInfo);
        edtCustomerPhone = (EditText)findViewById(R.id.edtPhone2);
        tvCustomerAddress = (TextView)findViewById(R.id.tvAddress);

        user_name = getStringPreference(Define.PREF_USER_NAME);
        if(user_name.length() > 0) edtCustomerName.setText(user_name);

        user_phone = getStringPreference(Define.PREF_USER_TEL);
        if(user_phone.length() > 0) edtCustomerPhone.setText(user_phone);

        user_addr = getStringPreference(Define.PREF_USER_ADDR);
        zip_code = getStringPreference(Define.PREF_ZIP_CODE);
        if(user_addr.length() > 0){
            tvCustomerAddress.setText(user_addr);
            isAddressComplete = true;
        }


        tvTotalPrice = (TextView)findViewById(R.id.tvTotPrice);
        edtGoods = (EditText)findViewById(R.id.edtGoods);
        edtGoodsNum = (EditText)findViewById(R.id.edtGoodsNum);

        btnOrderLow = (Button)findViewById(R.id.btnOrderLow);
        btnOrderLow.setOnClickListener(this);

        edtAgentName.setText(Shop_Name);
        edtAgentPhone.setText(Shop_Tel);

        edtGoods.setText(Album_Title);

        if(Album_type == Define.TYPE_GRID){
            Album_num = alFrameList.size();
            Cupon_num = 0;
            edtGoodsNum.setText("" + alFrameList.size());
            edtGoodsNum.setClickable(false);
            edtGoodsNum.setEnabled(false);
            if((Album_price * Album_num) >= albumDeliverNum) mOrderInfo.delevery_price = 0;
        } else {
            Album_num = 1;
            Cupon_num = 0;
            edtGoodsNum.setText("1");
        }

        radioGroup = (RadioGroup)findViewById(R.id.rgDelivery);
        radioGroup.check(R.id.rbhome);

        if((Album_price * Album_num) >= albumDeliverNum ){
            String sInfo = String.format("집으로 받기(2~3일 소요, 택배비 0원 발생)");
            ((RadioButton)findViewById(R.id.rbhome)).setText(Html.fromHtml(sInfo));
        } else {
            String sInfo = String.format("집으로 받기(2~3일 소요, 택배비 2500원 발생) <br><font color=\'red\'>%d원 이상 주문 시 배송비 무료입니다.</font>", albumDeliverNum);
            ((RadioButton)findViewById(R.id.rbhome)).setText(Html.fromHtml(sInfo));
        }

        if(cupon.toLowerCase().contains(Album_code.toLowerCase())){
            Cupon_num = 1;
        }

        edtGoodsNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(edtGoodsNum.getText().length() > 0) {
                    Album_num = Integer.parseInt(edtGoodsNum.getText().toString());
                    if((albumDeliverNum != 0) & (albumDeliverNum <= ((Album_num  - Cupon_num) * Album_price))){
                        mOrderInfo.delevery_price = 0;
                        tvTotalPrice.setText("총 결제하실 상품의 가격: " + makeStringComma("" + mOrderInfo.good_price * (Album_num  - Cupon_num)) + "원" );
                    } else {
                        mOrderInfo.delevery_price = 2500;
                        if(radioGroup.getCheckedRadioButtonId() == R.id.rbhome) {
                            tvTotalPrice.setText( "총 결제하실 상품의 가격: " + makeStringComma(""+mOrderInfo.good_price * (Album_num  - Cupon_num))
                                    + " 택배비: " + makeStringComma("" + mOrderInfo.delevery_price)
                                    + " = " +  makeStringComma("" + (mOrderInfo.good_price * (Album_num  - Cupon_num) + mOrderInfo.delevery_price)) + "원");

                        } else {
                            tvTotalPrice.setText("총 결제하실 상품의 가격: " + makeStringComma("" + mOrderInfo.good_price * (Album_num  - Cupon_num)) + "원" );

                        }
                    }
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rbhome) {
                    if(user_addr.length() == 0) {
                        isAddressComplete = false;
                        tvCustomerAddress.setText("");
                        tvCustomerAddress.setHint("눌러서 주소를 선택해 주세요");
                    } else {
                        tvCustomerAddress.setText(user_addr);
                    }

                    if((albumDeliverNum != 0) & (albumDeliverNum <= ((Album_num  - Cupon_num) * Album_price))){
                        mOrderInfo.delevery_price = 0;
                        tvTotalPrice.setText("총 결제하실 상품의 가격: " + makeStringComma("" + mOrderInfo.good_price * (Album_num  - Cupon_num)) + "원" );
                    } else {
                        mOrderInfo.delevery_price = 2500;
                        if(radioGroup.getCheckedRadioButtonId() == R.id.rbhome) {
                            tvTotalPrice.setText( "총 결제하실 상품의 가격: " + makeStringComma(""+mOrderInfo.good_price * (Album_num  - Cupon_num))
                                    + " 택배비: " + makeStringComma("" + mOrderInfo.delevery_price)
                                    + " = " +  makeStringComma("" + (mOrderInfo.good_price * (Album_num  - Cupon_num) + mOrderInfo.delevery_price)) + "원");

                        } else {
                            tvTotalPrice.setText("총 결제하실 상품의 가격: " + makeStringComma("" + mOrderInfo.good_price * (Album_num  - Cupon_num)) + "원" );

                        }
                    }

                } else if(checkedId == R.id.rbagent){
                    user_addr = tvCustomerAddress.getText().toString();
                    tvCustomerAddress.setText(Shop_Addr);
                    isAddressComplete = true;
                    mOrderInfo.delevery_price = 0;
                    tvTotalPrice.setText("총 결제하실 상품의 가격: " + makeStringComma("" + mOrderInfo.good_price * (Album_num  - Cupon_num)) + "원" );
                }
            }
        });
        tvCustomerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAddressInput();
            }
        });

        tvTotalPrice.setText( "총 결제하실 상품의 가격: " + makeStringComma(""+mOrderInfo.good_price * (Album_num  - Cupon_num))
                + " 택배비: " + makeStringComma("" + mOrderInfo.delevery_price)
                + " = " +  makeStringComma("" + (mOrderInfo.good_price * (Album_num  - Cupon_num) + mOrderInfo.delevery_price)) + "원");

    }
    protected String makeStringComma(String str) {
        if (str.length() == 0)
            return "";
        long value = Long.parseLong(str);
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(value);
    }
    private void goToAddressInput() {
        Intent intent = new Intent(this, AddressActivity.class);
        startActivityForResult(intent, GOTO_ZIPCODE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnOrderLow:
            case R.id.btnOrder:
                if(isAddressComplete) {
                    //if(radioGroup.getCheckedRadioButtonId() == R.id.rbhome) {
                        if(edtCustomerName.getText().toString().trim().length() > 1 &
                                edtCustomerPhone.getText().toString().trim().length() > 6 ){
                            user_name = edtCustomerName.getText().toString().trim();
                            user_phone = edtCustomerPhone.getText().toString().trim();
                            saveStringPreference(Define.PREF_USER_NAME, user_name);
                            saveStringPreference(Define.PREF_USER_TEL, user_phone);
                            saveStringPreference(Define.PREF_USER_ADDR, user_addr);
                            saveStringPreference(Define.PREF_ZIP_CODE, zip_code);

                            prepareNetworking(Define.HTTP_ORDER_PRODUCT, "GET");
                        } else {
                            Toast.makeText(this, "사용자 정보(이름,전화번호)를 정확하게 입력해 주세요.", Toast.LENGTH_LONG).show();
                        }
                    //} else {
                    //    prepareNetworking(Define.HTTP_ORDER_PRODUCT, "GET");
                    //}

                } else {
                    Toast.makeText(this, "주소를 입력해 주세요.", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnPre:
                onBackPressed();
                break;
        }
    }

    private void goToSendImage() {
        Intent intent = new Intent(this, SendImageActivity.class);
        try {
            int del_price = 0;
            Album_num = Integer.parseInt(edtGoodsNum.getText().toString());

            alParam.add(new ParamVO("P_No", edtGoodsNum.getText().toString()));

            mOrderInfo.setUserInfo( edtCustomerName.getText().toString(),
                                    edtCustomerPhone.getText().toString() ,
                                    user_addr);

            mOrderInfo.setAgentInfo(mOrderInfo.agent_name, mOrderInfo.agent_phone);            // 대리점 정보 추가 : 대리점 이름, 대리점 전화번호

            mOrderInfo.setGoodInfo(mOrderInfo.good_name, Album_num, mOrderInfo.good_price * (Album_num  - Cupon_num), mOrderInfo.delevery_price);  // 상품 정보 추가 : 상품 이름, 갯수, 가격, 배송비

            mOrderInfo.setOrderNumber(orderNumber); // 주문 번호 설정

        }catch(NullPointerException ne) {
            Log.d(TAG, "NullPointerException : goToSendImage() ");
            ne.printStackTrace();
        }

        intent.putExtra("AlbumTitle", Album_Title);
        intent.putExtra("AlbumCode", Album_code);
        intent.putExtra("AlbumSubCode", Album_sub_code);
        intent.putExtra("UserName", edtCustomerName.getText().toString());
        intent.putExtra("AlbumPrice", Album_price);
        intent.putExtra("ALFrameInfo", alFrameList);
        intent.putExtra("OrderInfo", mOrderInfo);
        intent.putExtra("inwha_yn", inwha_yn);
        startActivityForResult(intent, GOTO_SEND_IMAGE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
    public void Complete() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GOTO_SEND_IMAGE) {
            if(resultCode == RESULT_OK) {
                Complete();
            }
        } else if(requestCode == GOTO_ZIPCODE) {
            if(resultCode == RESULT_OK) {
                zip_code = data.getStringExtra("ZIP_CODE");
                user_addr = data.getStringExtra("ADDRESS");
                //Log.d(TAG, "Get Address :" + user_addr + " " + zip_code);
                tvCustomerAddress.setText(user_addr);
                isAddressComplete = true;
            }
        }
    }
    private void parseJson(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            if(jObject.getString("RESULT").equals("SUCCESS")) {
                //GoToPayProcess();
                orderNumber = jObject.getString("order_sum_id");
                saveStringPreference(Define.KEY_CUPON_PRODUCT, "");
                saveStringPreference(Define.KEY_CUPON, "");
                goToSendImage();
            } else {
                Toast.makeText(this, "주문 전송에 실패하였습니다.", Toast.LENGTH_LONG).show();
            };
        }catch(JSONException je) {
            Toast.makeText(this, "서버에서 주문 응답에 에 실패하였습니다.", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void makeParams() {
        alParam.clear();
        alParam.add(new ParamVO("a_agent",Shop_Code));
        alParam.add(new ParamVO("a_prs",Shop_Id));
        alParam.add(new ParamVO("order_name", edtCustomerName.getText().toString()));
        alParam.add(new ParamVO("order_hp", edtCustomerPhone.getText().toString()));

        String zip[];
        if(radioGroup.getCheckedRadioButtonId() == R.id.rbhome) {
            alParam.add(new ParamVO("order_p", "집"));
            alParam.add(new ParamVO("addr", user_addr));
            zip = zip_code.split("-");
        }else {
            alParam.add(new ParamVO("order_p", "대리점"));
            alParam.add(new ParamVO("addr", Shop_Addr));
            zip = Shop_Zip.split("-");
        }
        if(zip.length > 1) {
            alParam.add(new ParamVO("zip1", zip[0]));
            alParam.add(new ParamVO("zip2", zip[1]));
        } else {
            alParam.add(new ParamVO("zip1", zip_code));
        }
        alParam.add(new ParamVO("product_code", Album_code));
        alParam.add(new ParamVO("product_cover", Album_sub_code));

        if(cupon.toLowerCase().contains(Album_code.toLowerCase())){
            alParam.add(new ParamVO("c_cupun",getStringPreference(Define.KEY_CUPON)));
        }

        try {
            int pNum = Integer.parseInt(edtGoodsNum.getText().toString());

            alParam.add(new ParamVO("P_No", edtGoodsNum.getText().toString()));

        }catch(NullPointerException ne) {

        }
    }

    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    //Log.d(TAG, "Result(JSON):" + r);
                    parseJson(r);
                    return;
                }
            }
        }
        Toast.makeText(this, "정보 요청에 실패하였습니다.", Toast.LENGTH_LONG).show();
    }
}
