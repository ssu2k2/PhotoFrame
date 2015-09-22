package kr.pnit.mPhoto.DTO;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by macmini on 14. 12. 14..
 */
public class FrameInfo implements Serializable {
    public int type;
    public int pageNum;
    public String[] images;

    public int ratio_width;
    public int ratio_height;

    public int view_width;
    public int view_height;


    public int frame_width;
    public int frame_height;

    public boolean isConvert = false;

    public String pageImage = "";

    public FrameInfo () {

        images = new String[]{"","","",""};
        ratio_height = 4;
        ratio_width = 3;
        type = 0;
        frame_height = 2000;
        frame_width = 1500;
    }
}
