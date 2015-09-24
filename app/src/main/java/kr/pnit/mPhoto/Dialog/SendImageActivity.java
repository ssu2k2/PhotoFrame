package kr.pnit.mPhoto.Dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import kr.pnit.mPhoto.DTO.FrameInfo;
import kr.pnit.mPhoto.DTO.OrderInfo;
import kr.pnit.mPhoto.DTO.SendFileInfo;
import kr.pnit.mPhoto.Define.Define;
import kr.pnit.mPhoto.Network.ParamVO;
import kr.pnit.mPhoto.R;

import kr.pnit.mPhoto.Utils.FileUtils;
import kr.pnit.mPhoto.albummaker.PhotoAdder;
import kr.pnit.mPhoto.main.BaseActivity;
import kr.pnit.mPhoto.order.OrderPayProcess;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by macmini on 15. 1. 27..
 */
public class SendImageActivity extends BaseActivity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    ArrayList<FrameInfo> alFrameList;

    TextView tvSize;
    TextView tvStatus;

    Button btnSend;
    Button btnCancel;

    ImageButton iBtnPre;
    ImageButton iBtnOrder;

    long TotalSizeofFiles = 0l;
    String ServerPath = "FILEPATH";
    String albumTitle;
    String albumCode;
    String albumSubCode;
    int albumPrice;
    StringBuilder sbStatus;

    String product_name;
    String agent_name;
    String user_name;
    String inwha_yn;

    OrderInfo mOrderInfo = null;

    ArrayList<SendFileInfo> alSendFileInfo;
    boolean isCompleteTransfer = false;

    ProgressDialog photoMakerdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_send_server);
        getActionBar().hide();

        alSendFileInfo = new ArrayList<SendFileInfo>();
        sbStatus = new StringBuilder();

        Intent intent = getIntent();

        albumTitle = intent.getStringExtra("AlbumTitle");
        albumCode = intent.getStringExtra("AlbumCode");
        albumSubCode = intent.getStringExtra("AlbumSubCode");

        inwha_yn  = intent.getStringExtra("inwha_yn");

        user_name = intent.getStringExtra("UserName");
        albumPrice = getIntent().getIntExtra("AlbumPrice", 0);
        agent_name = getStringPreference(Define.KEY_PRE_NAME);

        Serializable intentListData = intent.getSerializableExtra("ALFrameInfo");
        alFrameList = (ArrayList<FrameInfo>) intentListData;

        mOrderInfo = (OrderInfo) intent.getSerializableExtra("OrderInfo");

        initLayout();

        tvSize.setText("이미지 변환 작업중...");
        btnSend.setEnabled(false);

        prepareNetworking(Define.HTTP_FTP_INFO, "GET");

    }
    private void StartimageProcss() {
        Log.d(TAG, "Inwha : " + inwha_yn);
        if(inwha_yn != null && inwha_yn.length() == 1){
            if(inwha_yn.toUpperCase().equals("N")){
                // Nothing
                setImageFIleInfo();
            } else if(inwha_yn.toUpperCase().equals("Y")){
                // 이미지 합치기 작업 수행

                photoMakerdialog = ProgressDialog.show(SendImageActivity.this, "", "사진 변환 중입니다.", true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageConvertingProcess();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                photoMakerdialog.dismiss();
                                btnSend.setEnabled(true);
                                tvSize.setText(FileUtils.customFormat("###,###,###", TotalSizeofFiles) + " Bytes");
                            }
                        });
                    }
                }).run();
                photoMakerdialog.show();
            }
        } else {
            setImageFIleInfo();
        }
    }
    private boolean ImageConvertingProcess(){
        Log.d(TAG, "RUN ImageConvertingProcess()");
        PhotoAdder photoAdder = new PhotoAdder(SendImageActivity.this, alFrameList);
        if(photoAdder.startPhotoMaker()){

            ArrayList<String> alResult = photoAdder.getResultFileInfo();
            alSendFileInfo.clear();
            for(int i = 0; i < alResult.size(); i++) {
                if(alResult.get(i) != null){
                    alSendFileInfo.add(new SendFileInfo(alResult.get(i), getFileInfo(alResult.get(i))));
                }
            }
            return true;
        } else {
            return false;
        }

    }

    private void setImageFIleInfo() {

        //Log.d(TAG, "Get Photo Data : " + alFrameList.size());
        for(int i = 0; i < alFrameList.size(); i++) {
            //Log.d(TAG, "Image Info :" + alFrameList.get(i).pageImage);
            //Log.d(TAG, "Image Size :" + getFileInfo(alFrameList.get(i).pageImage));
            if(alFrameList.get(i).isConvert){
                alSendFileInfo.add(new SendFileInfo(alFrameList.get(i).pageImage, getFileInfo(alFrameList.get(i).pageImage)));

            }
        }
        //Log.d(TAG, "Total Size :" + FileUtils.customFormat("###,###,###", TotalSizeofFiles) + " Bytes " + alSendFileInfo.size() + " Files");
        tvSize.setText(FileUtils.customFormat("###,###,###", TotalSizeofFiles) + " Bytes");
        btnSend.setEnabled(true);
        //tvPath.setText(ServerPath);
    }

    private final int GOTO_ORDERINPUT = 0x10;
    private final int GOTO_PAYPROCESS = 0x11;


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void Complete() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
    private void GoToPayProcess() {
        int imageNum  = alSendFileInfo.size();

        Intent intent = new Intent(this, OrderPayProcess.class);
        intent.putExtra("SendImageNum", imageNum);
        intent.putExtra("AlbumPrice", albumPrice);
        intent.putExtra("OrderInfo", mOrderInfo);
        startActivityForResult(intent, GOTO_PAYPROCESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GOTO_PAYPROCESS) {
            if(resultCode == RESULT_OK) {
                Complete();
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnOk:
                isBreak = false;
                sbStatus.append("이미지를 전송을 시작합니다.").append("\n");
                tvStatus.setText(sbStatus.toString());
                SendImageToServer();
                break;
            case R.id.btnCancel:
                isBreak = true;
                finish();
                break;
        }
    }

    private long getFileInfo(String path) {
        File f = new File(path);
        if(f.exists()) {
            TotalSizeofFiles += f.length();
            return f.length();
        }
        return 0;
    }

    private void addStatusString(String string) {
        sbStatus.append(string).append("\n");
        tvStatus.setText(sbStatus.toString());
    }
    private void initLayout() {
        tvSize = (TextView)findViewById(R.id.tvImageSize);
        tvStatus = (TextView)findViewById(R.id.tvStatus);

        btnSend = (Button)findViewById(R.id.btnOk);
        btnSend.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        btnCancel.setEnabled(false);
        btnCancel.setVisibility(View.GONE);

        iBtnOrder = (ImageButton) findViewById(R.id.btnOrder);
        iBtnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCompleteTransfer)
                    GoToPayProcess();
            }
        });
        iBtnPre = (ImageButton) findViewById(R.id.btnPre);
        iBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addStatusString("전송 준비중입니다.");
    }
    boolean isBreak = false;
    private void SendImageToServer() {
        String[] params = new String[]{
                ftpURL,ftpPort, ftpId, ftpPw, ServerPath
        };
        FtpTransferTask ftpTransferTask = new FtpTransferTask();
        ftpTransferTask.execute(params);
    }
    @Override
    public void makeParams() {
        alParam.clear();
    }
    String ftpURL= "book208.fotokids.co.kr";
    String ftpId = "mobile_photo";
    String ftpPw = "mobile_photo1030";
    String ftpPort = "8081";

    private boolean parseInfo(String json) {
        try{
            JSONObject j = new JSONObject(json);
            if(j.getString("RESULT").equals("SUCCESS")) {
                ftpURL =  j.getString("FTP_URL");
                ftpId =  j.getString("FTP_ID");
                ftpPw =  j.getString("FTP_PSWD");
                ftpPort =  j.getString("FTP_PORT");
                return true;
            } else {
                return false;
            }
        }catch(JSONException j) {
            Log.d(TAG, "JSON Exception " + j.getMessage());
            return true;
        }
    }
    @Override
    public void parseResult(String result) {
        if(result.startsWith("SUCCESS:")){
            if(result.indexOf("{") > 0) {
                String r = result.substring(result.indexOf("{"));
                if( r.length() > 0) {
                    //Log.d(TAG, "Result(JSON):" + r);
                    parseInfo(r);
                    StartimageProcss();
                }
            }
        }


    }

    private class FtpTransferTask extends AsyncTask<String , Integer , String> {
        FTPClient mFtpClient;
        boolean status = false;

        public void DisconnectFtp(){
            try {
                mFtpClient.disconnect();
            }catch(Exception e){

            }
        }
        private String makeServerPath(FTPClient ftpClient) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            DateFormat timeFormat = new SimpleDateFormat("HHmmss");
            Date date = new Date();


            String folder = agent_name + "_" + user_name + "_" + timeFormat.format(date);

            try {
                ftpClient.makeDirectory("/" + dateFormat.format(date));
                ftpClient.makeDirectory("/" + dateFormat.format(date) + "/" + albumTitle);
                ftpClient.makeDirectory("/" + dateFormat.format(date) + "/" + albumTitle + "/" + folder + "/");

            } catch (IOException ie){

            }
            return "/" + dateFormat.format(date) + "/" + albumTitle + "/" + folder + "/";
        }

        public boolean uploadFile(FTPClient ftpClient, String localFile, String serverPath,  String fileName) {
            try {

                //Log.d(TAG, "Uploading  Local:" + localFile + " Server Path:" + serverPath + fileName);

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                File f = new File(localFile);

                FileInputStream srcFileStream = new FileInputStream(f);

                boolean status = ftpClient.storeFile(serverPath + fileName, srcFileStream);

                //Log.e("Status", String.valueOf(status));

                srcFileStream.close();

                return status;

            } catch (Exception e) {
                e.printStackTrace();

            }
            return false;
        }

        @Override
        protected void onPostExecute(String s) {
            DisconnectFtp();
            btnSend.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            int iSuccess = 0;
            for(int i = 0; i < alSendFileInfo.size(); i++){
                if(alSendFileInfo.get(i).isComplete) {
                    iSuccess++;
                }
            }
            if(alSendFileInfo.size() == iSuccess) {
                btnSend.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                isCompleteTransfer = true;
                addStatusString("이미지 " + iSuccess + "개의 전송이 완료 되었습니다.");
                addStatusString("상단의 주문 버튼을 눌러주세요.");
            } else
                addStatusString("이미지 " + iSuccess + "개의 전송이 완료 되었습니다.");

            super.onPostExecute(s);
        }

        @Override
        protected void onPreExecute() {
            btnSend.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            //Log.d(TAG, "onProgressUpdate :" + values[0]);

            if(alSendFileInfo.get(values[0]).isComplete) {
                addStatusString((values[0] + 1) + " 번째 이미지를 전송하였습니다.");
            }else {
                addStatusString((values[0] + 1) + " 번째 이미지를 전송이 실패하였습니다.");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mFtpClient = new FTPClient();

                FTPClientConfig config = new FTPClientConfig();

                config.setServerLanguageCode("ko");
                config.setDefaultDateFormatStr("MMM d yyyy"); // IIS(set unix) directory type
                config.setRecentDateFormatStr("MMM d HH:mm");

                mFtpClient.configure(config);
                mFtpClient.setControlEncoding("euc-kr");

                mFtpClient.setConnectTimeout(10 * 1000);
                mFtpClient.connect(InetAddress.getByName(params[0]), Integer.parseInt(params[1]));
                status = mFtpClient.login(params[2], params[3]);

                //Log.d(TAG, "isFTPConnected " + String.valueOf(status));

                if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                    mFtpClient.setFileType(FTP.ASCII_FILE_TYPE);
                    //mFtpClient.enterLocalPassiveMode();
                    mFtpClient.enterLocalActiveMode();

                    //Log.d(TAG, "FTPConnected :" + mFtpClient.printWorkingDirectory());

                    FTPFile[] mFileArray = mFtpClient.listFiles();
                    //Log.d("Size :", String.valueOf(mFileArray.length));
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addStatusString("연결에 실패하였습니다.");
                        }
                    });
                    return null;
                }

                String serverPath = makeServerPath(mFtpClient);

                for(int i = 0; i < alSendFileInfo.size(); i++) {
                    if(isBreak) {
                        break;
                    }
                    if(alSendFileInfo.get(i).isComplete){
                        final int num = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addStatusString((num +1) + " 번쨰 이미지는 전송완료 되었습니다.");
                            }
                        });
                    } else {
                        String imageName = agent_name + "_" + user_name + "_" + albumTitle + String.format("%03d", i) + ".jpg";

                        if(uploadFile(mFtpClient, alSendFileInfo.get(i).path, serverPath, imageName)) {
                            alSendFileInfo.get(i).isComplete = true;
                        };
                    }
                    publishProgress(i);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
