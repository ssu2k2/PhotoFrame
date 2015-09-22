package kr.pnit.mPhoto.order;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    ArrayList<OrderInfo> alOrderInfo;

    String user_id;
    String order_name = null;
    String order_hp = null;

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
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.order_item, null);
            }

            OrderData orderData = items.get(position);
            if (orderData != null) {
                ((TextView)v.findViewById(R.id.tvDate)).setText(orderData.date);
                ((TextView)v.findViewById(R.id.tvName)).setText(orderData.name);
                ((TextView)v.findViewById(R.id.tvGoods)).setText(orderData.product);
            }
            return v;
        }
    }


    private void parseJson(String json) {
        try{
            JSONObject jObject = new JSONObject(json);
            JSONArray jArray = jObject.getJSONArray("product");
            for(int i = 0; i < jArray.length() ; i++) {
                OrderData order = new OrderData();
                JSONObject j = jArray.getJSONObject(i);

                order.date = j.getString("Order_datetime");
                order.name = j.getString("order_name");
                order.product = j.getString("product_name_value");

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
        alParam.add(new ParamVO("userID",user_id));
        alParam.add(new ParamVO("order_name",order_name));
        alParam.add(new ParamVO("order_hp",order_hp));
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
