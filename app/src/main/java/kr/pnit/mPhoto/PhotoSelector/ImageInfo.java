package kr.pnit.mPhoto.PhotoSelector;

import android.net.Uri;

import java.util.Date;

/**
 * Created by macmini on 14. 12. 26..
 */
public class ImageInfo {
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_TITLE = "title";

    String type;
    Date date;
    String content;
    boolean isSelect;
    String path;
    String folder;
    Uri    uri;

    public ImageInfo(){
        type  = TYPE_IMAGE;
        isSelect = false;
        content = "";
        path = null;
        folder = null;
    }
}
