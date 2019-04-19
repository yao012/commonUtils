package common.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.function.Supplier;

/**
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:25
 */
public class CipherUtil {

    private final static Logger logger = LogManager.getLogger(CipherUtil.class);

    private static RSAPrivateKey privateKey;

    public static void setPrivateKey(final RSAPrivateKey privateKey) {
        CipherUtil.privateKey = privateKey;
    }

    /**
     * <a href="https://github.com/travist/jsencrypt/issues/79">如何加密</a>
     *
     * <p>{@link Cipher} is not thread-safe.
     */
    private static ThreadLocal<Cipher> localCipher = ThreadLocal.withInitial(new Supplier<Cipher>() {
        @Override
        public Cipher get() {
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return cipher;
            } catch (Exception e) {
                logger.error("cipher failed to load", e);
            }

            return null;
        }
    });

    public static String decrypt(String encrypted) throws Exception {
        Cipher cipher = localCipher.get();
        final byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
