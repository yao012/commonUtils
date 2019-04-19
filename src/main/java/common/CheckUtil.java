package common;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:24
 */
public class CheckUtil {

    /**
     * 验证手机号
     */
    private final static Pattern VALID_PHONE_NUM = Pattern.compile("^1\\d{10}$");

    /**
     * 验证用户名
     */
    private final static Pattern VALID_USER_NAME = Pattern.compile("^[a-zA-Z]\\w{5,17}$");

    /**
     * 验证邮箱
     */
    private final static Pattern VALID_EMAIL = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    /**
     * 验证产品名
     * ^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]{2,18}+$
     *
     * (?!_)                        首位不能是下划线
     * (?!.*?_$)                    末尾不能是下划线
     * [a-zA-Z0-9_\u4e00-\u9fa5]    允许汉字,数字,字母,下划线
     * {2,18}                       长度范围2~18位
     */
    private final static Pattern VALID_APP_PRODUCT_NAME = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,18}+$");


    /**
     * 简单的验证手机号长度及首位数字为1
     * @param phone
     * @return
     */
    public static boolean isValidPhone(String phone){
        if (StringUtils.isNotBlank(phone)) {
            return VALID_PHONE_NUM.matcher(phone).matches();
        } else {
            return false;
        }
    }

    /**
     * 檢查用戶名是否符合要求,6~18位字符,第一位必须是字母,后面允许字母数字和下划线
     * @param username
     * @return
     */
    public static boolean isValidUsername(String username){
        if (StringUtils.isNotBlank(username)) {
            return VALID_USER_NAME.matcher(username).matches();
        } else {
            return false;
        }
    }


    public static boolean isValidEmail(String email){
        if(StringUtils.isNotBlank(email) && email.length() <= 128 && email.length() >= 3 &&
                VALID_EMAIL.matcher(email).matches()){
            return true;
        }
        return false;
    }

    /**
     * 验证产品名称的有效性
     * @param productName  2~18位字符,只允许汉字,字母,数字,下划线,且下划线不能做开头和结尾
     * @return
     */
    public static boolean isValidProductAPPName(String productName){
        if(StringUtils.isNotBlank(productName) && VALID_APP_PRODUCT_NAME.matcher(productName).matches()){
            return true;
        }
        return false;
    }

}
