package kr.pnit.mPhoto.DTO;

import java.io.Serializable;

/**
 * Created by macmini on 14. 11. 29..
 */
public class AlbumInfo implements Serializable {

    public final static int TYPE_PHOTOALONE     = 0;
    public final static int TYPE_PHOTOFRAME     = 1;
    public final static int TYPE_PHOTOICONFRAME = 2;

    public int type;
    public String code;

    public String imgURL;
    public String title;

    public int maxPage;

    public int ratio_width;
    public int ratio_height;

    public int max_width;
    public int max_height;

    public int price;
    public int delivery_price;

    public int edit_gubun;
    public int deliver_num;

    public int resolution_W;
    public int resolution_H;

    public String inwha_yn;
    public String sub_code;
    public String sub_name;
    public String sub_imgURL;

    public int sub_price;

    public AlbumInfo() {
        type = 0;
        edit_gubun = 1;
        code = "";
        imgURL = "";
        title = "";
        deliver_num = 0;
        maxPage = 1;
        ratio_width = 3;
        ratio_height = 4;
        max_width = 3000;
        max_height = 4000;
        price = 0;
        delivery_price = 0;

        sub_code = "";
        sub_name = "";
        sub_imgURL = "";
        sub_price = 0;
    }

}
