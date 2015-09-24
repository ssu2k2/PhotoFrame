package kr.pnit.mPhoto.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by macmini on 14. 11. 28..
 */
public class OrderMain extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    GridView gvItem;
    ImageButton ivPre;
    ImageButton ivNext;

    String cupon;

    private ImageLoaderConfiguration config;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_main);
        getActionBar().hide();
        setUpTitleBar();

        // Create configuration for ImageLoader
        config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2000000)) // You can pass your own memory cache implementation
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.imgloadin)
                .showImageOnFail(R.drawable.imgloadin)
                .cacheInMemory(true)
                        //.cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        cupon = getStringPreference(Define.KEY_CUPON_NUM);
        //Log.d(TAG, "CUPON NUMBER : " + cupon);
        // Get singleton instance of ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        init();
    }
    private void setUpTitleBar() {
        ivPre = (ImageButton)findViewById(R.id.btnPre);
        ivNext = (ImageButton)findViewById(R.id.btnNext);

        ivPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Prev Clicked!");
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Next Clicked!");

            }
        });
    }

    private final static int MODE_GET_MAIN = 0x100;
    private final static int MODE_GET_SUB  = 0x101;

    int mode = MODE_GET_MAIN;

    ArrayList<AlbumInfo> alAlbumInfo;

    AlbumInfo SelectedAlbum = null;

    ArrayList<AlbumInfo> alSubAlbumInfo;

    GridAdapter gridAdapter;
    GridAdapter gridSubAdapter;

    private void init() {

        alAlbumInfo = new ArrayList<AlbumInfo>();
        alSubAlbumInfo = new ArrayList<AlbumInfo>();

        gvItem = (GridView)findViewById(R.id.gdItemList);

        gridAdapter = new GridAdapter(this, R.layout.album_item, alAlbumInfo);
        gridSubAdapter = new GridAdapter(this, R.layout.album_item, alSubAlbumInfo);

        gvItem.setAdapter(gridAdapter);

        gvItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SelectedAlbum == null) {
                        mode = MODE_GET_SUB;
                        SelectedAlbum = alAlbumInfo.get(position);
                        prepareNetworking(Define.HTTP_PRODUCT_SUB_LIST, "GET");
                } else {
                    // Sub List Selectioni
                    if(position == 0) {
                        // Back to Main List;
                        mode = MODE_GET_MAIN;
                        SelectedAlbum = null;
                        prepareNetworking(Define.HTTP_PRODUCT_LIST, "GET");
                    } else {
                        if(alSubAlbumInfo.get(position).type == AlbumInfo.TYPE_PHOTOFRAME)
                            goToAlbumMaker(position);
                        else if(alSubAlbumInfo.get(position).type == AlbumInfo.TYPE_PHOTOALONE)
                            goToGridOrder(position);
                        else
                            goToAlbumMaker(position);
                    }
                }
            }
        });

        prepareNetworking(Define.HTTP_PRODUCT_LIST, "GET");

    }

    // For Test
    private final int GOTO_ORDERINPUT = 0x10;


    private void goToAlbumMaker(int position) {
        Intent intent = new Intent(this, OrderAlbum.class);
        intent.putExtra("ITEM", alSubAlbumInfo.get(position));
        startActivity(intent);
        finish();
    }
    private void goToGridOrder(int position) {
        Intent intent = new Intent(this, OrderGrid.class);
        intent.putExtra("ITEM", alSubAlbumInfo.get(position));
        startActivity(intent);
        finish();
    }

    private class Holder {
        ImageView ivAlbum;
        TextView tvTitle;
    }
    private class GridAdapter extends ArrayAdapter<AlbumInfo> {
        LayoutInflater inflater;
        int res;

        public GridAdapter(Context context, int res, List<AlbumInfo> list) {
            super(context, res, list);
            this.res = res;
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            View v;
            if(convertView == null) {
                v = inflater.inflate(this.res, parent, false);
                holder = new Holder();
                holder.ivAlbum = (ImageView)v.findViewById(R.id.ivImage);
                holder.tvTitle = (TextView)v.findViewById(R.id.tvTitle);
                v.setTag(holder);
            } else {
                v = convertView;
                holder = (Holder)v.getTag();
            }
            AlbumInfo info = getItem(position);
            if(position == 0){
                if(info.title.equals("대분류로")) {
                    holder.tvTitle.setText("대분류로");
                    holder.ivAlbum.setImageResource(R.drawable.goback);
                    return v;
                }
            }
            if(cupon.toLowerCase().contains(info.code.toLowerCase())){
                holder.tvTitle.setText(Html.fromHtml(info.title + "<br><font color=\"red\">쿠폰용 상품 무료</font>"));
            } else {
                holder.tvTitle.setText(info.title + "\n" + makeStringComma(""+info.price) + "원");
            }

            if(info.sub_imgURL.length() != 0){
                ImageLoader.getInstance().displayImage(info.sub_imgURL, (ImageView) holder.ivAlbum, options);
            } else {
                ImageLoader.getInstance().displayImage(info.imgURL, (ImageView) holder.ivAlbum, options);
            }

            return v;
        }
    }
    protected String makeStringComma(String str) {
        if (str.length() == 0)
            return "";
        long value = Long.parseLong(str);
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(value);
    }

    private void parseJson(String json) {
        try{
            JSONObject j = new JSONObject(json);

            if(j.getString("RESULT").equals("SUCCESS")) {

                JSONArray jArray = j.getJSONArray("product");
                if(mode == MODE_GET_MAIN){
                    alAlbumInfo.clear();
                    alSubAlbumInfo.clear();
                    gvItem.setAdapter(gridAdapter);

                    for(int i = 0; i < jArray.length(); i++)
                    {
                        JSONObject jObj = jArray.getJSONObject(i);
                        AlbumInfo album = new AlbumInfo();
                        album.code = jObj.getString("product_code");
                        album.edit_gubun = Integer.parseInt(jObj.getString("product_edit_gubun"));
                        album.ratio_width = Integer.parseInt(jObj.getString("product_W"));
                        album.ratio_height = Integer.parseInt(jObj.getString("product_H"));

                        album.resolution_W = Integer.parseInt(jObj.getString("resolution_W"));
                        album.resolution_H = Integer.parseInt(jObj.getString("resolution_H"));

                        album.inwha_yn = "Y" ;//jObj.getString("inwha_yn");
                        album.title = jObj.getString("product_name");
                        album.price = Integer.parseInt(jObj.getString("product_price"));
                        album.imgURL = jObj.getString("product_img_url");
                        album.deliver_num = 30000;//Integer.parseInt(jObj.getString("product_deliver"));
                        album.maxPage = Integer.parseInt(jObj.getString("product_Page"));

                        if(album.edit_gubun  == 3)
                            album.type = AlbumInfo.TYPE_PHOTOALONE;
                        else
                            album.type = AlbumInfo.TYPE_PHOTOFRAME;

                        alAlbumInfo.add(album);
                    }
                    //Log.d(TAG, "MODE_GET_MAIN:" + alAlbumInfo.size());
                    gridAdapter.notifyDataSetChanged();

                } else {
                    alAlbumInfo.clear();
                    alSubAlbumInfo.clear();
                    gvItem.setAdapter(gridSubAdapter);
                    AlbumInfo album0 = new AlbumInfo();
                    album0.title = "대분류로";
                    alSubAlbumInfo.add(album0); // Dummy

                    for(int i = 0; i < jArray.length(); i++)
                    {
                        JSONObject jObj = jArray.getJSONObject(i);
                        AlbumInfo album = new AlbumInfo();
                        album.code = SelectedAlbum.code;
                        album.edit_gubun = SelectedAlbum.edit_gubun;
                        album.ratio_width = SelectedAlbum.ratio_width;
                        album.ratio_height = SelectedAlbum.ratio_height;

                        album.resolution_W = SelectedAlbum.resolution_W;
                        album.resolution_H = SelectedAlbum.resolution_H;

                        album.title = SelectedAlbum.title;
                        album.price = SelectedAlbum.price;
                        album.imgURL = SelectedAlbum.imgURL;
                        album.deliver_num = SelectedAlbum.deliver_num;
                        album.maxPage = SelectedAlbum.maxPage;
                        album.inwha_yn = SelectedAlbum.inwha_yn;

                        album.sub_code = jObj.getString("product_code_no");
                        album.sub_name = jObj.getString("product_name");
                        album.sub_imgURL = jObj.getString("product_img_url");
                        album.sub_price = Integer.parseInt(jObj.getString("product_price"));

                        if(album.edit_gubun  == 3)
                            album.type = AlbumInfo.TYPE_PHOTOALONE;
                        else
                            album.type = AlbumInfo.TYPE_PHOTOFRAME;
                        alSubAlbumInfo.add(album);
                    }
                    //Log.d(TAG, "MODE_GET_SUB:" + alSubAlbumInfo.size());

                    gridSubAdapter.notifyDataSetChanged();
                }
            } else {
                if(mode == MODE_GET_SUB){
                    mode = MODE_GET_MAIN;
                    SelectedAlbum = null;
                    Toast.makeText(OrderMain.this, "상품 목록 요청이 실패하였습니다.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSONException" + je.getMessage());
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void makeParams() {
        alParam.clear();
        if(mode == MODE_GET_SUB){
            if(SelectedAlbum != null)
                alParam.add(new ParamVO("product_code",SelectedAlbum.code));
        } else {
            SelectedAlbum = null;
        }
    }

    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    Log.d(TAG, "Result(JSON):" + r);
                    parseJson(r);
                    return;
                }
            }
        }
        Toast.makeText(this, "정보 요청에 실패하였습니다.", Toast.LENGTH_LONG).show();
    }
}
