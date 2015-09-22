package kr.pnit.mPhoto.order;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.OrderInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.R;
import kr.pnit.mPhoto.main.BaseActivity;


import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * Created by macmini on 14. 12. 1..
 */
public class OrderPayProcess extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    Button btnPay;
    Button btnGotoHome;
    Button btnGotoList;

    LinearLayout llPay;
    LinearLayout llComplete;
    TextView tvTitle;

    OrderInfo mOrderInfo;

    // For Pay Process
    private WebView payWebView;

    private static final int DIALOG_PROGRESS_WEBVIEW = 0;
    private static final int DIALOG_PROGRESS_MESSAGE = 1;
    private static final int DIALOG_ISP = 2;
    private static final int DIALOG_CARDAPP = 3;
    private static String DIALOG_CARDNM = "";
    private AlertDialog alertIsp;


    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            OrderPayProcess.this.setProgress(newProgress*1000);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_complete);
        getActionBar().hide();

        Intent intent = getIntent();

        mOrderInfo = (OrderInfo) intent.getSerializableExtra("OrderInfo");

        initLayout();
        initWebView();
    }
    @JavascriptInterface
    private void initWebView() {
        payWebView = (WebView)findViewById(R.id.contentView);
        payWebView.setWebChromeClient(new ChromeClient());
        payWebView.setWebViewClient(new SampleWebView());
        payWebView.getSettings().setJavaScriptEnabled(true);
        payWebView.getSettings().setSavePassword(false);

        payWebView.addJavascriptInterface(new AndroidBridge(), "android");

        //버튼부분을 보이지 않게 합니다.
        LinearLayout layout = (LinearLayout)findViewById(R.id.tstLinearLayout);
        layout.setVisibility(View.GONE);


        if((mOrderInfo.delevery_price + mOrderInfo.good_price) == 0) {
            // 쿠폰 사용에 대한 서버 요청
            saveStringPreference(Define.KEY_CUPON_NUM, "");
            viewPayResult(true);
        } else {
            //기본 페이지
            payWebView.loadUrl("file:///android_asset/pay_main.html");
        }
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @SuppressWarnings("unused")
    private AlertDialog getCardInstallAlertDialog(final String coCardNm){

        final Hashtable<String, String> cardNm = new Hashtable<String, String>();
        cardNm.put("HYUNDAE", "현대 앱카드");
        cardNm.put("SAMSUNG", "삼성 앱카드");
        cardNm.put("LOTTE",   "롯데 앱카드");
        cardNm.put("SHINHAN", "신한 앱카드");
        cardNm.put("KB", 	  "국민 앱카드");
        cardNm.put("HANASK",  "하나SK 통합안심클릭");
        //cardNm.put("SHINHAN_SMART",  "Smart 신한앱");

        final Hashtable<String, String> cardInstallUrl = new Hashtable<String, String>();
        cardInstallUrl.put("HYUNDAE", "market://details?id=com.hyundaicard.appcard");
        cardInstallUrl.put("SAMSUNG", "market://details?id=kr.co.samsungcard.mpocket");
        cardInstallUrl.put("LOTTE",   "market://details?id=com.lotte.lottesmartpay");
        cardInstallUrl.put("LOTTEAPPCARD",   "market://details?id=com.lcacApp");
        cardInstallUrl.put("SHINHAN", "market://details?id=com.shcard.smartpay");
        cardInstallUrl.put("KB", 	  "market://details?id=com.kbcard.cxh.appcard");
        cardInstallUrl.put("HANASK",  "market://details?id=com.ilk.visa3d");
        //cardInstallUrl.put("SHINHAN_SMART",  "market://details?id=com.shcard.smartpay");//여기 수정 필요!!2014.04.01

        AlertDialog alertCardApp =  new AlertDialog.Builder(OrderPayProcess.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("알림")
                .setMessage( cardNm.get(coCardNm) + " 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String installUrl = cardInstallUrl.get(coCardNm);
                        Uri uri = Uri.parse(installUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        Log.d("<INIPAYMOBILE>", "Call : " + uri.toString());
                        try{
                            startActivity(intent);
                        }catch (ActivityNotFoundException anfe) {
                            Toast.makeText(OrderPayProcess.this, cardNm.get(coCardNm) + "설치 url이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                        }
                        //finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(OrderPayProcess.this, "(-1)결제를 취소 하셨습니다." , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create();

        return alertCardApp;

    }//end getCardInstallAlertDialog

    protected Dialog onCreateDialog(int id) {//ShowDialog


        switch(id){

            case DIALOG_PROGRESS_WEBVIEW:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("로딩중입니다. \n잠시만 기다려주세요.");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;

            case DIALOG_PROGRESS_MESSAGE:
                break;


            case DIALOG_ISP:

                alertIsp =  new AlertDialog.Builder(OrderPayProcess.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("알림")
                        .setMessage("모바일 ISP 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                        .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String ispUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp";
                                payWebView.loadUrl(ispUrl);
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(OrderPayProcess.this, "(-1)결제를 취소 하셨습니다." , Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        })
                        .create();

                return alertIsp;

            case DIALOG_CARDAPP :
                return getCardInstallAlertDialog(DIALOG_CARDNM);

        }//end switch

        return super.onCreateDialog(id);

    }//end onCreateDialog

    private class SampleWebView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

	    	/*
	    	 * URL별로 분기가 필요합니다. 어플리케이션을 로딩하는것과
	    	 * WEB PAGE를 로딩하는것을 분리 하여 처리해야 합니다.
	    	 * 만일 가맹점 특정 어플 URL이 들어온다면
	    	 * 조건을 더 추가하여 처리해 주십시요.
	    	 */
            Log.d(TAG, "LOAD URL :" + url);
            if( !url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:") )
            {
                Intent intent;

                try{
                    Log.d("<INIPAYMOBILE>", "intent url : " + url);
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                    Log.d("<INIPAYMOBILE>", "intent getDataString : " + intent.getDataString());
                    Log.d("<INIPAYMOBILE>", "intent getPackage : " + intent.getPackage() );

                } catch (URISyntaxException ex) {
                    Log.e("<INIPAYMOBILE>", "URI syntax error : " + url + ":" + ex.getMessage());
                    return false;
                }

                Uri uri = Uri.parse(intent.getDataString());
                intent = new Intent(Intent.ACTION_VIEW, uri);



                try{

                    startActivity(intent);

	    			/*가맹점의 사정에 따라 현재 화면을 종료하지 않아도 됩니다.
	    			    삼성카드 기타 안심클릭에서는 종료되면 안되기 때문에
	    			    조건을 걸어 종료하도록 하였습니다.*/
                    if( url.startsWith("ispmobile://"))
                    {
                        finish();
                    }

                }catch(ActivityNotFoundException e)
                {
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, ActivityNotFoundException INPUT >> " + url);
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, uri.getScheme()" + intent.getDataString());

                    //ISP
                    if( url.startsWith("ispmobile://"))
                    {
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_ISP);
                        return false;
                    }

                    //현대앱카드
                    else if( intent.getDataString().startsWith("hdcardappcardansimclick://"))
                    {
                        DIALOG_CARDNM = "HYUNDAE";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 현대앱카드설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //신한앱카드
                    else if( intent.getDataString().startsWith("shinhan-sr-ansimclick://"))
                    {
                        DIALOG_CARDNM = "SHINHAN";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 신한카드앱설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //삼성앱카드
                    else if( intent.getDataString().startsWith("mpocket.online.ansimclick://"))
                    {
                        DIALOG_CARDNM = "SAMSUNG";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 삼성카드앱설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //롯데 모바일결제
                    else if( intent.getDataString().startsWith("lottesmartpay://"))
                    {
                        DIALOG_CARDNM = "LOTTE";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 롯데모바일결제 설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //롯데앱카드(간편결제)
                    else if(intent.getDataString().startsWith("lotteappcard://"))
                    {
                        DIALOG_CARDNM = "LOTTEAPPCARD";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 롯데앱카드 설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //KB앱카드
                    else if( intent.getDataString().startsWith("kb-acp://"))
                    {
                        DIALOG_CARDNM = "KB";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, KB카드앱설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //하나SK카드 통합안심클릭앱
                    else if( intent.getDataString().startsWith("hanaansim://"))
                    {
                        DIALOG_CARDNM = "HANASK";
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 하나카드앱설치 ");
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        showDialog(DIALOG_CARDAPP);
                        return false;
                    }

	    			/*
	    			//신한카드 SMART신한 앱
	    			else if( intent.getDataString().startsWith("smshinhanansimclick://"))
	    			{
	    				DIALOG_CARDNM = "SHINHAN_SMART";
	    				Log.e("INIPAYMOBILE", "INIPAYMOBILE, Smart신한앱설치");
	    				view.loadData("<html><body></body></html>", "text/html", "euc-kr");
	    				showDialog(DIALOG_CARDAPP);
				        return false;
	    			}
	    			*/

                    /**
                     > 현대카드 안심클릭 droidxantivirusweb://
                     - 백신앱 : Droid-x 안드로이이드백신 - NSHC
                     - package name : net.nshc.droidxantivirus
                     - 특이사항 : 백신 설치 유무는 체크를 하고, 없을때 구글마켓으로 이동한다는 이벤트는 있지만, 구글마켓으로 이동되지는 않음
                     - 처리로직 : intent.getDataString()로 하여 droidxantivirusweb 값이 오면 현대카드 백신앱으로 인식하여
                     하드코딩된 마켓 URL로 이동하도록 한다.
                     */

                    //현대카드 백신앱
                    else if( intent.getDataString().startsWith("droidxantivirusweb"))
                    {
                        /*************************************************************************************/
                        Log.d("<INIPAYMOBILE>", "ActivityNotFoundException, droidxantivirusweb 문자열로 인입될시 마켓으로 이동되는 예외 처리: " );
                        /*************************************************************************************/

                        Intent hydVIntent = new Intent(Intent.ACTION_VIEW);
                        hydVIntent.setData(Uri.parse("market://search?q=net.nshc.droidxantivirus"));
                        startActivity(hydVIntent);

                    }


                    //INTENT:// 인입될시 예외 처리
                    else if( url.startsWith("intent://"))
                    {

                        /**

                         > 삼성카드 안심클릭
                         - 백신앱 : 웹백신 - 인프라웨어 테크놀러지
                         - package name : kr.co.shiftworks.vguardweb
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 신한카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 농협카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 외환카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 씨티카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 하나SK카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 하나카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 롯데카드
                         - 백신이 설치되어 있지 않아도, 결제페이지로 이동

                         */

                        /*************************************************************************************/
                        Log.d("<INIPAYMOBILE>", "Custom URL (intent://) 로 인입될시 마켓으로 이동되는 예외 처리: " );
                        /*************************************************************************************/

                        try {

                            Intent excepIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String packageNm = excepIntent.getPackage();

                            Log.d("<INIPAYMOBILE>", "excepIntent getPackage : " + packageNm );

                            excepIntent = new Intent(Intent.ACTION_VIEW);
                            excepIntent.setData(Uri.parse("market://search?q="+packageNm));

                            startActivity(excepIntent);

                        } catch (URISyntaxException e1) {
                            Log.e("<INIPAYMOBILE>", "INTENT:// 인입될시 예외 처리  오류 : " + e1 );
                        }

                    }
                }

            }
            else
            {
                view.loadUrl(url);
                view.addJavascriptInterface(new Object()
                {
                    @JavascriptInterface
                    public void performClick() throws Exception
                    {
                        Log.d("Pay", "Clicked");
                        GotoHome();
                    }
                }, "ok");
//
//                if(url.endsWith("payment_return.asp")){
//                    viewPayResult(true);
//                } else if(url.endsWith("payment_cancel.asp")){
//                    viewPayResult(false);
//                }
                return false;
            }

            view.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                public void performClick() throws Exception
                {
                    GotoHome();
                }
            }, "ok");

//            if(url.endsWith("payment_return.asp")){
//                viewPayResult(true);
//            } else if(url.endsWith("payment_cancel.asp")){
//                viewPayResult(false);
//            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showDialog(0);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.getSettings().setJavaScriptEnabled(true);

            if(url.endsWith("pay_main.html")){
                //Log.d(TAG, "Order Info " + mOrderInfo.good_price + " " + mOrderInfo.delevery_price);
                String url_string = "javascript:onChnageInfo('"
                        + mOrderInfo.orderNumber +  "','"
                        + mOrderInfo.good_name + "','"
                        + (mOrderInfo.good_price  + mOrderInfo.delevery_price ) + "','"
                        + mOrderInfo.user_name + "','"
                        + mOrderInfo.user_phone + "','"
                        + "" + "')";
                view.loadUrl(url_string);
            }
            dismissDialog(0);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
                    "</head><body>"+"요청실패 : ("+errorCode+")" + description+"</body></html>", "text/html", "utf-8");
        }
    }
    private class AndroidBridge {
        @JavascriptInterface
        public void callAndroid(final String result) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
//                    if(result.equals("COMPLETE")){
//                        //Toast.makeText(OrderPayProcess.this, "결제가 완료 되었습니다.", Toast.LENGTH_LONG).show();
//                    } else if(result.equals("CANCELED")) {
//                        //Toast.makeText(OrderPayProcess.this, "결제가 취소 되었습니다.", Toast.LENGTH_LONG).show();
//                    }
                    GotoHome();
                }
            });
        }
    }

    private void initLayout() {
        btnPay = (Button)findViewById(R.id.btnPay);
        btnGotoHome = (Button)findViewById(R.id.btnGotoHome);
        btnGotoList = (Button)findViewById(R.id.btnGoToList);

        btnPay.setOnClickListener(this);
        btnGotoHome.setOnClickListener(this);
        btnGotoList.setOnClickListener(this);

        llPay = (LinearLayout)findViewById(R.id.llPay);
        llComplete = (LinearLayout)findViewById(R.id.llComplete);
        llPay.setVisibility(View.VISIBLE);
        llComplete.setVisibility(View.GONE);

        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.pay_title));
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPay:
                viewPayResult(true);
                break;
            case R.id.btnGotoHome:
                GotoHome();
                break;
            case R.id.btnGoToList:
                GotoList();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if(llComplete.getVisibility() == View.VISIBLE) {
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    private void viewPayResult(boolean isComplete) {
        tvTitle.setText(getResources().getString(R.string.pay_complete_title));
        llPay.setVisibility(View.GONE);
        llComplete.setVisibility(View.VISIBLE);
        if(!isComplete){
            ((TextView)findViewById(R.id.tvComplete)).setText(R.string.pay_cancel_info);
        }
    }
    private void GotoHome() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void GotoList() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        // Goto List
        Intent intentToList = new Intent(this, OrderList.class);
        startActivity(intentToList);

        finish();

    }
}
