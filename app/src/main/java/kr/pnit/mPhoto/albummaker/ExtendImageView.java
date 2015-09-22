package kr.pnit.mPhoto.albummaker;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kr.pnit.mPhoto.order.OrderAlbum;

/**
 * Created by macmini on 14. 12. 17..
 */
public class ExtendImageView extends ImageView implements View.OnTouchListener{
    private final String TAG = getClass().getSimpleName();
    PointF currPointF;
    Handler mHandler;
    Context context;

    int pWidth = 600;
    int pHeight = 800;

    public ExtendImageView(Context context) {
        super(context);
        this.context = context;
        initLayout();
    }
    public ExtendImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initLayout();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    RelativeLayout.LayoutParams params;
    public void setParentViewSize(int width, int hight) {
        //Log.d(TAG, "Parent Size W:" + width + " H:" + hight);
        this.pWidth = width;
        this.pHeight = hight;
    }
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    private void initLayout() {
        setScaleType(ScaleType.FIT_XY);
        setOnTouchListener(this);
    }


    private int _xDelta;
    private int _yDelta;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    // 핀치시 두좌표간의 거리 저장

    float oldXDist = 1f;
    float newXDist = 1f;

    float oldYDist = 1f;
    float newYDist = 1f;

    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //Log.d(TAG, "ACTION_DOWN");
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                mode = DRAG;
                break;
            case MotionEvent.ACTION_UP:             // 첫번째 손가락을 떼었을 경우
            case MotionEvent.ACTION_POINTER_UP:     // 두번째 손가락을 떼었을 경우
                //Log.d(TAG, "ACTION_UP");
                mode = NONE;
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                //Log.d(TAG, "ACTION_POINTER_DOWN");
                //두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
                mode = ZOOM;
                oldXDist = newXDist = spacingX(event);
                oldYDist = newYDist = spacingY(event);
//                Log.d("zoom", "mode=ZOOM");
                return true;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                if(mode == DRAG) {  // 드래그 중
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;
                    layoutParams.rightMargin = -500;
                    layoutParams.bottomMargin = -500;

//                    Log.d(TAG, "Margins " + layoutParams.leftMargin + " " + layoutParams.topMargin
//                                          + getWidth() + " " + getHeight());

                    if((layoutParams.leftMargin < -getWidth()) |
                       (layoutParams.leftMargin > (pWidth)) |
                       (layoutParams.topMargin < -getHeight()) |
                       (layoutParams.topMargin > (pHeight))){
                        Message msg = new Message();
                        msg.what = OrderAlbum.REMOVE_CHILD_VIEW;
                        msg.obj = this;
                        mHandler.sendMessage(msg);
                        //Log.d(TAG, "REMOVE SELF");
                    }

                    view.setLayoutParams(layoutParams);
                } else if(mode == ZOOM){
                    newXDist = spacingX(event);
                    newYDist = spacingY(event);

                    if(newXDist > 20 | newYDist > 20){

                        layoutParams.width  = layoutParams.width + (int)(newXDist - oldXDist)/20;
                        layoutParams.height  = layoutParams.height + (int)(newYDist - oldYDist)/20;

                        if(layoutParams.width > 700) {
                            layoutParams.width = 700;
                        } else if(layoutParams.width < 50) {
                            layoutParams.width = 50;
                        }

                        if(layoutParams.height > 700) {
                            layoutParams.height = 700;
                        } else if(layoutParams.height < 50) {
                            layoutParams.height = 50;
                        }

                        view.setLayoutParams(layoutParams);
                    }
                }

                return true;
            default:
                mode = NONE;
                break;
        }
        return true;
    }
    private float spacingX(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        return (float)Math.sqrt(x * x);
    }
    private float spacingY(MotionEvent event) {
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(y * y);
    }
}
