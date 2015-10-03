package kr.pnit.mPhoto.PhotoSelector;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import kr.pnit.mPhoto.R;


import java.io.File;
import java.util.ArrayList;


public class PhotoSelectorActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> , View.OnClickListener{
    private final String TAG = "PhotoSelectorActivity";
    private static final int URL_LOADER = 0;

    Button btnOk;
    Button btnCancel;

    TextView tvSelectedPhotoCount;
    TextView tvSelectedPhotoInfo;

    GridView gvPhoto;
    GalleryAdapter galleryAdapter;
    String currPath = "";

    ArrayList<ImageInfo> alImageInfo;
    ArrayList<ImageInfo> alSelectedImageInfo;
    ArrayList<String> alImageFolder;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GalleryAdapter.HANDLER_UPDATE:
                    int count = 0;
                    for(int i = 0; i < alSelectedImageInfo.size(); i++){
                        if(alSelectedImageInfo.get(i).isSelect) {
                            count++;
                        }
                    }
                    tvSelectedPhotoCount.setText("선택된 사진 : " + count + "장");
                    break;
                case GalleryAdapter.HANDLER_MOVE:
                    String external = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String folder = (String)msg.obj;
                    if(folder.equals("../")){
                        if(currPath.substring(external.length()).lastIndexOf("/") > 0){
                            String target = currPath.substring(external.length(), currPath.lastIndexOf("/"));
                            //Log.d(TAG, "Handler : " + target);

                            while(target.startsWith("/"))
                                target = target.substring(1);

                            //Log.d(TAG, "Handler : " + external + "/"+ target);

                            getImageFileList( external + "/"+ target);
                        } else {
                            getImageFileList(external);
                        }

                    } else {
                        while(folder.startsWith("/"))
                            folder = folder.substring(1);
                        getImageFileList( external + "/"+ folder);
                    }
                    tvSelectedPhotoCount.setText("선택된 사진 : 0장");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selector_main);
        getLoaderManager().initLoader(URL_LOADER, null, this);

        alImageInfo = new ArrayList<ImageInfo>();
        alSelectedImageInfo = new ArrayList<ImageInfo>();
        alImageFolder = new ArrayList<String>();
        getActionBar().hide();
        initLayout();
    }

    private void getImageFileList(String path) {
        String external = Environment.getExternalStorageDirectory().getAbsolutePath();
        //Log.d(TAG, "External SD Folder : " + external + " " + path);
        currPath = path;
        File folder = new File(path);
        alSelectedImageInfo.clear();
        String[] fileList = folder.list();
        //Log.d(TAG, "File List :" + fileList.length);
        //Log.d(TAG, "Image Folder :" + alImageFolder.size());

        if(!path.equals(external)){
            ImageInfo ifo = new ImageInfo();
            ifo.type = ImageInfo.TYPE_TITLE;
            ifo.path = "../";
            ifo.folder = "../";
            //Log.d(TAG, "Loading :" + ifo.folder);
            alSelectedImageInfo.add(ifo);
        }

        for(int i = 0; i < fileList.length; i ++) {
            File f = new File(fileList[i]);
            //if(f.isFile())
            {
                if(fileList[i].toLowerCase().endsWith(".png") |
                        fileList[i].toLowerCase().endsWith(".jpg")|
                        fileList[i].toLowerCase().endsWith(".jpeg")) {

                    //Log.d(TAG, "Loading :" + path + " "+ fileList[i]);
                    ImageInfo ifo = new ImageInfo();
                    ifo.type = ImageInfo.TYPE_IMAGE;
                    ifo.path =  path + "/" +fileList[i];

                    if(path.equals(external))
                        ifo.folder = external;
                    else
                        ifo.folder = ifo.path.substring((external.length() + 1) , ifo.path.lastIndexOf("/"));

                    //Log.d(TAG, "Loading :" + ifo.path + " " +  ifo.folder);
                    alSelectedImageInfo.add(ifo);
                } else {
                    for(String str : alImageFolder) {
                        if(str.contains(fileList[i])){
                            //Log.d(TAG, "Check Folder :" + str + " "  + fileList[i]);
                            ImageInfo ifo = new ImageInfo();
                            ifo.type = ImageInfo.TYPE_TITLE;
                            ifo.path =  path + "/" +fileList[i];
                            ifo.folder = ifo.path.substring((external.length() + 1));
                            //Log.d(TAG, "Loading :" + ifo.path + " " +  ifo.folder);
                            alSelectedImageInfo.add(ifo);
                            break;
                        }
                    }
                }
            }
        }
        //Log.d(TAG, "Loading :" + alSelectedImageInfo.size());
        galleryAdapter.notifyDataSetChanged();
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String external = Environment.getExternalStorageDirectory().getAbsolutePath();
        //Log.d(TAG, "External SD Folder : " + external);
        if(data != null && data.moveToFirst()) {
            String thumbsData;
            int thumbsDataCol = data.getColumnIndex(MediaStore.Files.FileColumns.DATA);

            int num = 0;
            do {
                thumbsData = data.getString(thumbsDataCol);
                ImageInfo info = new ImageInfo();
                info.type = ImageInfo.TYPE_IMAGE;
                info.path = thumbsData;
                info.folder = thumbsData.substring(external.length(), thumbsData.lastIndexOf("/"));
                //Log.d(TAG, "Loading :" + thumbsData);

                boolean isContain = true;
                for(String folder : alImageFolder) {
                    if(folder.equals(info.folder)){
                        isContain = false;
                        break;
                    }
                }
                // Save Image Folder
                if(isContain){
                    //Log.d(TAG, "Add Folder : " + info.folder);
                    String folder = info.folder;
                    alImageFolder.add(folder);
                }

                // Save Image List
                alImageInfo.add(info);
            }while(data.moveToNext());
        }

        getImageFileList(external);

        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Log.d(TAG, "onCreateLoader ");
        Uri queryUri = MediaStore.Files.getContentUri("external");

        String[] projection = { MediaStore.Files.FileColumns._ID,
                                MediaStore.Files.FileColumns.DATA} ;
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ;
        String[] selectionArgs = null;
        // CursorLoader 를 생성해서 넘겨줍니다.
        return new CursorLoader(this, queryUri, projection, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
    }

    private void initLayout() {

        tvSelectedPhotoCount = (TextView)findViewById(R.id.tvSelectedPhotoCount);
        tvSelectedPhotoInfo = (TextView)findViewById(R.id.tvSelectedPhotoInfo);

        btnOk  = (Button)findViewById(R.id.btnOk);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        gvPhoto = (GridView)findViewById(R.id.gvPhoto);
        galleryAdapter = new GalleryAdapter(this, R.layout.grid_item, alSelectedImageInfo);
        galleryAdapter.setHandler(mHandler);
        gvPhoto.setAdapter(galleryAdapter);
    }

    public void ReturnList(){
        ArrayList<String> alList = new ArrayList<String>();
        for(int i = 0; i < alSelectedImageInfo.size(); i++){
            if(alSelectedImageInfo.get(i).isSelect) {
                if(alSelectedImageInfo.get(i).uri.toString() != null)
                    alList.add(alSelectedImageInfo.get(i).uri.toString());
            }
        }
        //Log.d(TAG, "Selected Item : " + alList.size());
        Intent intent = getIntent();
        intent.putExtra("URIS", alList.toArray(new String[alList.size()]));
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnOk:
                ReturnList();
                break;
            case R.id.btnCancel:
                onBackPressed();
                break;
        }
    }
}
