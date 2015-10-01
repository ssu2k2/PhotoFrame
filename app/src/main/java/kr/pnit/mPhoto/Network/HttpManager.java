package kr.pnit.mPhoto.Network;

import android.net.ParseException;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class HttpManager {
	private final static String CLASSNAME = "HttpManager";
	private List<NameValuePair> paramList;
	private static int iTimeoutValue;

	/**
	 * HttpManager
	 */
	public HttpManager() {
		iTimeoutValue = 10000;
	}

	public static int getiTimeoutValue() {
		return iTimeoutValue;
	}

	public static void setiTimeoutValue(int value) {
		iTimeoutValue = value;
	}

	/**
	 * Http
	 * 
	 * @param serverUrl
	 * @param alParam
	 * @param callback
	 * @param handler
	 * @throws Exception
	 */
	public void executeHttpWithCallback(String serverUrl, String method,
			ArrayList<ParamVO> alParam, NetCallback callback, Handler handler)
			throws Exception {
		if (method.equals("POST")) {
			paramList = createParametertList(alParam);
			callback.NetCallBack(sendPostData(serverUrl, paramList), handler);
		} else if (method.equals("GET")) {
			callback.NetCallBack(sendGetData(serverUrl, alParam), handler);
		} else {
			Log.d(CLASSNAME, "executeHttp : Method Error " + method);
		}
	}

    public String executeHttp(String serverUrl, String method, ArrayList<ParamVO> alParam)
            throws Exception {
        if (method.equals("POST")) {
            paramList = createParametertList(alParam);
            return sendPostData(serverUrl, paramList);
        } else if (method.equals("GET")) {
            return sendGetData(serverUrl, alParam);
        } else {
            Log.d(CLASSNAME, "executeHttp : Method Error " + method);
            return "ERROR : Method Error" + method;
        }
    }
	/**
	 * ��Ʈ��ũ �Ķ��Ÿ �� �Լ�
	 * 
	 * @param alParam
	 * @return
	 */
	private List<NameValuePair> createParametertList(ArrayList<ParamVO> alParam) {
		List<NameValuePair> parameterList = null;
		parameterList = new ArrayList<NameValuePair>();
		for (ParamVO param : alParam) {
			parameterList.add(new BasicNameValuePair(param.getKEY(), param
					.getVALUE()));
//			Log.d(CLASSNAME,
//					"PARAMS :" + param.getKEY() + " " + param.getVALUE());
		}
		return parameterList;
	}

	/**
	 * Post ����� ������ ��û
	 * 
	 * @param serverUrl
	 * @param parameterList
	 * @return
	 */
	public String sendPostData(String serverUrl,
			List<NameValuePair> parameterList) {
		String response = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(serverUrl);
		UrlEncodedFormEntity urlEncode = null;

		// Time Out : 10 Sec
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, iTimeoutValue);
		HttpConnectionParams.setSoTimeout(params, iTimeoutValue);

		try {

			urlEncode = new UrlEncodedFormEntity(parameterList, HTTP.UTF_8);
			httpPost.setEntity(urlEncode);
			HttpResponse httpResponse = null;
			HttpEntity httpEntity = null;
			httpResponse = httpClient.execute(httpPost);
			httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				String entityData = EntityUtils.toString(httpEntity);
				response = URLDecoder.decode(entityData.replace("%", "%25"),
                        HTTP.UTF_8);
			}
			response = "SUCCESS:" + response;
		} catch(SocketTimeoutException st){
			st.printStackTrace();
			Log.d(CLASSNAME, "ERROR: " + st.toString());
			response = "ERROR:" + NetErrorCode.ERROR_TIMEOUT +":";
		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + cpe.toString());
			response = "ERROR:" + NetErrorCode.ERROR_PROTOCOL +":";
		} catch (ParseException pe) {
			pe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + pe.toString());
			response = "ERROR:" + NetErrorCode.ERROR_PARSE +":";
		} catch (IOException ioe) {
			ioe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + ioe.toString());
			response = "ERROR:" + NetErrorCode.ERROR_IOEXCEP +":";
		}

		return response;
	}

	/**
	 * Get
	 * 
	 * @param serverUrl
	 * @param alParam
	 * @return
	 */
	public String sendGetData(String serverUrl, ArrayList<ParamVO> alParam) {
		String result = null;

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		HttpEntity httpEntity = null;
		StringBuilder sb = new StringBuilder();
		sb.append(serverUrl);
        if(!serverUrl.endsWith("?")) sb.append("?");
		if (alParam != null) {
			for (int i = 0; i < alParam.size(); i++) {
				// sb.append(alParam.get(i).getKEY()).append("=").append(URLEncoder.encode(alParam.get(i).getVALUE()));
				sb.append(alParam.get(i).getKEY()).append("=")
						.append(alParam.get(i).getVALUE());
                //Log.d(CLASSNAME, "PARAMS :" + alParam.get(i).getKEY() + " " + alParam.get(i).getVALUE());
				if ((alParam.size() - 1) != i)
					sb.append("&");
			}
		}
		try {

            String url = sb.toString().replaceAll(" ", "%20");
			Log.d(CLASSNAME, "URL:" + url);
			request.setURI(new URI(url));
			HttpResponse httpResponse = client.execute(request);
			httpEntity = httpResponse.getEntity();

			if (httpEntity != null) {
				result = EntityUtils.toString(httpEntity);
                //Log.d(CLASSNAME, "RESULT:" + result);
			}
			result = "SUCCESS:" + result;
			
		} catch(SocketTimeoutException st){
			st.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + st.toString());
			result = "ERROR:" + NetErrorCode.ERROR_TIMEOUT +":";
		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + cpe.toString());
			result = "ERROR:" + NetErrorCode.ERROR_PROTOCOL +":";
		} catch (ParseException pe) {
			pe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + pe.toString());
			result = "ERROR:" + NetErrorCode.ERROR_PARSE +":";
		} catch (IOException ioe) {
			ioe.printStackTrace();
            Log.d(CLASSNAME, "ERROR: " + ioe.toString());
			result = "ERROR:" + NetErrorCode.ERROR_IOEXCEP +":";
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            Log.d(CLASSNAME, "ERROR:" + e.toString());
			result = "ERROR:" + NetErrorCode.ERROR_URLERR +":";
		}
		return result;
	}

}
