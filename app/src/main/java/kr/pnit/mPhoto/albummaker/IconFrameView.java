package kr.pnit.mPhoto.albummaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kr.pnit.mPhoto.R;

/**
 * Created by macmini on 15. 1. 9..
 */
public class IconFrameView extends ViewGroup implements View.OnTouchListener{
    private final String TAG = getClass().getSimpleName();
    PointF currPointF;
    Handler mHandler;
    int pWidth = 600;
    int pHeight = 800;

    ImageView ivIcon;
    ImageButton ibTopLeft;
    ImageButton ibTopRight;
    ImageButton ibCenter;
    ImageButton ibBottomLeft;
    ImageButton ibBottomRight;

    public IconFrameView(Context context) {
        super(context);
        initLayout(context);
    }
    public IconFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
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
    private void initLayout(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.icon_frame, this, true);

        ivIcon = (ImageView)findViewById(R.id.iv_icon);
        ibTopLeft = (ImageButton)findViewById(R.id.iv_icon_topleft);
        ibTopRight = (ImageButton)findViewById(R.id.iv_icon_topright);
        ibCenter = (ImageButton)findViewById(R.id.iv_icon_center);
        ibBottomLeft = (ImageButton)findViewById(R.id.iv_icon_bottomleft);
        ibBottomRight = (ImageButton)findViewById(R.id.iv_icon_bottomright);

        ivIcon.setScaleType(ImageView.ScaleType.FIT_XY);

        ibTopLeft.setOnTouchListener(this);
        ibTopRight.setOnTouchListener(this);
        ibCenter.setOnTouchListener(this);
        ibBottomRight.setOnTouchListener(this);
        ibBottomLeft.setOnTouchListener(this);
    }

    public void setImageRes(int res) {
        ivIcon.setImageResource(res);
    }
    public void setImageUri(Uri uri) {
        ivIcon.setImageURI(uri);
    }
    public void setImageBitmap(Bitmap bmp) {
        ivIcon.setImageBitmap(bmp);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

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


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()){
            case R.id.iv_icon_topleft:
                break;
            case R.id.iv_icon_topright:
                break;
            case R.id.iv_icon_center:
                break;
            case R.id.iv_icon_bottomleft:
                break;
            case R.id.iv_icon_bottomright:
                break;
        }
        return false;
    }
}
