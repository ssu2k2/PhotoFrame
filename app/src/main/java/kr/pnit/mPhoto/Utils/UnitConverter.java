package kr.pnit.mPhoto.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by macmini on 15. 2. 9..
 */
public class UnitConverter {
    /**
     * dp 를 px 로 변환하는 메소드
     *
     * @param dp
     * @return
     */
    public static float convertDpPixel(Context context, int dp) {
        final Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }
}
