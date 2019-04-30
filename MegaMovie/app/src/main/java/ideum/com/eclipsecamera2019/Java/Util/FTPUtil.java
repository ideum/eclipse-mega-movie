package ideum.com.eclipsecamera2019.Java.Util;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by MT_User on 7/24/2017.
 */

public class FTPUtil {
    public FTPClient ftpClient;

    public FTPUtil(String ip) {
        boolean status = false;
        try {
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(10 * 1000);
            ftpClient.connect(InetAddress.getByName(ip));
            status = ftpClient.login("anonymous","braxton@ideum.com");
            Log.e("isFTPConnected",String.valueOf(status));


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean ftpUpload(String srcFilePath, String desFileName, String desDirectory) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            status = ftpClient.storeFile(desFileName,srcFileStream);
            srcFileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("FTPUtil", "upload failed: " + e);
        }
        return status;
    }


}
