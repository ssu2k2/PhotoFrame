package kr.pnit.mPhoto.order;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.R;


import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by macmini on 14. 12. 13..
 */
public class OrderProcessDialog extends Activity {
    private final String TAG = getClass().getSimpleName();
    LayoutInflater inflater;
    AlbumInfo albumInfo;
    String[]  images;
    int frameType = 0;
    int scale = 2000;
    FrameInfo frameInfo;
    String saveFilePath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_process_dialog);
        Intent intent = getIntent();
        frameInfo = (FrameInfo)intent.getSerializableExtra("FRAMEINFO");
        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        setAlbumInfo();
    }
    private void setAlbumInfo() {
        Intent intent = getIntent();
        albumInfo = new AlbumInfo();
        albumInfo.ratio_width = frameInfo.ratio_width;
        albumInfo.ratio_height = frameInfo.ratio_height;

        while(albumInfo.ratio_width * scale > 1000){
            scale --;
        }

        //Log.d(TAG, "Image ratio:" + albumInfo.ratio_width + " " + albumInfo.ratio_height);
        //Log.d(TAG, "Scale : " + scale);
        DrawBitmapToCanvas(frameInfo.type);

    }
    void initLayout(){

    }
    public static int getBitmapOfWidth( String fileName ){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outWidth;
        } catch(Exception e) {
            return 0;
        }
    }
    public static int getBitmapOfHeight( String fileName ){

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);

            return options.outHeight;
        } catch(Exception e) {
            return 0;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        //Log.d(TAG, "Resize Scale :" + scaleWidth + " " + scaleHeight);
        // recreate the new Bitmap
        //Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    void DrawBitmapToCanvas(int type) {
        //Log.d(TAG, "Image Size :" + albumInfo.ratio_width * scale+ " " + albumInfo.ratio_height * scale);
        Bitmap bitmap = Bitmap.createBitmap(albumInfo.ratio_width * scale, albumInfo.ratio_height * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int width = albumInfo.ratio_width * scale;
        int height = albumInfo.ratio_height * scale;

//        Log.d(TAG, "Layout params :" + width + " " + height);
//        Log.d(TAG, "Frame Type :" + type);
//        Log.d(TAG, "Frame info :" + frameInfo.frame_width + " " + frameInfo.frame_height);
//        Log.d(TAG, "Frame Image :" + frameInfo.images[0]);
//        Log.d(TAG, "Frame Image :" + frameInfo.images[1]);
//        Log.d(TAG, "Frame Image :" + frameInfo.images[2]);
//        Log.d(TAG, "Frame Image :" + frameInfo.images[3]);

        Bitmap bmImage = null;
        Bitmap bmResize = null;

        Paint paint = new Paint();

        switch(type) {
            case 0:
                //Log.d(TAG, "Frame Size :" + width + " " + height);
                if(frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width) || (bmImage.getHeight() != height)){
                        bmResize = getResizedBitmap(bmImage, width, height);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, new Paint());
                    }

                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
            case 1:
                if(frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width, height/2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[2] != null&& frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width, height/2);
                        canvas.drawBitmap(bmResize, 0, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height/2, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
            case 2:
                if(frameInfo.images[0] != null&& frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[1] != null && frameInfo.images[1].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[1]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, width/2, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width/2, 0, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width, height/2);
                        canvas.drawBitmap(bmResize, 0, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height/2, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
            case 3:
                if(frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width, height/2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }

                if(frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, 0, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height/2, paint);
                    }
                }else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[3] != null && frameInfo.images[3].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[3]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, width/2, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width/2, height/2, paint);
                    }
                } else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
            case 4:
            default:
                if(frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[1] != null && frameInfo.images[1].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[1]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, width/2, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width/2, 0, paint);
                    }
                } else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, 0, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height/2, paint);
                    }
                } else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                if(frameInfo.images[3] != null && frameInfo.images[3].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[3]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if((bmImage.getWidth() != width/2) || (bmImage.getHeight() != height/2)){
                        bmResize = getResizedBitmap(bmImage, width/2, height/2);
                        canvas.drawBitmap(bmResize, width/2, height/2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width/2, height/2, paint);
                    }
                } else {
                    Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
        }
        FileOutputStream fos = null;
        //Log.d(TAG, "Save File :" + Environment.getExternalStorageDirectory()+"/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg");

        File file = new File(Environment.getExternalStorageDirectory()+"/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg");
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            saveFilePath = Environment.getExternalStorageDirectory()+"/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
                if (bitmap != null) bitmap.recycle();
                if (bmImage != null) bmImage.recycle();
                if (bmResize != null) bmResize.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        exit();
    }

    private void exit() {
        Intent intent = getIntent();
        intent.putExtra("PAGE", frameInfo.pageNum);
        intent.putExtra("FILE", saveFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}
