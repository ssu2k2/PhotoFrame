package kr.pnit.mPhotoManager.Network;

public class ParamVO {
	private String KEY;
	private String VALUE;
	
	public ParamVO(){
		this.KEY="";
		this.VALUE="";
	}
	public ParamVO(String kEY, String vALUE){
		this.KEY = kEY;
		this.VALUE = vALUE;
	}
	public String getKEY() {
		return KEY;
	}
	public void setKEY(String kEY) {
		KEY = kEY;
	}
	public String getVALUE() {
		return VALUE;
	}
	public void setVALUE(String vALUE) {
		VALUE = vALUE;
	}
}
