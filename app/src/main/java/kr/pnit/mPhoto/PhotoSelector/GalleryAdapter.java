package kr.pnit.mPhoto.PhotoSelector;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kr.pnit.mPhoto.R;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

/**
 * Created by macmini on 14. 12. 26..
 */
public class GalleryAdapter extends ArrayAdapter<ImageInfo> {
    private static final String TAG = "GalleryAdapter";
    private ImageLoaderConfiguration config;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private Handler mHandler;

    public static final int HANDLER_MOVE = 0x01;
    public static final int HANDLER_UPDATE = 0x02;

    public class Holder {
        TextView tvTitle;
        ImageView ivPhoto;
        ImageView ivCheck;
    }

    ArrayList<ImageInfo> items;
    Context context;
    int resId;
    public GalleryAdapter (Context context, int res, ArrayList<ImageInfo> items) {
        super(context, res, items);
        this.items = items;
        this.context = context;
        this.resId = res;

        // Create configuration for ImageLoader
        config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2000000)) // You can pass your own memory cache implementation
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.gallery_temp)
                .showImageForEmptyUri(R.drawable.gallery_temp)
                .showImageOnFail(R.drawable.gallery_temp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        // Get singleton instance of ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

    }
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final Holder holder;
        if(v == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.selector_grid_item, null);
            holder = new Holder();
            holder.tvTitle = (TextView)v.findViewById(R.id.tvTitle);
            holder.ivPhoto = (ImageView)v.findViewById(R.id.ivPhoto);
            holder.ivCheck = (ImageView)v.findViewById(R.id.ivCheck);
            v.setTag(holder);
        } else {
            holder = (Holder)v.getTag();
        }

        ImageInfo info = getItem(position);

        String[] path = info.folder.split("/");

        if(info.type == ImageInfo.TYPE_IMAGE) {

            if(path.length > 0)
                holder.tvTitle.setText(path[path.length - 1]);
            else
                holder.tvTitle.setText(info.folder);

            holder.tvTitle.setVisibility(View.INVISIBLE);

            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivCheck.setVisibility(View.VISIBLE);
            //holder.ivPhoto.setImage
            info.uri = getUriFromPath(info.path);

            if(info.uri != null)
                ImageLoader.getInstance().displayImage(info.uri.toString(), (ImageView)holder.ivPhoto, options);
            else {

            }
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageInfo i = getItem(position);
                    if(i.isSelect){
                        i.isSelect = false;
                    } else {
                        i.isSelect = true;
                    }
                    if(mHandler != null) {
                        Message msg = new Message();
                        msg.what = HANDLER_UPDATE;
                        mHandler.sendMessage(msg);
                    }
                    notifyDataSetChanged();
                }
            });
            if(info.isSelect)
                holder.ivCheck.setImageResource(R.drawable.btn_check_on);
            else
                holder.ivCheck.setImageResource(R.drawable.btn_check_off);
        } else {
            // Folder
            if(path.length > 0)
                holder.tvTitle.setText(path[path.length - 1]);
            else
                holder.tvTitle.setText(info.folder);

            holder.tvTitle.setTag(R.string.tag_path, info.folder);

            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.ivCheck.setVisibility(View.GONE);
            holder.ivPhoto.setImageResource(R.drawable.btn_folder);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if(mHandler != null) {
                            Message msg = new Message();
                            msg.what = HANDLER_MOVE;
                            msg.obj = (String)holder.tvTitle.getTag(R.string.tag_path);
//                            msg.obj = (String)(holder.tvTitle.getText().toString());
                            mHandler.sendMessage(msg);
                        }
                }
            });
        }
        return v;
    }
    public Uri getUriFromPath(String path){
        Uri fileUri = Uri.parse(path);
        String filePath = fileUri.getPath();
        //Log.d(TAG, "getUriFromPath :" + filePath);
        Cursor c = context.getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, "_data = '" + filePath + "'", null, null );
        c.moveToNext();
        try {
            int id = c.getInt(c.getColumnIndex("_id"));
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            c.close();
            return uri;
        }catch(CursorIndexOutOfBoundsException ce){
            Log.d(TAG, "Cannot Load getUriFromPath :" + filePath);
            return null;
        }
    }
}
