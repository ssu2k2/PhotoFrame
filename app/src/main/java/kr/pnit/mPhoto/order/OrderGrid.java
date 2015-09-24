package kr.pnit.mPhoto.order;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.DTO.FrameUris;
import kr.pnit.mPhoto.DTO.PhotoInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Dialog.ImageViewActivity;
import kr.pnit.mPhoto.Dialog.SendImageActivity;
import kr.pnit.mPhoto.PhotoSelector.PhotoSelectorActivity;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.Utils.FileUtils;
import kr.pnit.mPhoto.Utils.ImageUtils;
import kr.pnit.mPhoto.albummaker.EmoticonFrame;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by macmini on 14. 11. 29..
 */
public class OrderGrid extends Activity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    LinearLayout llPhoto;
    LinearLayout llPage;
    LayoutInflater inflater;

    // Album Type
    LinearLayout llPhotoGrid;


    // ActionBar Navigation
    ImageButton iBtnPre;
    ImageButton iBtnOrder;

    Button btnAutoSelect;           // Auto Select Button
    Button btnSelectPhoto;
    AlbumInfo albumInfo;

    private ArrayList<PhotoInfo> alPhotoList;


    private ImageLoaderConfiguration config;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private String APP_FOLDER = "Pnit";

    private ArrayList<FrameInfo> alFrameInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_grid);
        getActionBar().hide();
        alFrameInfo = new ArrayList<FrameInfo>();

        alPhotoList = new ArrayList<PhotoInfo>();

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // Create configuration for ImageLoader
        config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2000000)) // You can pass your own memory cache implementation
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.imgloadin)
                .showImageForEmptyUri(R.drawable.imgloadin)
                .showImageOnFail(R.drawable.imgloadin)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        // Get singleton instance of ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        setAlbumInfo();
        initLayout();
    }
    private void initAppFolder() {

    }
    private void setAlbumInfo() {
        Intent intent = getIntent();
        albumInfo = (AlbumInfo)intent.getSerializableExtra("ITEM");
        Log.d(TAG, "Album Info :" + albumInfo.maxPage + " Ratio W:" + albumInfo.ratio_width + " H:" + albumInfo.ratio_height);
    }

    ImageView ivEmptyMessage;
    GridAdapter gridAdapter;
    GridView gvItem;
    ArrayList<ImageInfo> alImageURL = new ArrayList<ImageInfo>();
    /**
     * Layout 설정
     */
    private void initLayout() {

        llPhotoGrid = (LinearLayout)findViewById(R.id.llPhotoGrid);
        llPhoto = (LinearLayout) findViewById(R.id.llPhotoSelect);

        btnSelectPhoto = (Button)findViewById(R.id.btnSelectPhoto);
        btnSelectPhoto.setOnClickListener(this);

        btnAutoSelect = (Button)findViewById(R.id.btnAutoSelect);
        btnAutoSelect.setOnClickListener(this);

        llPhotoGrid.setVisibility(View.VISIBLE);

        ivEmptyMessage = (ImageView)findViewById(R.id.ivEmptyMessage);

        gvItem = (GridView)findViewById(R.id.gdPhotoList);
        gridAdapter = new GridAdapter(this, R.layout.grid_item, alImageURL);
        gvItem.setAdapter(gridAdapter);


        gridAdapter.notifyDataSetChanged();

        iBtnOrder = (ImageButton)findViewById(R.id.btnOrder);
        iBtnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alImageURL.size() == 0) {
                    Toast.makeText(OrderGrid.this, "선택된 사진이 없습니다.", Toast.LENGTH_LONG).show();
                } else {
                    goToImageViewer();
                }
            }
        });
        iBtnPre = (ImageButton)findViewById(R.id.btnPre);
        iBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAutoSelect:
                Message msg = new Message();
                msg.what = OrderAlbum.AUTO_SELECT_IMAGE;
                msg.obj = this;
                actionHandler.sendMessage(msg);
                break;
            case R.id.btnSelectPhoto:
                //doTakeAlbumAction();
                doTakePhotoFromSelector();
                break;
        }
    }

    public static final int AUTO_SELECT_IMAGE = 0x2;

    private boolean isAutoSelectRunning = false;

    Handler actionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case AUTO_SELECT_IMAGE:
                    Log.d(TAG, "Auto Select Image");
                    boolean isCropPass = false;
                    Uri uri  = null;
                    for(PhotoInfo p : alPhotoList) {
                        if(!p.isCheck){
                            uri = p.uri;
                            break;
                        }
                    }
                    if(uri == null){
                        Log.d(TAG, "No more Images");
                        isAutoSelectRunning = false;
                        isCropPass = true;
                        return;
                    } else {
                        isAutoSelectRunning = true;
                        Log.d(TAG, "Auto Select Image :" + uri);
                    }

                    if(!isCropPass) {
                        doCropImage(uri, albumInfo.ratio_width, albumInfo.ratio_height);
                    }

                    break;
                default:
                    break;
            }

        }
        //}
    };

    /**
     * 앨범 호출 하기
     */
    private void doTakeAlbumAction()
    {
        Log.i(TAG, "doTakeAlbumAction()");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), GET_IMAGE_FROM_GALLERY);
    }

    private void doTakePhotoFromSelector()
    {
        Log.i(TAG, "doTakePhotoFromSelector()");
        Intent intent = new Intent(this, PhotoSelectorActivity.class);
        startActivityForResult(intent, GET_IMAGE_FROM_SELECTOR);
    }

    private boolean CheckImageFile(String name) {
        for(int i =0; i < alImageURL.size(); i++) {
            if(alImageURL.get(i).Crop_Image_Path.contains(name))
                return false;
        }
        return true;
    }
    Uri mImageCropUri;
    Uri mOriginalCropUri = null;
    ImageInfo cropImageInfo = null;

    private void doCropImage(Uri uri, int width, int height) {
        cropImageInfo = new ImageInfo();

        mOriginalCropUri = uri;

        File original = ImageUtils.getImageFile(this, uri);

        cropImageInfo.Original_URI = uri;
        cropImageInfo.Original_Image_Path = original.getPath();

        Log.d(TAG, "NAME :" +  original.getName());
//        if(!CheckImageFile(original.getName())){
//            Toast.makeText(this, "이미 포함된 파일 입니다.", Toast.LENGTH_LONG).show();
//            return;
//        }

        mImageCropUri = FileUtils.createSaveCropFile(original.getName());

        File copy = new File(mImageCropUri.getPath());
        FileUtils.copyFile(original, copy);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCropUri, "image/*");
        intent.putExtra( "scale", true );
        intent.putExtra( "aspectX", width );
        intent.putExtra( "aspectY", height );
        intent.putExtra("output", mImageCropUri);
        startActivityForResult(intent, CROP_IMAGE );
    }

    private void goToOrderMain() {
        Intent intent = new Intent(this, OrderMain.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        goToOrderMain();
    }

    private final int GOTO_ORDERINPUT        = 0x10;
    private final int GET_IMAGE_FROM_GALLERY = 0x11;
    private final int GET_IMAGE_FROM_SELECTOR= 0x12;

    private final int CROP_IMAGE             = 0x13;
    private final int GET_IMAGE_FROM_CAMERA  = 0x14;

    private View cropedView = null;
    private final int GOTO_SEND_IMAGE       = 0x15;
    private final int GOTO_IMAGE_VIEWER     = 0x16;


    private void setFrameInfo() {
        alFrameInfo.clear();
        for(int i = 0; i < alImageURL.size(); i++) {
            FrameInfo f = new FrameInfo();
            f.type = 0;
            f.isConvert = true;
            f.pageImage = alImageURL.get(i).Crop_Image_Path;
            alFrameInfo.add(f);
        }

    }

    private void goToImageViewer() {

        setFrameInfo();

        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra("AlbumTitle", albumInfo.title);
        intent.putExtra("AlbumCode", albumInfo.code);
        intent.putExtra("AlbumSubCode", albumInfo.sub_code);

        intent.putExtra("AlbumDeliverNum", ""+albumInfo.deliver_num);

        intent.putExtra("inwha_yn", albumInfo.inwha_yn);
        intent.putExtra("AlbumPrice", albumInfo.price);
        intent.putExtra("ALFrameInfo", alFrameInfo);

        intent.putExtra("AlbumType", Define.TYPE_GRID);

        startActivityForResult(intent, GOTO_IMAGE_VIEWER);
    }

    private boolean checkImageSize(int width, int height, Uri uri){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), options);
        Log.d(TAG, "checkImageSize " + uri.getPath() + " W:" + width + " W:" + options.outWidth);
        Log.d(TAG, "checkImageSize " + uri.getPath() + " H:" + height + " H:" + options.outHeight);
        if((width > options.outWidth) | (height > options.outHeight)) {
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == GOTO_IMAGE_VIEWER) {
            if(resultCode == RESULT_OK) {
                // Order Complete
                finish();
            } else {
                // Order Canceled!!!
            }
        }
        else if(requestCode == GOTO_ORDERINPUT) {
            if(resultCode == RESULT_OK) {
                // Order Complete
                finish();
            } else {
                // Order Canceled!!!
            }
        }
        else if(requestCode == GET_IMAGE_FROM_GALLERY) {
            Log.d(TAG, "GET_IMAGE_FROM_GALLERY");

            if (resultCode == Activity.RESULT_OK) {
                ClipData clip = data.getClipData();
                if(clip == null) {
                    ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    // iterate over these images
                    try {
                        for (Parcelable parcel : list) {
                            Uri uri = (Uri) parcel;
                            // handle the images one by one here
                            if (uri.toString().startsWith("content://media"))
                                addPhotoIcon(llPhoto, uri);
                            else
                                Log.d(TAG, "FILE PATH:" + uri.toString());
                        }
                    } catch (NullPointerException ne) {

                        Uri selectedImageUri = data.getData();
                        if(selectedImageUri != null)
                            addPhotoIcon(llPhoto, selectedImageUri);
                    }
                } else {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        ClipData.Item item = clip.getItemAt(i);
                        Uri uri = item.getUri();
                        if(uri.toString().startsWith("content://media"))
                            addPhotoIcon(llPhoto, uri);
                        else
                            Log.d(TAG, "FILE PATH:" + uri.toString());
                    }
                }
                //Do something with the uris array
            }
        }
        else if(requestCode == GET_IMAGE_FROM_SELECTOR){
            Log.d(TAG, "GET_IMAGE_FROM_SELECTOR");
            if(resultCode == RESULT_OK){
                String[] uris = data.getStringArrayExtra("URIS");
                Log.d(TAG, "GET URIS " + uris.length);
                for(int i = 0; i < uris.length; i++) {
                    Uri uri = Uri.parse(uris[i]);
                    addPhotoIcon(llPhoto, uri);
                }
            }
        }
        else if(requestCode == CROP_IMAGE)
        {
            Log.w(TAG, "CROP_IMAGE");
            if(resultCode == RESULT_OK) {
                // Crop 된 이미지를 넘겨 받습니다.
                Log.w(TAG, "mImageCropUri = " + mImageCropUri);
                String full_path = mImageCropUri.getPath();

                // CHECK IMAGE SIZE
                if((albumInfo.resolution_H != 0)& (albumInfo.resolution_W != 0)){
                    if(!checkImageSize(albumInfo.resolution_W,
                            albumInfo.resolution_H, mImageCropUri)){
                        Toast.makeText(this, "이미지가 작습니다. 제품의 품질이 저하될 수 있으니 큰 사진으로 교체하거나 사진을 크게 다시 잘라주세요.", Toast.LENGTH_LONG).show();
                        isAutoSelectRunning = false;
                    }

                } else {
                    if(!checkImageSize(100 * albumInfo.ratio_width,
                            100 * albumInfo.ratio_height, mImageCropUri)){
                        Toast.makeText(this, "이미지가 작습니다. 제품의 품질이 저하될 수 있으니 큰 사진으로 교체하거나 사진을 크게 다시 잘라주세요.", Toast.LENGTH_LONG).show();
                        isAutoSelectRunning = false;
                    }
                }

                cropImageInfo.Crop_Uri = mImageCropUri;
                cropImageInfo.Crop_Image_Path = full_path;

                Log.w(TAG, "Image path = "+full_path);
                alImageURL.add(cropImageInfo);

                gridAdapter.notifyDataSetChanged();


                if(alImageURL.size() > 0) {
                    ivEmptyMessage.setVisibility(View.GONE);
                    gvItem.setVisibility(View.VISIBLE);
                } else {
                    ivEmptyMessage.setVisibility(View.VISIBLE);
                    gvItem.setVisibility(View.GONE);
                }
                checkUsedPhoto(mOriginalCropUri);
                mOriginalCropUri = null;

                if(isAutoSelectRunning) {
                    Message msg = new Message();
                    msg.what = OrderAlbum.AUTO_SELECT_IMAGE;
                    msg.obj = this;
                    actionHandler.sendMessageDelayed(msg, 100);
                }

            } else {
                cropImageInfo = null;
            }
        }
    }

    private void checkUsedPhoto(Uri uri){

        if(uri == null) return;

        for(PhotoInfo p : alPhotoList) {
            if (p.uri == uri) {
                p.isCheck = true;
            }
        }
    }

    private LinearLayout addPhotoIcon(LinearLayout main, Uri uri) {
        Log.d(TAG, "addPhotoIcon :" + uri);
        String path = ImageUtils.getImagePath(this, uri);
        Log.d(TAG, "Image PATH :" + path);
        try {

            Bitmap bmp = ImageUtils.getThumbnail(getContentResolver(), path);
            FrameLayout llLayout = (FrameLayout) inflater.inflate(R.layout.photo_icon_layout, null);
            ImageView btn = (ImageView)llLayout.findViewById(R.id.imgIcon);//new ImageView(this);
            llLayout.findViewById(R.id.imgCheck).setVisibility(View.GONE);

            float d = getResources().getDisplayMetrics().density;
            int iconSize = (int) (50 * d);
            Log.d(TAG, "addPhotoIcon " + iconSize);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    iconSize, iconSize
            );
            params.setMargins(5, 0, 5, 0);
            llLayout.setLayoutParams(params);

            btn.setImageBitmap(bmp);
            btn.setScaleType(ImageView.ScaleType.FIT_XY);

            btn.setTag(R.string.tag_res_id, uri);
            btn.setClickable(true);
            btn.setOnClickListener(onClickListener);

            main.addView(llLayout);

            PhotoInfo pInfo = new PhotoInfo();

            pInfo.uri = uri;
            pInfo.isCheck = false;

            alPhotoList.add(pInfo);


        }catch (Exception e) {
            e.printStackTrace();
        }
        return main;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Uri uri = (Uri)v.getTag(R.string.tag_res_id);
            doCropImage(uri, albumInfo.ratio_width, albumInfo.ratio_height);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageInfo {
        Uri Original_URI;
        String Original_Image_Path;
        Uri Crop_Uri;
        String Crop_Image_Path;
    }

    private class Holder {
        ImageView ivAlbum;
    }

    private class GridAdapter extends ArrayAdapter<ImageInfo> {
        LayoutInflater inflater;
        int res;

        public GridAdapter(Context context, int res, List<ImageInfo> list) {
            super(context, res, list);
            this.res = res;
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder;
            View v;
            if(convertView == null) {
                v = inflater.inflate(this.res, parent, false);
                holder = new Holder();
                holder.ivAlbum = (ImageView)v.findViewById(R.id.ivImage);
                v.setTag(holder);
            } else {
                v = convertView;
                holder = (Holder)v.getTag();
            }
            final ImageInfo info = (ImageInfo)getItem(position);

            holder.ivAlbum.setScaleType(ImageView.ScaleType.FIT_CENTER);

            holder.ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alImageURL.remove(position);
                    doCropImage(info.Original_URI, albumInfo.ratio_width, albumInfo.ratio_height);
                }
            });

            holder.ivAlbum.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ImageInfo imgInfo = alImageURL.get(position);
                    for (PhotoInfo p : alPhotoList) {
                        if (p.uri == imgInfo.Original_URI) {
                            p.isCheck = false;
                        }
                    }
                    alImageURL.remove(position);
                    Toast.makeText(OrderGrid.this, "선택하신 사진을 제거하였습니다.", Toast.LENGTH_LONG).show();
                    gridAdapter.notifyDataSetChanged();

                    return true;
                }
            });
            holder.ivAlbum.setImageURI(info.Crop_Uri);
            //ImageLoader.getInstance().displayImage(info.Crop_Uri.toString(), (ImageView)holder.ivAlbum, options);

            return v;
        }
    }
}
