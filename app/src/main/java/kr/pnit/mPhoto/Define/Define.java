package kr.pnit.mPhoto.Define;

/**
 * Created by macmini on 14. 12. 9..
 */
public class Define {

    public static final String FOLDER = "Pnit";
    public static final String FOLDER_ALBUM = FOLDER + "/Album";
    public static final String FOLDER_GRID  = FOLDER + "/Grid";
    public static final String FOLDER_TEMP  = FOLDER + "/Temp";

    public static final String HTTP_URL = "http://book208.fotokids.co.kr/PhotoMobileManager";
    public static final String HTTP_GET_SMS = HTTP_URL + "/member/mobile_sns_ok.asp";
    public static final String HTTP_SHOP_ID = HTTP_URL + "/member/Mobile_manager.asp?";
    public static final String HTTP_ORDER_LIST= HTTP_URL + "/member/Mobile_order_ok.asp?";
    public static final String HTTP_PRODUCT_LIST = HTTP_URL + "/member/Mobile_product_list.asp";
    public static final String HTTP_PRODUCT_SUB_LIST = HTTP_URL + "/member/mobile_product_sub_list.asp";

    public static final String HTTP_ORDER_PRODUCT = HTTP_URL + "/order/order_Mobile_ok.asp";
    public static final String HTTP_DECO_ICON = HTTP_URL + "/member/Mobile_icon_list.asp";
    public static final String HTTP_FTP_INFO = HTTP_URL + "/member/Mobile_FTP_ok.asp";

    public static final String HTTP_ORDER_CANCEL = HTTP_URL + "/member/mobile_cancel_ok.asp";

    public static final String HTTP_ZIP_CODE_01 = HTTP_URL + "/member/Mobile_zipcode_one.asp";
    public static final String HTTP_ZIP_CODE_02 = HTTP_URL + "/member/Mobile_zipcode_two.asp";
    public static final String HTTP_ZIP_CODE_03 = HTTP_URL + "/member/Mobile_zipcode_three.asp";
    public static final String HTTP_ZIP_CODE_04 = HTTP_URL + "/member/Mobile_zipcode_four.asp";

    public static final String KEY_PRE_ID = "pre_id";
    public static final String KEY_A_CODE = "a_code";
    public static final String KEY_PRE_NAME = "pre_name";
    public static final String KEY_OW_TEL = "ow_tel";
    public static final String KEY_OW_ZIP = "ow_zip";
    public static final String KEY_OW_ADDR = "ow_addr";

    public static final String KEY_CUPON = "c_cupun";
    public static final String KEY_CUPON_PRODUCT = "c_product_code";

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_GRID = 1;

    //Preference
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_TEL  = "user_tel";
    public static final String PREF_USER_ADDR = "user_addr";
    public static final String PREF_ZIP_CODE = "zip_code";

    public static final boolean isDebugging = false;
}
