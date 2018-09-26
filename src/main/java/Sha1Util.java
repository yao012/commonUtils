import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Util {
	private final static Logger logger = LogManager.getLogger(Sha1Util.class);

	public final static String SALT = "pWxy58q3qikJDmpuIEJCARlY2YfjkE1z";

	public static String sha1(String text){
		if(StringUtils.isBlank(text)){
			return "";
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA1");
			digest.update(text.getBytes(StandardCharsets.UTF_8));
			return HexUtils.hexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	/**
	 * 比 sha1 更安全
	 * @param text
	 * @return
	 */
	public static String sha256(String text){
		if(StringUtils.isBlank(text)){
			return "";
		}
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes(StandardCharsets.UTF_8));
            return HexUtils.hexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
