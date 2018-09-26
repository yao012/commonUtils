import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.TimeZone;

public class TimeZoneUtils {

    private final static Logger logger = LogManager.getLogger(TimeZoneUtils.class);

    /** 中国+8时区的偏移 */
    public final static long TZ_CHINA_8_OFFSET = 28800000L;
    public final static long MS_OF_DAY = 24L*60L*60L*1000L;

    /** 如果时区中没有符号信息, 则使用默认符号, 1表示正常, -1表示负号, 其它值未定义 */
    private static int defaultSign = 1;

    private static class Holder{
        static final String systemTimeZone;
        static final Date DATE_1970;
        static {
            TimeZone timezone = TimeZone.getDefault();
            int offset = timezone.getRawOffset();
            DATE_1970 = new Date(offset);
            systemTimeZone = offsetToNormalizedDigit(offset, "+08:00");
        }
    }


	/**
	 * 获取当前时区的utc偏移, 比如北京时区, 返回 "+8:0"
	 * @return
	 */
    public static String getSystemDigitTimezone(){
        return Holder.systemTimeZone;
    }

    public static Date getDate1970(){
        return Holder.DATE_1970;
    }

    /**  */
    public static void setDefaultSign(int sign){
        if(sign != 1 && sign != -1){
            return;
        }
        defaultSign = sign;
    }

    /**
     * 将字符串形式的时区"+08:00"转换成毫秒偏移量
     * 合法的格式 "8", "+8", "+08", "+8:0"类似
     * @param tz
     * @return 如果出错时,返回defaultValue,
     * 正常情况下返回 -12L*60*60*1000 ~ +12L*60*60*1000之间的数据
     */
    public static long getTimeZoneOffset(String tz, long defaultValue){
        if(tz == null){
            return defaultValue;
        }
        int length = tz.length();
        //符号标记, 0表示未设定, 1表示正数, -1表示负数
        int sig = 0;

        //负数表示未设定
        int hour = -1;
        int minute = -1;
        boolean isSettingHour = true;

        for(int i=0; i<length; i++){
            final char c = tz.charAt(i);
            if(c == '+'){
                if(sig != 0){ //已经设置过符号了
                    logger.error("siganle setted: {}", c);
                    return defaultValue;
                }
                sig = 1;
            }else if(c == '-'){
                if(sig != 0){ //遇到错误
                    logger.error("siganle setted: {}", c);
                    return defaultValue;
                }
                sig = -1;
            }else if(c == ':'){
                if(hour == -1){ //还未设置小时
                    logger.error("no hour");
                    return defaultValue;
                }
                isSettingHour = false;
            }else if(c >='0' && c <='9'){
                if(isSettingHour){
                    if(hour == -1){
                        hour = 0;
                    }
                    hour = hour*10+(c - '0');
                }else{
                    if(minute == -1){
                        minute = 0;
                    }
                    minute = minute*10+(c - '0');
                }
            }else if(c ==' '){
                //跳过空格
                continue;
            }else{ //非法字符
                if(hour == -1){
                    //抛弃开始位置的非法字符
                    continue;
                }else{
                    //抛弃末尾的非法字符
                    break;
                }
            }
        }

        if(hour == -1){
            return defaultValue;
        }
        if(minute == -1){
            minute = 0;
        }

        if(sig == 0){
            sig = defaultSign;
        }

        long ms =  (hour *60L*60*1000 + minute*60L*1000)*sig;
        if(ms < -12L*60*60*1000 || ms > 12L*60*60*1000){
            logger.error("tz out of range: {}", ms);
            return defaultValue;
        }else{
            return ms;
        }
    }

    /**
     * 将offset(毫秒)形式的时区utc偏移, 转换成类似于"+HH:MM"的形式
     * @param offset
     * @param defaultZone
     * @return
     */
    public static String offsetToNormalizedDigit(int offset, String defaultZone){
        //超出范围
        if(offset < -12*60*60*1000 || offset > 12*60*60*1000){
            return defaultZone;
        }
        char sig = offset<0?'-':'+';
        offset = Math.abs(offset);
        int hour = (int) (offset/(60*60*1000));
        int minute = (int) (offset%(60*60*1000) / (60*1000));
        return String.format("%c%d:%d", sig, hour, minute);
    }


    /**
     * 检查字符串是否符合 Sign Hour:Minute的形式, 例如"+8:0"
     * @param tz
     * @return
     */
    public static boolean checkNormalizedDigitFormat(String tz){
        int length = tz.length();
        if(length==0){
            return false;
        }
        int index = 0;
        char sig = tz.charAt(index);

        //检查符号位
        if(sig != '+' && sig != '-'){
            //logger.error("first char should be +, or -");
            return false;
        }

        //接下来是必须是数字
        index++;
        if(index >= length){
            //logger.error("string too short, need hour");
            return false;
        }
        char hour1 = tz.charAt(index);
        if(hour1<'0' || hour1>'9'){
            //logger.error("hour1 should be number");
            return false;
        }

        //接下来必须是数字或是:
        index++;
        if(index >= length){
            //logger.error("string too short, need colon");
            return false;
        }
        char colon = tz.charAt(index);
        if(colon>='0' &&  colon<='9'){
            //仍然是小时位, 再偏移
            index++;
            if(index >= length){
                //logger.error("string too short, need colon");
                return false;
            }
            colon = tz.charAt(index);
        }

        if(colon != ':'){

            return false;
        }

        //剩下的应当全部是数字, 最多两位.
        index++;
        int remain = length - index;
        if(remain<=0 || remain > 2){
            return false;
        }
        for(; index<length; index++){
            char num = tz.charAt(index);
            if(num<'0' || num>'9'){
                return false;
            }
        }

        return true;
    }
	
}
