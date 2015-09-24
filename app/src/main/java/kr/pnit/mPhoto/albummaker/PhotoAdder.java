package kr.pnit.mPhoto.albummaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.DTO.DecoIconInfo;
import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.DTO.SendFileInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Dialog.SendImageActivity;

/**
 * Created by macmini on 15. 3. 30..
 */
public class PhotoAdder {
    private static final String TAG = "PhotoAdder";
    Context mContext;
    private ArrayList<FrameInfo> alFileInfo;
    private ArrayList<String> alResultFileInfo;

    String saveFilePath = "";
    int image_width = 0;
    int image_height = 0;

    public PhotoAdder(Context context,  ArrayList<FrameInfo> list) {
        this.mContext = context;
        this.alFileInfo = list;
        alResultFileInfo = new ArrayList<String>();
    }
    public ArrayList<String> getResultFileInfo() {
        return alResultFileInfo;
    }

    public boolean startPhotoMaker() {

        // get Image Width, Height
        getImageSize(alFileInfo.get(0).pageImage);

        for(int i = 0; i < alFileInfo.size() ; i+=2){
            String resultFile = null;
            if(alFileInfo.get(i+1) != null) {
                String firstFile = alFileInfo.get(i).pageImage;
                String secondFile = alFileInfo.get(i + 1).pageImage;
                Log.d(TAG, "First : " + firstFile + " Second:" + secondFile);
                resultFile = DrawBitmapToCanvas(image_width, image_height, firstFile, secondFile, i);
                if (resultFile != null) {
                    Log.d(TAG, "SUCCESS : " + resultFile);
                    alResultFileInfo.add(resultFile);
                }else
                    return false;
            } else {
                alResultFileInfo.add(alFileInfo.get(i).pageImage);
            }

        }
        return true;
    }

    private void getImageSize(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        image_height = options.outHeight;
        image_width = options.outWidth;
        Log.d(TAG, "getImageSize " +path + " W:" + options.outWidth + " H:" + options.outHeight);
    }
    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        //Log.d(TAG, "Resize Scale :" + scaleWidth + " " + scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    private String DrawBitmapToCanvas(int width, int height, String firstFile, String secondFile, int count) {

        Bitmap bitmap = Bitmap.createBitmap(width * 2, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Bitmap bmImage = null;
        Bitmap bmResize = null;
        Paint paint = new Paint();

        {
                if (firstFile != null && firstFile.length() != 0) {
                    bmImage = BitmapFactory.decodeFile(firstFile);
                    Log.d(TAG, "First Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height)) {
                        bmResize = getResizedBitmap(bmImage, width, height);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return null;
                }
                if (secondFile != null && secondFile.length() != 0) {
                    bmImage = BitmapFactory.decodeFile(secondFile);
                    Log.d(TAG, "Second Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height)) {
                        bmResize = getResizedBitmap(bmImage, width, height);
                        canvas.drawBitmap(bmResize, width, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return null;
                }

        }

        FileOutputStream fos = null;

        File file = new File(Environment.getExternalStorageDirectory() + "/" + Define.FOLDER_ALBUM + "/PAGE_MERGE_" + count +"_" + (int)(count+1) + ".jpg");
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            saveFilePath = Environment.getExternalStorageDirectory() + "/" + Define.FOLDER_ALBUM + "/PAGE_MERGE_" + count +"_" + (int)(count+1) + ".jpg";
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
       return saveFilePath;
    }
}
