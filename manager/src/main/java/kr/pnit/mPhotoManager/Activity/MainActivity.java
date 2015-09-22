package kr.pnit.mPhotoManager.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.pnit.mPhotoManager.Define.Define;
import kr.pnit.mPhotoManager.Network.ParamVO;
import kr.pnit.mPhotoManager.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    Button btnPushOnOff;
    ListView lvOrder;
    OrderListAdapter orderListAdapter;


    private final int MODE_REG_PUSH = 0x100;
    private final int MODE_REQ_LIST = 0x101;

    int mode = MODE_REQ_LIST;

    int page_item_num = 0;
    int page = 0;
    boolean isEnd = false;

    boolean isPushEnable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);

        btnPushOnOff = (Button)findViewById(R.id.btPushOnOff);
        btnPushOnOff.setOnClickListener(this);

        alOrderInfo = new ArrayList<OrderInfo>();
        lvOrder = (ListView)findViewById(R.id.lvOrder);
        orderListAdapter = new OrderListAdapter(this, alOrderInfo);
        lvOrder.setAdapter(orderListAdapter);

        lvOrder.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                //Log.d(TAG, "onScroll lastInScreen:" + lastInScreen);
                if(alOrderInfo.size() > 0) {
                    if((lastInScreen == totalItemCount) && !(isEnd)){
                        Log.d(TAG, "Add More Item");
                        if(!isRequest)
                            prepareNetworking(Define.HTTP_GET_PAGE, "GET");
                    }
                }
            }
        });


        isPushEnable = getBooleanPreference(Define.PARAM_PUSH_ONOFF);

        if(isPushEnable){
            btnPushOnOff.setText("푸쉬 알람 끄기");
        } else {
            btnPushOnOff.setText("푸쉬 알람 켜기");
        }

        prepareNetworking(Define.HTTP_GET_PAGE, "GET");
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btPushOnOff:
                mode = MODE_REG_PUSH;
                if(isPushEnable){
                    btnPushOnOff.setText("푸쉬 알람 켜기");
                    isPushEnable = false;
                } else {
                    btnPushOnOff.setText("푸쉬 알람 끄기");
                    isPushEnable = true;
                }
                prepareNetworking(Define.HTTP_SET_PUSH, "GET");
                break;
        }
    }

    private void setSavePushOnOff(boolean status) {
        SharedPreferences shared = getSharedPreferences(Define.PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(Define.PARAM_PUSH_ONOFF, status);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void SetPoint(int point) {
        ((TextView)findViewById(R.id.tvTotalPoint)).setText("" + point + " p");
    }



    private class OrderListAdapter extends ArrayAdapter<OrderInfo> {
        LayoutInflater inflater;
        Context context;
        OrderListAdapter(Context context, ArrayList<OrderInfo> list){
            super(context, 0, list);
            this.context = context;
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.item_layout, null);
            }
            OrderInfo info = (OrderInfo)getItem(position);

            ((TextView)convertView.findViewById(R.id.tvDate)).setText(info.order_date);
            ((TextView)convertView.findViewById(R.id.tvName)).setText(info.order_name);
            ((TextView)convertView.findViewById(R.id.tvPhone)).setText(info.order_hp);
            ((TextView)convertView.findViewById(R.id.tvAgent)).setText(info.pre_name);
            ((TextView)convertView.findViewById(R.id.tvProduct)).setText(info.product_name);
            ((TextView)convertView.findViewById(R.id.tvPayPoint)).setText(""+info.p_amt + "원 "+info.point+"P");

            return convertView;
        }
    }
    private class OrderInfo {
        String order_date;
        String order_name;
        String order_hp;
        String pre_name;
        String product_name;
        int p_amt;
        int point;
    }
    ArrayList<OrderInfo> alOrderInfo;
    boolean isRequest = false;
    private boolean parseLogin(String json) {
        try{
            JSONObject j = new JSONObject(json);
            if(j.getString("RESULT").equals("SUCCESS")) {

                int item_num = j.getInt(Define.PARAM_PAGE_NO);

                page_item_num = j.getInt(Define.PARAM_PAGE_NO);
                page  = j.getInt(Define.PARAM_PAGE);
                SetPoint(j.getInt(Define.PARAM_OW_POINT_VALUE));

                JSONArray orderList = j.getJSONArray(Define.PARAM_ORDER_LIST);

                if(page_item_num != orderList.length()) {
                    Log.d(TAG, "List End!!!" + page_item_num + " " + orderList.length());
                    isEnd = true;
                } else {
                    isEnd = false;
                }

                for(int i = 0; i < orderList.length(); i++){
                    JSONObject jItem = orderList.getJSONObject(i);

                    OrderInfo info = new OrderInfo();
                    info.order_date = jItem.getString(Define.PARAM_DATE_TIME);
                    info.order_name = jItem.getString(Define.PARAM_ORDER_NAME);
                    info.order_hp = jItem.getString(Define.PARAM_ORDER_HP);
                    info.pre_name = jItem.getString(Define.PARAM_PRE_NAME_VALUE);
                    info.product_name = jItem.getString(Define.PARAM_PRODUCT_NAME_VALUE);
                    info.p_amt = jItem.getInt(Define.PARAM_P_AMT);
                    info.point = jItem.getInt(Define.PARAM_TOTAL_POINT_VALUE);

                    alOrderInfo.add(info);
                }
                Log.d(TAG, "TOTAL List : " + alOrderInfo.size() + " Order List :" + orderList.length());


                orderListAdapter.notifyDataSetChanged();

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
    public void makeParams() {
        alParam.clear();
        if(mode == MODE_REG_PUSH){
            alParam.add(new ParamVO("UserGubun", getStringPreference(Define.PARAM_USER_GUBUN)));
            alParam.add(new ParamVO("UserID",getStringPreference(Define.PARAM_ID)));
            alParam.add(new ParamVO("RegId",getStringPreference(Define.PARAM_PUSH_ID)));
            alParam.add(new ParamVO("Push_OnOff",(isPushEnable?"On":"Off")));
            setSavePushOnOff(isPushEnable);
        } else if(mode == MODE_REQ_LIST){
            isRequest = true;
            alParam.add(new ParamVO(Define.PARAM_USER_GUBUN, getStringPreference(Define.PARAM_USER_GUBUN)));
            alParam.add(new ParamVO(Define.PARAM_USERID,getStringPreference(Define.PARAM_ID)));
            alParam.add(new ParamVO(Define.PARAM_PAGE,""+(page+1)));
        }
    }

    @Override
    public void parseResult(String result) {
        isRequest = false;
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    //Log.d(TAG, "Result(JSON):" + r);
                    if(mode == MODE_REQ_LIST){
                        if(parseLogin(r)){
                            return;
                        }
                    }else {

                        return;
                    }
                }
            }
        }

    }
}
