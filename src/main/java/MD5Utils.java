import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    private final static Logger logger = LogManager.getLogger(MD5Utils.class);

    public static String md5(File file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");

            byte[] bytes = new byte[1024];
            int readLen;
            FileInputStream in = new FileInputStream(file);
            try {
                while ((readLen = in.read(bytes)) > 0) {
                    digest.update(bytes, 0, readLen);
                }
            } catch (Exception ex) {

            } finally {
                in.close();
            }
            return HexUtils.hexString(digest.digest());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }


    public static String md5(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");

            digest.update(text.getBytes(StandardCharsets.UTF_8));
            return HexUtils.hexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }


    public static String md5(InputStream inputStream) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");

            byte[] bytes = new byte[1024];
            int readLen;
            try {
                while ((readLen = inputStream.read(bytes)) > 0) {
                    digest.update(bytes, 0, readLen);
                }
            } catch (Exception ex) {

            } finally {
                inputStream.close();
            }
            return HexUtils.hexString(digest.digest());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

}
