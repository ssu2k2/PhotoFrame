package kr.pnit.mPhoto.DTO;

/**
 * Created by Yongsu on 2015-09-24.
 */
public class SendFileInfo {
    public boolean isComplete;
    public long size;
    public String path;
    public SendFileInfo(String path, long size) {
        this.size = size;
        this.path = path;
        isComplete = false;
    }
}

