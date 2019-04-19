package common;

import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;

/**
 * @author: yaozhenguo
 * @date: 2018/09/26
 * @destination:
 */
public class NumberFormatUtil {

    /**
     * 计算百分比
     *
     * @param divisor  除数
     * @param dividend 被除数
     * @return
     */
    public static String getPercent(int divisor, int dividend) {
        String percent;
        Double p3 = 0.0;
        if (dividend != 0) {
            p3 = divisor * 1.0 / dividend;
        }
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        // 保留2位小数
        numberFormat.setMinimumFractionDigits(2);
        percent = numberFormat.format(p3);
        return percent;
    }


    /**
     * 手机号的中间四位是****,例:15238075013  ==> 152****5013
     * @param phone
     * @return
     */
    public static String getFuzzyPhone (String phone){

        if(StringUtils.isBlank(phone) || !CheckUtil.isValidPhone(phone)){
            return null;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 1219776857@qq.com    ==>     121*****57@qq.com
     * @param email
     * @return
     */
    public static String getFuzzyEmail (String email){

        if(StringUtils.isBlank(email)){
            return null;
        }
        // 邮箱号很短的情况暂时不考虑
        return email.replaceAll("(.{3})(.*?)(.{2})(@.*)", "$1*****$3$4");
    }


    /**
     * 留前四位后四位,其余用*标识
     * @param productKey
     * @return
     */
    public static String getFuzzyKey (String productKey){

        if(StringUtils.isBlank(productKey)){
            return null;
        }
        return productKey.replaceAll("(\\w{4})\\w{24}(\\w{4})", "$1*****$2");
    }




}
