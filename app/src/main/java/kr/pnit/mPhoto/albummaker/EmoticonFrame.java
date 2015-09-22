package kr.pnit.mPhoto.albummaker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import kr.pnit.mPhoto.R;
import kr.pnit.mPhoto.order.OrderAlbum;

/**
 * Created by yongsucho on 15. 3. 20..
 */
public class EmoticonFrame extends LinearLayout implements View.OnTouchListener{
    public static final int REMOVE = 0xf0;
    private static final String TAG = "EmoticonFrame";
    private final int DRAG_MARGIN = 5;
    Context mContext = null;

    ImageView ivEmoticon;
    View vParent;
    Handler mHandler = null;

    boolean isControl;
    boolean isMove;

    public EmoticonFrame(Context context) {
        super(context);
        init(context);
    }
    public EmoticonFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmoticonFrame(Context context, AttributeSet attrs, int theme) {
        super(context, attrs, theme);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        Log.d(TAG, "init");

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        vParent = li.inflate(R.layout.emoticon_frame, null);

        addView(vParent);

        ivEmoticon = (ImageView)vParent.findViewById(R.id.ivEmoticon);

        vParent.findViewById(R.id.ivRemove).setOnTouchListener(this);
        vParent.findViewById(R.id.ivRotate).setOnTouchListener(this);
        vParent.findViewById(R.id.ivResize).setOnTouchListener(this);
        vParent.findViewById(R.id.ivEmoticon).setOnTouchListener(this);

        setControlVisible(View.INVISIBLE);

        pDown = new PointF();
        pMove = new PointF();

    }
    public void setMargin(int left, int top) {
        lLeftMargin = left;
        lTopMargin = top;
    }
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    int visible;

    public int getControlVisible()
    {
        return visible;
    }
    public void setControlVisible(int visible) {
        this.visible = visible;
        if(visible == View.VISIBLE) {
            Log.d(TAG, "setControlVisible VISIBLE");
            vParent.findViewById(R.id.ivRemove).setVisibility(View.VISIBLE);
            vParent.findViewById(R.id.ivRotate).setVisibility(View.VISIBLE);
            vParent.findViewById(R.id.ivResize).setVisibility(View.VISIBLE);
            isControl = true;
        } else if(visible == View.INVISIBLE) {
            Log.d(TAG, "setControlVisible INVISIBLE");
            vParent.findViewById(R.id.ivRemove).setVisibility(View.INVISIBLE);
            vParent.findViewById(R.id.ivRotate).setVisibility(View.INVISIBLE);
            vParent.findViewById(R.id.ivResize).setVisibility(View.INVISIBLE);
            isControl = false;
        } else if(visible == View.GONE) {
            Log.d(TAG, "setControlVisible GONE");
            vParent.findViewById(R.id.ivRemove).setVisibility(View.GONE);
            vParent.findViewById(R.id.ivRotate).setVisibility(View.GONE);
            vParent.findViewById(R.id.ivResize).setVisibility(View.GONE);
            isControl = false;
        }
    }

    public ImageView getEmoticonView() {
        return ivEmoticon;
    }

    public void setImageResource(int res) {
        ivEmoticon.setImageResource(res);
    }

    public void setImageDrawable(Drawable drawable) {
        ivEmoticon.setImageDrawable(drawable);
    }

    private PointF pDown;
    private PointF pMove;

    int lLeftMargin;
    int lTopMargin;
    int iOrgWidth;
    int iOrgHeight;
    Bitmap bitmap;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();


        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                if(isControl) {
                    pDown.x = X;
                    pDown.y = Y;
                    if(v.getId() == R.id.ivResize) {
                        //Log.d(TAG, "onTouch Down Resize");
                        LayoutParams  lOrgParams = (LayoutParams)ivEmoticon.getLayoutParams();
                        iOrgWidth = lOrgParams.width;
                        iOrgHeight = lOrgParams.height;
                    } else if(v.getId() == R.id.ivRotate) {
                        //Log.d(TAG, "onTouch Down Rotate");
                        bitmap =((BitmapDrawable)ivEmoticon.getDrawable()).getBitmap();
                    }else if(v.getId() == R.id.ivEmoticon) {
                        //Log.d(TAG, "onTouch Down Emoticon");
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)getLayoutParams();
                        lLeftMargin = lParams.leftMargin;
                        lTopMargin = lParams.topMargin;
                    }

                } else {
                    pDown.x = 0;
                    pDown.y = 0;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isControl) {
                    //Log.d(TAG, "onTouch Control End");
                    if(v.getId() == R.id.ivRemove) {
                        //Log.d(TAG, "onTouch Remove Emoticon");
                        if(mHandler != null) {
                            Message msg = new Message();
                            msg.what = OrderAlbum.REMOVE_CHILD_VIEW;
                            msg.obj = this;
                            mHandler.sendMessage(msg);
                        }

                    } else if(v.getId() == R.id.ivEmoticon) {
                        if(!isMove)
                            setControlVisible(View.INVISIBLE);
                    } else if(v.getId() == R.id.ivRotate) {
                        bitmap.recycle();
                    } else {
                        pDown.x = 0;
                        pDown.y = 0;

                        pMove.x = 0;
                        pMove.y = 0;
                    }
                } else {
                    //Log.d(TAG, "onTouch Control Enable");
                    if(v.getId() == R.id.ivEmoticon){
                        setControlVisible(View.VISIBLE);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isControl) {
                    //Log.d(TAG, "onTouch Control Move");
                    pMove.x = X;
                    pMove.y = Y;
                    if(v.getId() == R.id.ivResize) {
                        //Log.d(TAG, "onTouch Resize Emotion");
                        LayoutParams lParams = (LayoutParams) ivEmoticon.getLayoutParams();
                        if(lParams.width + getDistance(pDown.x , pMove.x) > 0 &
                           lParams.height + getDistance(pDown.y , pMove.y) > 0){

                            lParams.width = iOrgWidth +  getDistance(pDown.x , pMove.x);
                            lParams.height = iOrgHeight + getDistance(pDown.y , pMove.y);

                            ivEmoticon.setLayoutParams(lParams);
                            ivEmoticon.setScaleType(ImageView.ScaleType.FIT_XY);
                        }
                    } else if(v.getId() == R.id.ivRotate) {
                        //Log.d(TAG, "onTouch Rotate Emoticon ");
                        if((getDistance(pDown.y , pMove.y) % 6) == 0){
                            float degree = getDistance(pDown.y , pMove.y) / 3;
                            if(degree > 360) degree = 360;
                            if(degree < -360) degree = -360;
                            //Log.d(TAG, "Rotate degree:" + degree + " " + getDistance(pDown.y , pMove.y));
                            Bitmap mod = null;
                            try {
                                if(bitmap.isRecycled()){
                                    bitmap =((BitmapDrawable)ivEmoticon.getDrawable()).getBitmap();
                                }
                                mod = RotateBitmap(bitmap, degree);
                            } catch(RuntimeException re) {
                                Log.d(TAG, "Bitmap RunTimeException");
                            }
                            if(mod != null)
                                ivEmoticon.setImageBitmap(mod);
                        }

                    } else if(v.getId() == R.id.ivEmoticon) {
                        //Log.d(TAG, "onTouch Move Emotion");
                        if(Math.abs(getDistance(pDown.x , pMove.x)) > DRAG_MARGIN &
                                Math.abs(getDistance(pDown.y , pMove.y)) > DRAG_MARGIN) {
                            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)getLayoutParams();
                            lParams.leftMargin = lLeftMargin + getDistance(pDown.x , pMove.x);
                            lParams.topMargin  = lTopMargin + getDistance(pDown.y , pMove.y);
                            lParams.rightMargin = -500;
                            lParams.bottomMargin = -500;
                            isMove = true;
//                            Log.d(TAG, "ORG  L:" + lLeftMargin + " T:" + lTopMargin);
//                            Log.d(TAG, "Move L:" + (lLeftMargin + getDistance(pDown.x , pMove.x)) + " T:" + (lTopMargin + getDistance(pDown.y , pMove.y)));
//                            Log.d(TAG, "DIST X:" + getDistance(pDown.x , pMove.x) + " Y:" + getDistance(pDown.y , pMove.y));

                            this.setLayoutParams(lParams);
                        }
                    }
                }
                break;

            default:
                break;
        }
        return true;
    }

    private Bitmap RotateBitmap(Bitmap source, float angle) throws RuntimeException
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private int getDistance(float org, float tgt) {
        return (int)(tgt - org);
    }

    private  int convertDpPixel(Context context, int dp) {
        final Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int)px;
    }
}
