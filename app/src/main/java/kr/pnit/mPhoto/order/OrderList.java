package kr.pnit.mPhoto.order;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.OrderInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;
import kr.pnit.mPhoto.main.Main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by macmini on 14. 12. 1..
 */
public class OrderList extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    Button btnGotoHome;
    ListView lvOrder;
    OrderListAdapter laOrder;

    String user_id;
    String order_name = null;
    String order_hp = null;

    boolean isCancel = false;
    OrderData CancelInfo;

    private static final int HANDLE_ORDER_CANCEL = 0x01;
    private static final int HANDLE_REQUEST_ORDER = 0x02;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case HANDLE_ORDER_CANCEL:
                    int item = (int)msg.arg1;
                    CancelInfo = alOrderData.get(item);
                    Log.d(TAG, "Order Cancel : " + CancelInfo.code + " " + CancelInfo.product + " " + CancelInfo.price);
                    String txt = "상품 : " + CancelInfo.product + "\n가격 : " + CancelInfo.price + "\n상품을 취소 하시겠습니까?";
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(OrderList.this);
                    alert_confirm.setMessage(txt).setCancelable(false).setPositiveButton("취소요청",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    isCancel = true;
                                    prepareNetworking(Define.HTTP_ORDER_CANCEL, "GET");
                                }
                            }).setNegativeButton("아니요",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    return;
                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                    break;
                case HANDLE_REQUEST_ORDER:
                    requestList();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list);
        getActionBar().hide();

        alOrderData = new ArrayList<OrderData>();

        initLayout();
    }
    public void initLayout() {
        btnGotoHome = (Button)findViewById(R.id.btnGotoHome);
        btnGotoHome.setOnClickListener(this);

        lvOrder = (ListView)findViewById(R.id.lvOrderList);
        laOrder = new OrderListAdapter(this,R.layout.order_item, alOrderData);
        lvOrder.setAdapter(laOrder);

        user_id = getStringPreference(Define.KEY_PRE_ID);
        requestList();
    }
    private void requestList() {
        if(order_name == null & order_hp == null) {
            Log.d(TAG, "Create UserInfo Dialog");
            createUserInfoDialog();
        } else {
            prepareNetworking(Define.HTTP_ORDER_LIST, "GET");
        }
    }
    private void createUserInfoDialog() {
        AlertDialog.Builder builder;
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate( R.layout.userinfo_dialog,
                (ViewGroup)findViewById( R.id.layout_root));
        final EditText edtName = (EditText)layout.findViewById(R.id.edtCustomerName);
        final EditText edtHP = (EditText)layout.findViewById(R.id.edtCustomerHp);
        final Button btnCompelte = (Button)layout.findViewById(R.id.btnComplete);

        order_name = getStringPreference(Define.PREF_USER_NAME);
        if(order_name.length() > 0) edtName.setText(order_name);
        order_hp = getStringPreference(Define.PREF_USER_TEL);
        if(order_hp.length() > 0) edtHP.setText(order_hp);

        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();

        btnCompelte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order_name = edtName.getText().toString();
                order_hp = edtHP.getText().toString();
                if(order_name.trim().length() > 0 &
                        order_hp.trim().length() > 0) {
                    prepareNetworking(Define.HTTP_ORDER_LIST, "GET");
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(OrderList.this, "", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }
    private class OrderData {
        public String date;
        public String name;
        public String product;
        public String price;
        public String status;
        public String code;
    }
    ArrayList<OrderData> alOrderData;
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnGotoHome:
                onBackPressed();
                break;
        }
    }

    private class OrderListAdapter extends ArrayAdapter<OrderData> {
        private ArrayList<OrderData> items;

        public OrderListAdapter(Context context, int textViewResourceId, ArrayList<OrderData> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.order_item, null);
            }

            final OrderData orderData = items.get(position);
            if (orderData != null) {
                ((TextView)v.findViewById(R.id.tvDate)).setText(orderData.date);
                ((TextView)v.findViewById(R.id.tvName)).setText(orderData.name);
                ((TextView)v.findViewById(R.id.tvGoods)).setText(orderData.product);
                ((TextView)v.findViewById(R.id.tvPrice)).setText(orderData.price + "원");
                ((TextView)v.findViewById(R.id.tvStatus)).setText(orderData.status);
                ((Button)v.findViewById(R.id.btnOrderCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Cancel
                        Message msg = new Message();
                        msg.what = HANDLE_ORDER_CANCEL;
                        msg.arg1 = position;
                        mHandler.sendMessage(msg);
                    }
                });
            }
            return v;
        }
    }


    private void parseJson(String json) {
        try{
            alOrderData.clear();
            JSONObject jObject = new JSONObject(json);
            JSONArray jArray = jObject.getJSONArray("product");
            for(int i = 0; i < jArray.length() ; i++) {
                OrderData order = new OrderData();
                JSONObject j = jArray.getJSONObject(i);

                order.date = j.getString("Order_datetime");
                order.name = j.getString("order_name");
                order.product = j.getString("product_name_value");
                order.price = j.getString("P_price");
                order.code  = j.getString("order_sum_id");

                alOrderData.add(order);
            }

            laOrder.notifyDataSetChanged();

        }catch(JSONException je) {

        }
    }
    @Override
    public void onBackPressed() {
        finish();
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
        if(isCancel){
            alParam.add(new ParamVO("order_sum_id",CancelInfo.code));
        } else {
            alParam.add(new ParamVO("userID",user_id));
            alParam.add(new ParamVO("order_name",order_name));
            alParam.add(new ParamVO("order_hp",order_hp));
        }
    }

    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    Log.d(TAG, "Result(JSON):" + r);
                    if(isCancel){
                        isCancel = false;
                        CancelInfo = null;
                        Toast.makeText(OrderList.this, "주문이 취소되었습니다.", Toast.LENGTH_LONG).show();
                        mHandler.sendEmptyMessage(HANDLE_REQUEST_ORDER);
                    } else {
                        parseJson(r);
                    }
                    return;
                }
            }
        }
        Toast.makeText(this, "정보 요청에 실패하였습니다.", Toast.LENGTH_LONG).show();
    }
}
