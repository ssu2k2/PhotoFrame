package kr.pnit.mPhoto.albummaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.AlbumInfo;
import kr.pnit.mPhoto.DTO.DecoIconInfo;
import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.Define.Define;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by macmini on 15. 3. 30..
 */
public class PhotoMaker {
    private static final String TAG = "PhotoMaker";
    Context mContext;
    AlbumInfo albumInfo;
    FrameInfo frameInfo;
    private ArrayList<DecoIconInfo> alDecoIconInfo;

    String[]  images;
    int frameType = 0;
    int scale = 2000;
    String saveFilePath = "";

    public PhotoMaker(Context context, AlbumInfo albumInfo, FrameInfo frameInfo, ArrayList<DecoIconInfo> alDecoIconInfo) {
        this.mContext = context;
        this.albumInfo = albumInfo;
        this.frameInfo = frameInfo;
        this.alDecoIconInfo = alDecoIconInfo;
    }
    public String getSaveFilePath(){
        return saveFilePath;
    }
    public boolean startPhotoMaker() {

        albumInfo = new AlbumInfo();
        albumInfo.ratio_width = frameInfo.ratio_width;
        albumInfo.ratio_height = frameInfo.ratio_height;

        if(albumInfo.ratio_height > 1000 ) {
            albumInfo.ratio_height  /= 1000;
            albumInfo.ratio_width /= 1000;
        }

        while(albumInfo.ratio_width * scale > 1000){
            scale --;
        }

//        Log.d(TAG, "Image ratio:" + albumInfo.ratio_width + " " + albumInfo.ratio_height);
//
//        Log.d(TAG, "Frame size  W:" + frameInfo.view_width + " H:" + frameInfo.view_height);
//
//        Log.d(TAG, "Scale : " + scale);

        if(DrawBitmapToCanvas(frameInfo.type)) {
            return true;
        } else {
            return false;
        }
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

    private boolean DrawBitmapToCanvas(int type) {
        float iconScale = 1f;

        //Log.d(TAG, "Image Size :" + albumInfo.ratio_width * scale + " " + albumInfo.ratio_height * scale);

        iconScale = (float)(albumInfo.ratio_width * scale) / (float)frameInfo.view_width ;

        Bitmap bitmap = Bitmap.createBitmap(albumInfo.ratio_width * scale, albumInfo.ratio_height * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int width = albumInfo.ratio_width * scale;
        int height = albumInfo.ratio_height * scale;

        Bitmap bmImage = null;
        Bitmap bmResize = null;

        Paint paint = new Paint();

        switch (type) {
            case 0:
                //Log.d(TAG, "Frame Size :" + width + " " + height);
                if (frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height)) {
                        bmResize = getResizedBitmap(bmImage, width, height);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, new Paint());
                    }

                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 1:
                if (frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width, height / 2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width, height / 2);
                        canvas.drawBitmap(bmResize, 0, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 2:
                if (frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[1] != null && frameInfo.images[1].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[1]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, width / 2, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width / 2, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width, height / 2);
                        canvas.drawBitmap(bmResize, 0, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 3:
                if (frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width, height / 2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, 0, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[3] != null && frameInfo.images[3].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[3]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, width / 2, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width / 2, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 4:
            default:
                if (frameInfo.images[0] != null && frameInfo.images[0].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[0]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, 0, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[1] != null && frameInfo.images[1].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[1]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, width / 2, 0, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width / 2, 0, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[2] != null && frameInfo.images[2].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[2]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, 0, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, 0, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (frameInfo.images[3] != null && frameInfo.images[3].length() != 0) {
                    bmImage = BitmapFactory.decodeFile(frameInfo.images[3]);
                    //Log.d(TAG, "Image Size :" + bmImage.getWidth() + " " + bmImage.getHeight());
                    if ((bmImage.getWidth() != width / 2) || (bmImage.getHeight() != height / 2)) {
                        bmResize = getResizedBitmap(bmImage, width / 2, height / 2);
                        canvas.drawBitmap(bmResize, width / 2, height / 2, paint);
                    } else {
                        canvas.drawBitmap(bmImage, width / 2, height / 2, paint);
                    }
                } else {
                    //Toast.makeText(mContext, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }
        FileOutputStream fos = null;
        //Log.d(TAG, "Save File :" + Environment.getExternalStorageDirectory() + "/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg");

        for(int i = 0; i < alDecoIconInfo.size(); i++) {
            DecoIconInfo deco = alDecoIconInfo.get(i);
            if(deco.bIcon != null) {
                canvas.drawBitmap(deco.bIcon, null,
                        new Rect((int)(deco.PointX * iconScale),
                                 (int)(deco.PointY * iconScale),
                                 (int)(deco.PointX * iconScale) + (int)(deco.ImageWidth * iconScale) ,
                                 (int)(deco.PointY * iconScale) + (int)(deco.ImageHeight * iconScale)), paint);
                //canvas.drawBitmap(deco.bIcon, deco.PointX, deco.PointY, paint);
            }
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg");
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            saveFilePath = Environment.getExternalStorageDirectory() + "/" + Define.FOLDER_ALBUM + "/PAGE_" + frameInfo.pageNum + ".jpg";
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
       return true;
    }
}
