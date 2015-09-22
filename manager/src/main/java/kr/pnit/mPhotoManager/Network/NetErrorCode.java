package kr.pnit.mPhotoManager.Network;

public class NetErrorCode {
	public final static int HANDLER_NET_ERROR_CODE = 1000;
	public final static int ERROR_TIMEOUT 	= HANDLER_NET_ERROR_CODE + 1;
	public final static int ERROR_PROTOCOL 	= HANDLER_NET_ERROR_CODE + 2;
	public final static int ERROR_PARSE 	= HANDLER_NET_ERROR_CODE + 3;
	public final static int ERROR_IOEXCEP 	= HANDLER_NET_ERROR_CODE + 4;
	public final static int ERROR_URLERR 	= HANDLER_NET_ERROR_CODE + 5;
}
