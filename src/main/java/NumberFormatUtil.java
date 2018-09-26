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
        numberFormat.setMinimumFractionDigits(2);   // 保留2位小数
        percent = numberFormat.format(p3);
        return percent;
    }

}
