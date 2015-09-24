package kr.pnit.mPhoto.order;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.DTO.DecoIconInfo;
import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.DTO.FrameUris;
import kr.pnit.mPhoto.DTO.PhotoInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Dialog.ImageViewActivity;
import kr.pnit.mPhoto.Dialog.SendImageActivity;
import kr.pnit.mPhoto.PhotoSelector.PhotoSelectorActivity;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.Utils.DragShadowBuilder;
import kr.pnit.mPhoto.Utils.FileUtils;
import kr.pnit.mPhoto.Utils.ImageUtils;
import kr.pnit.mPhoto.Utils.UnitConverter;
import kr.pnit.mPhoto.albummaker.EmoticonFrame;
import kr.pnit.mPhoto.albummaker.PhotoMaker;
import kr.pnit.mPhoto.main.BaseActivity;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by macmini on 14. 11. 29..
 */
public class OrderAlbum extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    LayoutInflater inflater;
    // ActionBar Navigation
    ImageButton iBtnPre;
    ImageButton iBtnOrder;

    LinearLayout llIcon;            // 아이콘 리스트
    ViewFlipper vfPhotoFrame;       // Album Frame
    LinearLayout llPhoto;           // 사진 리스트

    // Page Navigation
    ImageButton btnPre;
    ImageButton btnNext;

    Button btnAutoSelect;           // Auto Select Button
    Button btnSelectPhoto;
    TextView tvPageCount;
    AlbumInfo albumInfo;


    boolean isIconDrag = false;

    private ImageLoaderConfiguration config;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    private ArrayList<FrameInfo> alFrameInfo;
    private ArrayList<FrameUris> alFrameUris;

    private ArrayList<PhotoInfo> alPhotoList;

    private ArrayList<DecoIconInfo> alDecoIconInfo;

    private ArrayList<String> alDecoIconsUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getActionBar().hide();
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        alFrameInfo = new ArrayList<FrameInfo>();
        alFrameUris = new ArrayList<FrameUris>();
        alDecoIconInfo = new ArrayList<DecoIconInfo>();
        alDecoIconsUrl = new ArrayList<String>();
        alPhotoList = new ArrayList<PhotoInfo>();

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

        // Get singleton instance of ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        setAlbumInfo();
        initLayout();
    }

    private void setAlbumInfo() {
        Intent intent = getIntent();
        albumInfo = (AlbumInfo) intent.getSerializableExtra("ITEM");
        Log.d(TAG, "Album Info :" + albumInfo.maxPage + " Ratio W:" + albumInfo.ratio_width + " H:" + albumInfo.ratio_height);
    }
    /**
     * Layout 설정
     */
    private void initLayout() {
        llPhoto = (LinearLayout) findViewById(R.id.llPhotoSelect);
        llIcon = (LinearLayout)findViewById(R.id.llFrameicon);

        btnSelectPhoto = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelectPhoto.setOnClickListener(this);

        btnAutoSelect = (Button)findViewById(R.id.btnAutoSelect);
        btnAutoSelect.setOnClickListener(this);

        if(albumInfo.type == AlbumInfo.TYPE_PHOTOICONFRAME){
            findViewById(R.id.llTemplete).setVisibility(View.GONE);
        } else {
            LinearLayout llFrameSelect = (LinearLayout) findViewById(R.id.llFrameSelect);
            for (int i = 0; i < llFrameSelect.getChildCount(); i++) {
                llFrameSelect.getChildAt(i).setOnClickListener(this);
            }
        }


        vfPhotoFrame = (ViewFlipper) findViewById(R.id.vfPhotoFrame);
        btnPre = (ImageButton) findViewById(R.id.btnVFPre);
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vfPhotoFrame.showPrevious();
                int count = vfPhotoFrame.getDisplayedChild() + 1;
                tvPageCount.setText(albumInfo.maxPage + "페이지중 " + count + "페이지");
            }
        });

        btnNext = (ImageButton) findViewById(R.id.btnVFNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveViewToImage(vfPhotoFrame.getDisplayedChild());
                vfPhotoFrame.showNext();
                int count = vfPhotoFrame.getDisplayedChild() + 1;
                tvPageCount.setText(albumInfo.maxPage + "페이지중 " + count + "페이지");

            }
        });

        tvPageCount = (TextView) findViewById(R.id.tvPageCount);

        iBtnOrder = (ImageButton) findViewById(R.id.btnOrder);
        iBtnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SaveViewToImage(vfPhotoFrame.getDisplayedChild())) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean isComplete = true;
                            for(int i = 0; i < albumInfo.maxPage; i++) {
                                if(alFrameInfo.get(i).isConvert == false) {
                                    isComplete = false;
                                    Toast.makeText(OrderAlbum.this, "사진을 넣지 않은 페이지(" + (i + 1) + ")가 있습니다.", Toast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                            if(isComplete) {
                                goToImageViewer();
                            }
                        }
                    }, 200);
                };
            }
        });
        iBtnPre = (ImageButton) findViewById(R.id.btnPre);
        iBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(albumInfo.edit_gubun == 2){
            // 프레임 선택 불가 상품
            findViewById(R.id.llTemplete).setVisibility(View.GONE);
        }

        prepareNetworking(Define.HTTP_DECO_ICON, "GET");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.btnTemp01:
                setFrameType(0);
                break;
            case R.id.btnTemp02:
                setFrameType(1);
                break;
            case R.id.btnTemp03:
                setFrameType(2);
                break;
            case R.id.btnTemp04:
                setFrameType(3);
                break;
            case R.id.btnTemp05:
                setFrameType(4);
                break;
        }
    }
    private void addDecoIcons() {
        for(int i =0; i < alDecoIconsUrl.size(); i++) {
            addDecoIcon(llIcon, alDecoIconsUrl.get(i));
        }
    }

    /**
     * 앨범 호출 하기
     */
    private void doTakeAlbumAction() {
        Log.i(TAG, "doTakeAlbumAction()");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), GET_IMAGE_FROM_GALLERY);
    }

    private void doTakePhotoFromSelector()
    {
        Log.i(TAG, "doTakePhotoFromSelector()");
        Intent intent = new Intent(this, PhotoSelectorActivity.class);
        startActivityForResult(intent, GET_IMAGE_FROM_SELECTOR);
    }

    Uri mImageCropUri;
    private int cropeScale = 2;
    private int cropWidth;
    private int cropHeight;

    private void doCropImage(Uri uri, String name , int width, int height) {

        String cropImageName = System.currentTimeMillis() +  "_PAGE" + vfPhotoFrame.getDisplayedChild() + "_" + name;

        mImageCropUri = FileUtils.createSaveCropFile(cropImageName);

        File original = ImageUtils.getImageFile(this, uri);
        File copy = new File(mImageCropUri.getPath());

        FileUtils.copyFile(original, copy);

        cropWidth = width * cropeScale;
        cropHeight = height * cropeScale;

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCropUri, "image/*");
        intent.putExtra("scale", true);
//        intent.putExtra("outputX", width * cropeScale); // crop한 이미지의 x축 크기
//        intent.putExtra("outputY", height * cropeScale); // crop한 이미지의 y축 크기
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("output", mImageCropUri);
        Log.d(TAG, "doCropImage W:" + (width * cropeScale) + " H:" + (height * cropeScale));

        startActivityForResult(intent, CROP_IMAGE);
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

    private final int GOTO_ORDERINPUT = 0x10;
    private final int GOTO_IMAGE_VIEWER = 0x12;
    private final int GOTO_SEND_IMAGE = 0x13;


    private final int GET_IMAGE_FROM_GALLERY = 0x20;
    private final int GET_IMAGE_FROM_SELECTOR= 0x21;


    private final int SAVE_IMAGE             = 0x30;
    private final int CROP_IMAGE             = 0x31;
    private final int GET_IMAGE_FROM_CAMERA  = 0x32;
    private final int SAVE_IMAGE_AND_VIEW    = 0x33;


    private View cropedView = null;

    public static final int REMOVE_CHILD_VIEW = 0x1;
    public static final int AUTO_SELECT_IMAGE = 0x2;

    private boolean isAutoSelectRunning = false;

    Handler actionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();
            switch(msg.what) {
                case REMOVE_CHILD_VIEW:
                    Log.d(TAG, "REMOVE CHILD VIEW");
                    EmoticonFrame v = (EmoticonFrame)msg.obj;
                    RelativeLayout iconLayout = (RelativeLayout)currFrame.findViewById(R.id.rlIcon);
                    iconLayout.removeView(v);
                    break;
                case AUTO_SELECT_IMAGE:
                    Log.d(TAG, "Auto Select Image");
                    FrameInfo frameInfo = alFrameInfo.get(vfPhotoFrame.getDisplayedChild());
                    FrameUris frameUris = alFrameUris.get(vfPhotoFrame.getDisplayedChild());
                    int width = albumInfo.ratio_width * scale;
                    int height = albumInfo.ratio_height * scale;
                    // Photo Frame 설정 : type 에 따라서 Child View 를 숨김
                    LinearLayout llFrame = (LinearLayout) currFrame.findViewById(R.id.llFrame);

                    //for (int i = 0; i < llFrame.getChildCount(); i++) {
                        //LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
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
                            return;
                        } else {
                            Log.d(TAG, "Auto Select Image :" + uri);
                        }
                        boolean isCropPass = false;
                        switch (frameInfo.type) {
                            case 0:
                                if (frameInfo.images[0] == "") {
                                    frameUris.uris[0] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = false;
                                } else {
                                    isCropPass = true;
                                }
                                break;
                            case 1:
                                if (frameInfo.images[0] == "") {
                                    frameUris.uris[0] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[2] == "") {
                                    frameUris.uris[2] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = false;
                                } else {
                                    isCropPass = true;
                                    isAutoSelectRunning = false;
                                }

                                break;
                            case 2:
                                if (frameInfo.images[0] == "") {
                                    frameUris.uris[0] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[1] == "") {
                                    frameUris.uris[1] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(1);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 1);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[2] == "") {
                                    frameUris.uris[2] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = false;
                                } else {
                                    isCropPass = true;
                                    isAutoSelectRunning = false;
                                }

                                break;
                            case 3:
                                if (frameInfo.images[0] == "") {
                                    frameUris.uris[0] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[2] == "") {
                                    frameUris.uris[2] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[3] == "") {
                                    frameUris.uris[3] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(1);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 1);
                                    isAutoSelectRunning = false;
                                } else {
                                    isCropPass = true;
                                    isAutoSelectRunning = false;
                                }


                                break;
                            case 4:
                            default:
                                if (frameInfo.images[0] == "") {
                                    frameUris.uris[0] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[1] == "") {
                                    frameUris.uris[1] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(0);
                                    cropedView = llSub.getChildAt(1);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 0);
                                    cropedView.setTag(R.string.tag_cal, 1);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[2] == "") {
                                    frameUris.uris[2] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(0);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 0);
                                    isAutoSelectRunning = true;
                                }else if (frameInfo.images[3] == "") {
                                    frameUris.uris[3] = uri;
                                    LinearLayout llSub = (LinearLayout) llFrame.getChildAt(1);
                                    cropedView = llSub.getChildAt(1);
                                    cropedView.setTag(R.string.tag_res_id, uri);
                                    cropedView.setTag(R.string.tag_row, 1);
                                    cropedView.setTag(R.string.tag_cal, 1);
                                    isAutoSelectRunning = false;
                                } else {
                                    isCropPass = true;
                                    isAutoSelectRunning = false;
                                }
                                break;
                        }
                        if(!isCropPass){
                            String name = cropedView.getTag(R.string.tag_row) + "_" + cropedView.getTag(R.string.tag_cal);
                            Integer row = (Integer)cropedView.getTag(R.string.tag_row);
                            Integer cal = (Integer)cropedView.getTag(R.string.tag_cal);
                            doCropImage(uri, name ,
                                    cropedView.getMeasuredWidth(), cropedView.getMeasuredHeight());
                        }

                    break;
                default:
                    break;
                }

            }
        //}
    };

    private void goToImageViewer() {
        Intent intent = new Intent(this, ImageViewActivity.class);

        intent.putExtra("AlbumTitle", albumInfo.title);
        intent.putExtra("AlbumCode", albumInfo.code);
        intent.putExtra("AlbumSubCode", albumInfo.sub_code);

        intent.putExtra("inwha_yn", albumInfo.inwha_yn);

        intent.putExtra("AlbumDeliverNum", ""+albumInfo.deliver_num);

        intent.putExtra("AlbumPrice", albumInfo.price);
        intent.putExtra("ALFrameInfo", alFrameInfo);

        startActivityForResult(intent, GOTO_IMAGE_VIEWER);
    }
    private boolean checkImageSize(int width, int height, Uri uri){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), options);
        Log.d(TAG, "checkImageSize " + uri.getPath() + " W:" + options.outWidth + " H:" + options.outHeight);
        if((width > options.outWidth) | (height > options.outHeight)) {
            return false;
        }else{
            cropHeight = options.outHeight;
            cropWidth = options.outWidth;
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GOTO_IMAGE_VIEWER) {
            if (resultCode == RESULT_OK) {
                // Order Complete
                finish();
            } else {
                // Order Canceled!!!
            }
        } else if (requestCode == GOTO_ORDERINPUT) {
            if (resultCode == RESULT_OK) {
                // Order Complete
                finish();
            } else {
                // Order Canceled!!!
            }
        } else if (requestCode == GET_IMAGE_FROM_GALLERY) {
            Log.d(TAG, "GET_IMAGE_FROM_GALLERY");

            if (resultCode == Activity.RESULT_OK) {
                ClipData clip = data.getClipData();
                if (clip == null) {
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
                        if (uri.toString().startsWith("content://media"))
                            addPhotoIcon(llPhoto, uri);
                        else
                            Log.d(TAG, "FILE PATH:" + uri.toString());
                    }
                }
                //Do something with the uris array
            }
        } else if(requestCode == GET_IMAGE_FROM_SELECTOR){
                Log.d(TAG, "GET_IMAGE_FROM_SELECTOR");
                if(resultCode == RESULT_OK){
                    String[] uris = data.getStringArrayExtra("URIS");
                    Log.d(TAG, "GET URIS " + uris.length);
                    for(int i = 0; i < uris.length; i++) {
                        Uri uri = Uri.parse(uris[i]);
                        addPhotoIcon(llPhoto, uri);
                    }
                }

        } else if (requestCode == CROP_IMAGE) {
            Log.w(TAG, "CROP_IMAGE");
            if (resultCode == RESULT_OK) {
                // Crop 된 이미지를 넘겨 받습니다.
                Log.w(TAG, "mImageCropUri = " + mImageCropUri.getPath());

                FrameInfo frameInfo = alFrameInfo.get(vfPhotoFrame.getDisplayedChild());

                Integer row = (Integer)cropedView.getTag(R.string.tag_row);
                Integer cal = (Integer)cropedView.getTag(R.string.tag_cal);

                // CHECK IMAGE SIZE
                if((albumInfo.resolution_H != 0) & (albumInfo.resolution_W != 0)){
                    int res_w = albumInfo.resolution_W;
                    int res_h = albumInfo.resolution_H;

                    switch(frameInfo.type){
                        case 0:
                            // original
                            break;
                        case 1:
                            res_h = res_h / 2;
                            break;
                        case 2:
                            res_h = res_h / 2;
                            if(row == 0)
                                res_w = res_w/2;
                            break;
                        case 3:
                            res_h = res_h / 2;
                            if(row == 1)
                                res_w = res_w/2;

                            break;
                        case 4:
                        default:
                            res_h = res_h / 2;
                            res_w = res_w / 2;
                            break;

                    }
                    Log.w(TAG, "Image Resolution W: " + res_w + " H :" + res_h);
                    if(!checkImageSize(res_w, res_h, mImageCropUri)){
                        Toast.makeText(this, "이미지가 작습니다. 제품의 품질이 저하될 수 있으니 큰 사진으로 교체하거나 사진을 크게 다시 잘라주세요.", Toast.LENGTH_LONG).show();
                        isAutoSelectRunning = false;
                    }

                } else {
                    Log.w(TAG, "Image Resolution(CAL) W: " + (100 * albumInfo.ratio_width) + " H :" + (100 * albumInfo.ratio_height));
                    if(!checkImageSize(100 * albumInfo.ratio_width,
                            100 * albumInfo.ratio_height, mImageCropUri)){
                        Toast.makeText(this, "이미지가 작습니다. 제품의 품질이 저하될 수 있으니 큰 사진으로 교체하거나 사진을 크게 다시 잘라주세요.", Toast.LENGTH_LONG).show();
                        isAutoSelectRunning = false;
                    }
                }


                String full_path = mImageCropUri.getPath();

                Log.w(TAG, "비트맵 Image path = " + full_path);
                if((row != null) && (cal != null)) {
                    int index = row* 2   + cal;
                    frameInfo.images[index] = full_path;
                    frameInfo.frame_width = cropWidth;
                    frameInfo.frame_height = cropHeight;
                    Log.d(TAG, "Insert Photo Path " + index + " " + full_path);
                }

                if (cropedView != null) {
                    cropedView.setTag(R.string.tag_image_path, full_path);
                    cropedView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = (Uri) v.getTag(R.string.tag_res_id);
                            cropedView = v;
                            String name = cropedView.getTag(R.string.tag_row) + "_" + cropedView.getTag(R.string.tag_cal);
                            if(!isVisibleDecoIcon())
                                doCropImage(uri, name , v.getMeasuredWidth(), v.getMeasuredHeight());
                        }
                    });
                    ImageLoader.getInstance().displayImage(mImageCropUri.toString(), (ImageView) cropedView, options);
                }
                checkUsedPhoto();
                if(isAutoSelectRunning) {
                    Message msg = new Message();
                    msg.what = OrderAlbum.AUTO_SELECT_IMAGE;
                    msg.obj = this;
                    //actionHandler.sendMessage(msg);
                    actionHandler.sendMessageDelayed(msg, 500);
                }
            } else {
                isAutoSelectRunning = false;
            }

        } else if(requestCode == SAVE_IMAGE) {
            int pageNum = data.getIntExtra("PAGE", -1);
            String path = data.getStringExtra("FILE");
            Toast.makeText(this, (pageNum + 1) + "페이지를 저장하였습니다.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Save Image : " + pageNum + " Path:" + path);
            if(pageNum > -1) {
                alFrameInfo.get(pageNum).pageImage = path;
                alFrameInfo.get(pageNum).isConvert = true;
            }

        }
    }
    private boolean isVisibleDecoIcon() {
        FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();
        RelativeLayout iconLayout = (RelativeLayout)currFrame.findViewById(R.id.rlIcon);
        boolean result = false;
        for(int i = 0; i < iconLayout.getChildCount(); i++){
            EmoticonFrame eView = (EmoticonFrame)iconLayout.getChildAt(i);
            if(eView.getControlVisible() == View.VISIBLE) {
                eView.setControlVisible(View.INVISIBLE);
                result =  true;
            }
        }
        return result;
    }
    private void setDecoIconInfo() {
        FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();
        RelativeLayout iconLayout = (RelativeLayout)currFrame.findViewById(R.id.rlIcon);
        alDecoIconInfo.clear();

        for(int i = 0; i < iconLayout.getChildCount(); i++ ){
            DecoIconInfo deco = new DecoIconInfo();
            EmoticonFrame emoticonFrame = (EmoticonFrame)iconLayout.getChildAt(i);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)emoticonFrame.getLayoutParams();

            deco.bIcon = ((BitmapDrawable)(emoticonFrame.getEmoticonView().getDrawable())).getBitmap();

            deco.ImageWidth = emoticonFrame.getEmoticonView().getWidth();
            deco.ImageHeight = emoticonFrame.getEmoticonView().getHeight();

            deco.PointX =  params.leftMargin + (emoticonFrame.getWidth() - deco.ImageWidth)/2;
            deco.PointY =  params.topMargin + (emoticonFrame.getHeight() - deco.ImageHeight)/2;


            Log.d(TAG, "Image Res :" + deco.ImageUri + " L:" + params.leftMargin + " T:" + params.topMargin + " Unit:" + (int)UnitConverter.convertDpPixel(OrderAlbum.this, 32) );
            Log.d(TAG, "Image Frame W:" + emoticonFrame.getWidth() + " IMG W:" + deco.ImageWidth +" T:" + emoticonFrame.getPaddingTop() + " L:" + emoticonFrame.getPaddingLeft());
            Log.d(TAG, "Image Res :" + deco.ImageUri + " W:" + deco.ImageWidth + " H:" + deco.ImageHeight + " X:" + deco.PointX + " Y:" + deco.PointY);
            alDecoIconInfo.add(deco);
        }
    }

    FrameInfo frameInfo;
    ProgressDialog photoMakerdialog;
    Thread tSave = null;
    private boolean showSaveImageDialog(final int pageNum) {
        frameInfo = alFrameInfo.get(pageNum);
        if((frameInfo.images[0] == "") &&
           (frameInfo.images[1] == "") &&
           (frameInfo.images[2] == "") &&
           (frameInfo.images[3] == "")) {
            Toast.makeText(OrderAlbum.this, "사진을 넣지 않은 페이지가 있습니다." , Toast.LENGTH_LONG).show();
            return false;
        } else {
            setDecoIconInfo();
            Log.d(TAG, "Frame Info w:" + frameInfo.view_width + " h:" + frameInfo.view_height);
            for(int i = 0 ; i < 4; i++)
                Log.d(TAG, "Photo URL : " + frameInfo.images[i] + " w:" + frameInfo.frame_width + " h:" + frameInfo.frame_height);

            Thread tSave = new Thread(new Runnable() {
                @Override
                public void run() {
                    PhotoMaker maker = new PhotoMaker(OrderAlbum.this, albumInfo, frameInfo, alDecoIconInfo);
                    if(maker.startPhotoMaker()) {
                        Log.d(TAG, "Save Image : " + pageNum + " Path:" + maker.getSaveFilePath());
                        if(pageNum > -1) {
                            alFrameInfo.get(pageNum).pageImage = maker.getSaveFilePath();
                            alFrameInfo.get(pageNum).isConvert = true;
                        }
                    }
                    photoMakerdialog.dismiss();
                }
            });
            photoMakerdialog = ProgressDialog.show(OrderAlbum.this, "", "사진 저장 중입니다.", true);
            tSave.start();
            //Toast.makeText(OrderAlbum.this, "사진 저장 중입니다." , Toast.LENGTH_LONG).show();
            photoMakerdialog.show();

        }
        return true;
    }
    private boolean SaveViewToImage(int pageNum) {
        return showSaveImageDialog(pageNum);
    }


    /**
     * Photo Frame 비율에 따른 화면 스케일 설정 메소드
     *
     * @return
     */
    private int getScale() {
        float scale = 1;
        if(albumInfo.ratio_height > 1000 ) {
            albumInfo.ratio_height  /= 1000;
            albumInfo.ratio_width /= 1000;
        } else if(albumInfo.ratio_height > 100) {
            albumInfo.ratio_height  /= 100;
            albumInfo.ratio_width /= 100;
        }
        Log.d(TAG, "Flipper Width:" + vfPhotoFrame.getMeasuredWidth() + " Hight:" + vfPhotoFrame.getMeasuredHeight());
        scale = vfPhotoFrame.getMeasuredHeight() / (albumInfo.ratio_height);
        while (true) {
            if (scale * (albumInfo.ratio_width) < vfPhotoFrame.getMeasuredWidth()) break;
            scale--;
            if (scale < 0) return -1;
        }
        Log.d(TAG, "Set Scale :" + scale);

        if(vfPhotoFrame.getMeasuredWidth() < 800) {
            cropeScale = 3;
        } else {
            cropeScale = 2;
        }

        return (int)scale;
    }

    private void setDragListener(boolean isMakeFrame){
        FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();

        RelativeLayout iconLayout = (RelativeLayout)currFrame.findViewById(R.id.rlIcon);
        LinearLayout llFrame = (LinearLayout) currFrame.findViewById(R.id.llFrame);

        if(isMakeFrame){
            iconLayout.setOnDragListener(null);
            for (int i = 0; i < llFrame.getChildCount(); i++) {
                LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
                for(int j = 0; j < llSub.getChildCount(); j++ ) {
                    ImageView iv = (ImageView)llSub.getChildAt(j);
                    iv.setOnDragListener(onDragListener);
                }
            }
        } else {
            iconLayout.setOnDragListener(onIconDragListener);
            for (int i = 0; i < llFrame.getChildCount(); i++) {
                LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
                for(int j = 0; j < llSub.getChildCount(); j++ ) {
                    ImageView iv = (ImageView)llSub.getChildAt(j);
                    iv.setOnDragListener(null);
                }
            }
        }
    }

    private void setFrameType(int type) {
        FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();
        FrameInfo frameInfo = alFrameInfo.get(vfPhotoFrame.getDisplayedChild());
        FrameUris frameUris = alFrameUris.get(vfPhotoFrame.getDisplayedChild());

        int width = albumInfo.ratio_width * scale;
        int height = albumInfo.ratio_height * scale;

        if(frameInfo == null) {
            Log.d(TAG, "FrameInfo Load Error " + vfPhotoFrame.getDisplayedChild());
        }

        for(int i = 0; i < 4; i++) {
            frameInfo.images[i] = "";
            frameUris.uris[i] = null;
        }

        checkUsedPhoto();

        frameInfo.type = type;

        frameInfo.view_width = width;
        frameInfo.view_height = height;

        frameInfo.frame_height = height;
        frameInfo.frame_width = width;

        Log.d(TAG, "frameInfo Layout params :" + width + " " + height);
        // Photo Frame 설정 : type 에 따라서 Child View 를 숨김
        LinearLayout llFrame = (LinearLayout) currFrame.findViewById(R.id.llFrame);
        for (int i = 0; i < llFrame.getChildCount(); i++) {
            LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
            llSub.removeAllViews();
        }
        for (int i = 0; i < llFrame.getChildCount(); i++) {
            LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
            switch (type) {
                case 0:
                    if (i == 0) {
                        llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                        setSubFrame(llSub, width, height, 0, 0);
                    }
                    break;
                case 1:
                    llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height / 2));
                    if (i == 0) {
                        setSubFrame(llSub, width, height/2, 0, 0);
                    } else {
                        setSubFrame(llSub, width, height/2, 1, 0);
                    }
                    break;
                case 2:
                    llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height / 2));
                    if (i == 0) {
                        setSubFrame(llSub, width/2, height/2, 0, 0);
                        setSubFrame(llSub, width/2, height/2, 0, 1);
                    } else {
                        setSubFrame(llSub, width, height/2, 1, 0);
                    }
                    break;
                case 3:
                    llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height / 2));
                    if (i == 0) {
                        setSubFrame(llSub, width, height/2, 0, 0);
                    } else {
                        setSubFrame(llSub, width/2, height/2, 1, 0);
                        setSubFrame(llSub, width/2, height/2, 1, 1);
                    }
                    break;
                case 4:
                default:
                    llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height / 2));
                    if (i == 0) {
                        setSubFrame(llSub, width/2, height/2, 0, 0);
                        setSubFrame(llSub, width/2, height/2, 0, 1);
                    } else {
                        setSubFrame(llSub, width/2, height/2, 1, 0);
                        setSubFrame(llSub, width/2, height/2, 1, 1);
                    }
                    break;
            }
        }
    }

    private void setSubFrame(LinearLayout llLayout, int width, int height, int row_num, int cal_num) {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        iv.setBackgroundResource(R.drawable.frame_background);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setOnDragListener(onDragListener);
        iv.setTag(R.string.tag_row, row_num);
        iv.setTag(R.string.tag_cal, cal_num);
        llLayout.addView(iv);
    }

    int scale;

    /**
     * Photo Frame  추가 메소드
     *
     * @param num Page Number
     * @return
     */
    private boolean addPhotoFrame(int num, FrameInfo frameInfo) {

        scale = getScale();

        if (scale == -1) {
            Log.d(TAG, "Photo FrameLayout Size Error!!!");
            return false;
        }

        // Photo Frame 크기 설정 : Scale 에 맞춰서 크기 설정함.
        FrameLayout flFrame = (FrameLayout) inflater.inflate(R.layout.layout_frame_main, null);

        Log.d(TAG, "Set Size : " + (int) (albumInfo.ratio_width * scale) + " "
                + (int) (albumInfo.ratio_height * scale));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                albumInfo.ratio_width * scale, albumInfo.ratio_height * scale);

        flFrame.setLayoutParams(params);

        // Photo Frame 설정 : type 에 따라서 Child View 를 숨김
        LinearLayout llFrame = (LinearLayout) flFrame.findViewById(R.id.llFrame);

        int width = albumInfo.ratio_width * scale;
        int height = albumInfo.ratio_height * scale;

        if(frameInfo == null) {
            Log.d(TAG, "FrameInfo Load Error " + vfPhotoFrame.getDisplayedChild());
        }

        frameInfo.view_width = width;
        frameInfo.view_height = height;

        frameInfo.frame_height = height;
        frameInfo.frame_width = width;

        Log.d(TAG, "addPhotoFrame Layout params :" + frameInfo.view_width + " " + frameInfo.view_height);

        for (int i = 0; i < llFrame.getChildCount(); i++) {
            LinearLayout llSub = (LinearLayout) llFrame.getChildAt(i);
            llSub.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            setSubFrame(llSub, width, height, 0, 0);
        }

        // Photo Frame Tag 추가
        flFrame.setTag(R.string.tag_page_id, num);

        // Photo Frame 추가
        vfPhotoFrame.addView(flFrame);

        // 가운데로 정렬하기 위한 마진 추가
        int margin_width = vfPhotoFrame.getMeasuredWidth() - (albumInfo.ratio_width * scale);
        int margin_heigh = vfPhotoFrame.getMeasuredHeight() - (albumInfo.ratio_height * scale);
        Log.d(TAG, "Set Margin : " + margin_width + " " + margin_heigh);
        vfPhotoFrame.setPadding(margin_width / 2, margin_heigh / 2, margin_width / 2, margin_heigh / 2);

        return true;
    }
    private void checkUsedPhoto(){

        for(int i = 0; i <  llPhoto.getChildCount(); i++) {
            View v = llPhoto.getChildAt(i);
            PhotoInfo pInfo = alPhotoList.get(i);
            boolean isCheck = false;
            for(FrameUris f : alFrameUris) {
                for(int j = 0; j < 4; j++){
                    if(f.uris[j] == pInfo.uri){
                        Log.d(TAG, "Photo Images [" + i + "] " + pInfo.uri);
                        Log.d(TAG, "Photo URI [" + j + "] " + f.uris[j]);
                        isCheck = true;
                        break;
                    }
                }

            }
            if(isCheck){
                v.findViewById(R.id.imgCheck).setVisibility(View.VISIBLE);
                pInfo.isCheck = true;
            } else {
                v.findViewById(R.id.imgCheck).setVisibility(View.INVISIBLE);
                pInfo.isCheck = false;
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
            int iconSize = (int) (46 * d);
            Log.d(TAG, "addPhotoIcon " + iconSize);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    iconSize, iconSize
            );
            params.setMargins(5, 0, 5, 0);
            llLayout.setLayoutParams(params);

            btn.setClickable(true);
            btn.setOnLongClickListener(onLongClickListener);
            btn.setImageBitmap(bmp);
            btn.setTag(R.string.tag_res_id, uri);
            btn.setScaleType(ImageView.ScaleType.FIT_XY);

            main.addView(llLayout);
            PhotoInfo pInfo = new PhotoInfo();

            pInfo.uri = uri;
            pInfo.isCheck = false;

            alPhotoList.add(pInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return main;
    }

    private LinearLayout addDecoIcon(LinearLayout main, String uri) {
        //Log.d(TAG, "addDecoIcon :" + uri);

        try {
            ImageView btn = new ImageView(this);

            float d = getResources().getDisplayMetrics().density;
            int iconSize = (int) (50 * d);
            //Log.d(TAG, "addDecoIcon " + iconSize);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    iconSize, iconSize
            );
            params.setMargins(5, 0, 5, 0);
            btn.setClickable(true);
            btn.setOnLongClickListener(onLongIconClickListener);
            ImageLoader.getInstance().displayImage(uri, btn, options);
            btn.setTag(R.string.tag_res_id, uri);
            btn.setScaleType(ImageView.ScaleType.FIT_XY);
            btn.setLayoutParams(params);

            main.addView(btn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return main;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
        }
    };
    View.OnLongClickListener onLongIconClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            //Log.d(TAG, "onDragListener ACTION_DOWN");
            setDragListener(false);
            DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
            return true;
        }
    };
    View.OnTouchListener onIconTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Log.d(TAG, "onDragListener ACTION_DOWN");
                    setDragListener(false);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(null, shadowBuilder, v, 0);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return false;
        }
    };


    View.OnDragListener onIconDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {

            View view = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_STARTED");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_ENTERED");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_EXITED");
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_LOCATION");
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "onIconDragListener ACTION_DROP : " + event.getX() + " " + event.getY());

                    FrameLayout currFrame = (FrameLayout) vfPhotoFrame.getCurrentView();
                    RelativeLayout iconLayout = (RelativeLayout)currFrame.findViewById(R.id.rlIcon);

                    String uri = (String) view.getTag(R.string.tag_res_id);

                    final EmoticonFrame emoticonImageView = new EmoticonFrame(OrderAlbum.this);
                    ImageLoader.getInstance().displayImage(uri, emoticonImageView.getEmoticonView() , options);
                    emoticonImageView.setTag(R.string.tag_image_res, uri);
//                    emoticonImageView.setX(event.getX() - UnitConverter.convertDpPixel(OrderAlbum.this, 64));
//                    emoticonImageView.setY(event.getY() - UnitConverter.convertDpPixel(OrderAlbum.this, 64));
                    emoticonImageView.setHandler(actionHandler);

                    iconLayout.addView(emoticonImageView);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    boolean dropped = event.getResult();
                    break;
                default:
                    return false;
            }
            return true;
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            //Log.d(TAG, "onDragListener ACTION_DOWN");
            setDragListener(true);
            //View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
            return true;
        }
    };

    View.OnDragListener onDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {

            View view = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_STARTED");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_ENTERED");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_EXITED");
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    //Log.d(TAG, "onDragListener ACTION_DRAG_LOCATION");
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "onDragListener ACTION_DROP : " + v.toString() + " " + view.toString());
                    Uri uri = (Uri) view.getTag(R.string.tag_res_id);
                    cropedView = v;
                    cropedView.setTag(R.string.tag_res_id, uri);

                    String name = cropedView.getTag(R.string.tag_row) + "_" + cropedView.getTag(R.string.tag_cal);

                    Integer row = (Integer)cropedView.getTag(R.string.tag_row);
                    Integer cal = (Integer)cropedView.getTag(R.string.tag_cal);

                    FrameUris frameUris = alFrameUris.get(vfPhotoFrame.getDisplayedChild());

                    if((row != null) && (cal != null)) {
                        int index = row * 2 + cal;
                        frameUris.uris[index] = uri;
                        Log.d(TAG, "Insert Photo Path " + index + " " + uri);
                    }

                    doCropImage(uri, name , v.getMeasuredWidth(), v.getMeasuredHeight());

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    boolean dropped = event.getResult();
                    //Log.d(TAG, "onDragListener ACTION_DRAG_ENDED : "  + (dropped?"Dropped!":"Not Drop"));
                    break;
                default:
                    return false;
            }
            return true;
        }
    };
    boolean isFrameAdded = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (albumInfo.type == AlbumInfo.TYPE_PHOTOFRAME) {
            if (!isFrameAdded) {

                for (int i = 0; i < albumInfo.maxPage; i++) {
                    FrameInfo frameInfo = new FrameInfo();
                    frameInfo.ratio_width = albumInfo.ratio_width;
                    frameInfo.ratio_height = albumInfo.ratio_height;
                    frameInfo.type = 0;
                    frameInfo.pageNum = i;
                    addPhotoFrame(i, frameInfo);
                    alFrameInfo.add(frameInfo);
                    alFrameUris.add(new FrameUris());
                }
                tvPageCount.setText(albumInfo.maxPage + "페이지중 1페이지");
                isFrameAdded = true;
            }
        }else if(albumInfo.type == AlbumInfo.TYPE_PHOTOICONFRAME) {
            if (!isFrameAdded) {
                    FrameInfo frameInfo = new FrameInfo();
                    frameInfo.ratio_width = albumInfo.ratio_width;
                    frameInfo.ratio_height = albumInfo.ratio_height;
                    frameInfo.type = 0;
                    frameInfo.pageNum = 0;
                    addPhotoFrame(0, frameInfo);
                    alFrameInfo.add(frameInfo);
                    alFrameUris.add(new FrameUris());

                tvPageCount.setText("사진 1장 짜리 상품입니다.");
                isFrameAdded = true;
            }
        }
    }

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
    private void parseJson(String json) {
        try{
            JSONObject j = new JSONObject(json);
            JSONArray jArray = j.getJSONArray("product");
            for(int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                String url = jObject.getString("icon_img_url");
                alDecoIconsUrl.add(url);
            }
        } catch(JSONException je) {

        }
        addDecoIcons();
    }
    @Override
    public void makeParams() {
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
