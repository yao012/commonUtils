import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * HtppDigest的握手过程处理
 */
public class HttpDigest {
	
	private final static Logger logger = LogManager.getLogger(HttpDigest.class);

	private final static char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String md5(String text){
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(text.getBytes());
			return cvtHex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
        	logger.error(e.getMessage(), e);
		}
		return "";
	}

	public static String calcHA1(String algorithms, String username,
			String realm, String password, String nonce,
			String cnonce) {
		byte colon = ':';
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(username.getBytes());
			digest.update(colon);
			digest.update(realm.getBytes());	
			if(password != null){
				digest.update(colon);
				digest.update(password.getBytes());
			}
			byte[] HA1 = digest.digest();
			if ("md5-sess".equalsIgnoreCase(algorithms)) {
				MessageDigest sessDigest = MessageDigest.getInstance("MD5");
				sessDigest.update(HA1);
				sessDigest.update(colon);
				sessDigest.update(nonce.getBytes());
				sessDigest.update(colon);
				sessDigest.update(cnonce.getBytes());
				HA1 = sessDigest.digest();
			}
			return cvtHex(HA1);
		} catch (Exception e) {
        	logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String calcHA2(String method, String uri) {
		byte colon = ':';
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(method.getBytes());
			digest.update(colon);
			digest.update(uri.getBytes());			
			byte[] a2 = digest.digest();
			return cvtHex(a2);
		} catch (Exception e) {
        	logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String cvtHex(byte[] bin) {
		if(bin == null){
			return "";
		}
		StringBuffer stringBuf = new StringBuffer(bin.length<<1);
		for (int i = 0; i < bin.length; i++) {
			stringBuf.append(chars[(bin[i]>>4)&0x0f]);
			stringBuf.append(chars[bin[i]&0x0f]);
		}
		return stringBuf.toString();
	}


	public static String calcResponse(String HA1, String HA2, String nonce,
			String nc, String cnonce, String qop) {
		byte colon = ':';
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(HA1.getBytes());
			digest.update(colon);
			digest.update(nonce.getBytes());
			digest.update(colon);
			if (qop != null && qop.length() > 0) {
				digest.update(nc.getBytes());
				digest.update(colon);
				digest.update(cnonce.getBytes());
				digest.update(colon);
				digest.update(qop.getBytes());
				digest.update(colon);
			}
			digest.update(HA2.getBytes());
			return cvtHex(digest.digest());
		} catch (Exception e) {
        	logger.error(e.getMessage(), e);
		}
		return "";
	};

    public static String calcNonce(){
        long time = new Date().getTime()/1000 + 15;
        StringBuilder builder = new StringBuilder(32);
        builder.append(time).append("00000000");
        return builder.toString();
    }

}