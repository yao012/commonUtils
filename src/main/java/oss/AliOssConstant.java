package oss;

import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云的一些常量
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:30
 */
public class AliOssConstant {

    /**
     * accessKeyId和accessKeySecret是OSS的访问密钥，您可以在控制台上创建和查看，
     * 创建和查看访问密钥的链接地址是：https://ak-console.aliyun.com/#/。
     * 注意：accessKeyId和accessKeySecret前后都没有空格，从控制台复制时请检查并去除多余的空格。
     */
    public static final String ACCESS_KEY_ID = "***";
    public static final String ACCESS_KEY_SECRET = "****";


    /**
     * 阿里云API的外网域名,与使用的 BACKET_NAME对应的域名保持一致,在阿里控制台上找到
     * 地址:https://oss.console.aliyun.com/bucket/oss-cn-beijing/wzj-test/overview
     */
    public static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com";

    /**
     * 阿里云API的bucket名称(测试),需要与阿里云控制台设置的名称保持一致
     */
    public static final String TEST_BUCKET_NAME = "***";

    /**
     * 阿里云API的bucket名称(生产环境)
     */
    public static final String PROD_BUCKET_NAME = "***";

    /**
     * 阿里云API的文件夹名称;
     * 注意:
     *  OSS是没有文件夹这个概念的,所有元素都是以Object来存储。
     *  创建模拟文件夹本质上来说是创建了一个size为0的Object。
     *  对于这个Object可以上传下载，只是控制台会对以”/“结尾的Object以文件夹的方式展示
     *
     *
     */
    public static final String IOT_PRODUCT_FOLDER="***/";

    /**
     * 防盗链白名单
     */
    public static final List<String> REFERER_LIST = new ArrayList<String>(){{
        // 本地调试
        add("http://localhost*");
        // 阿里控制台
        add("https://*.console.aliyun.com");
    }};

}
