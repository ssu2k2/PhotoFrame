/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.pnit.mPhoto.Dialog;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.main.BaseActivity;
import kr.pnit.mPhoto.order.OrderInfoInput;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 * 
 * Julia Zudikova
 */

public class ImageViewActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

	private ViewPager mViewPager;
	private MenuItem menuLockItem;

    ImageButton iBtnPre;
    ImageButton iBtnOrder;

    ArrayList<FrameInfo> alFrameList;
    SamplePagerAdapter pageAdapter;
    String albumTitle;
    String albumCode;
    String albumSubCode;
    String albumDeliverNum;
    int albumPrice;
    int albumType;
    String agent_name;
    String user_name;
    String inwha_yn;
    ArrayList<Uri> alImageUri;
    ArrayList<String> alImagePath;
    private class MultiMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
        ArrayList<String> alList;
        int count;
        MediaScannerConnection mediaScannerConnection;
        boolean isScanning = false;
        public MultiMediaScanner(Context context , ArrayList<String> list){
            this.alList = list;
            count = 0;
            mediaScannerConnection = new MediaScannerConnection(context, this);
        }
        public void startScan(){
            isScanning = true;
            mediaScannerConnection.connect();
        }

        public boolean getScanningStatus(){
            return isScanning;
        }

        @Override
        public void onMediaScannerConnected() {
            //Log.d(TAG, "onMediaScannerConnected [" + count +"]: " + alList.get(count));
            mediaScannerConnection.scanFile(alList.get(count), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            //Log.d(TAG, "onScanCompleted [" + count +"]: " + path + " Uri:" + uri);
            count++;

            if(count < alList.size())
                onMediaScannerConnected(); // Restart
            else {
                Log.d(TAG, "onScanCompleted [Finish]");
                mediaScannerConnection.disconnect();
                isScanning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pageAdapter = new SamplePagerAdapter(alImageUri);
                        mViewPager.setAdapter(pageAdapter);
                        pageAdapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                });
            }
        }
    }
    ProgressDialog progressDialog;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        getActionBar().hide();

        Intent intent = getIntent();
        Serializable intentListData = intent.getSerializableExtra("ALFrameInfo");

        albumTitle = intent.getStringExtra("AlbumTitle");
        albumCode = intent.getStringExtra("AlbumCode");
        albumSubCode = getIntent().getStringExtra("AlbumSubCode");
        albumDeliverNum = getIntent().getStringExtra("AlbumDeliverNum");

        inwha_yn = getIntent().getStringExtra("inwha_yn");

        user_name = intent.getStringExtra("UserName");
        albumPrice = getIntent().getIntExtra("AlbumPrice", 0);

        albumType = getIntent().getIntExtra("AlbumType", Define.TYPE_NORMAL);

        agent_name = getStringPreference(Define.KEY_PRE_NAME);
        alFrameList = (ArrayList<FrameInfo>) intentListData;
        alImageUri = new ArrayList<Uri>();
        alImagePath = new ArrayList<String>();

        //Log.d(TAG, "Get Photo Data : " + alFrameList.size());
        for(int i = 0; i < alFrameList.size(); i++) {
            //Log.d(TAG, "Image Info :" + alFrameList.get(i).pageImage);
            File f = new File(alFrameList.get(i).pageImage);
            Uri uri = getImageContentUri(this, f);
            if(uri != null) {
                alImageUri.add(uri);
                alImagePath.add(alFrameList.get(i).pageImage);
            }
        }

        MultiMediaScanner scanner= new MultiMediaScanner(this, alImagePath);
        scanner.startScan();

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);


        iBtnOrder = (ImageButton) findViewById(R.id.btnOrder);
        iBtnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOrderInfoInput();
            }
        });
        iBtnPre = (ImageButton) findViewById(R.id.btnPre);
        iBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("이미지 검색");
            progressDialog.setMessage("잠시만 기다려 주세요.");
            progressDialog.show();

	}
    private final int GOTO_ORDERINPUT = 0x10;

    public void Complete() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void goToOrderInfoInput() {
        Intent intent = new Intent(this, OrderInfoInput.class);
        intent.putExtra("AlbumTitle", albumTitle);
        intent.putExtra("AlbumCode", albumCode);
        intent.putExtra("AlbumSubCode", albumSubCode);
        intent.putExtra("AlbumDeliverNum", albumDeliverNum);
        intent.putExtra("AlbumPrice", albumPrice);
        intent.putExtra("ALFrameInfo", alFrameList);
        intent.putExtra("AlbumType", albumType);
        intent.putExtra("inwha_yn", inwha_yn);

        startActivityForResult(intent, GOTO_ORDERINPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GOTO_ORDERINPUT) {
            if(resultCode == RESULT_OK) {
                Complete();
            }
        }
    }
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

	static class SamplePagerAdapter extends PagerAdapter {

        private ArrayList<Uri> alImageList;
        public SamplePagerAdapter(ArrayList<Uri> list) {
            this.alImageList = list;
        }

		@Override
		public int getCount() {
			return alImageList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());

			photoView.setImageURI(alImageList.get(position));

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

    private boolean isViewPagerActive() {
    	return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }
}
