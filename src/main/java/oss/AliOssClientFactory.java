package oss;

import com.aliyun.oss.model.BucketReferer;

import java.io.ByteArrayInputStream;

/**
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:51
 */
public class AliOssClientFactory {

    /**
     * 获取OSSClient
     *
     * @return OSSClient
     */
    public static AliOssClient getOSSClient(boolean prod) {
        AliOssClient ossClientWrapper = new AliOssClient(
                AliOssConstant.ENDPOINT,
                AliOssConstant.ACCESS_KEY_ID,
                AliOssConstant.ACCESS_KEY_SECRET
        );
        init(ossClientWrapper, prod);
        return ossClientWrapper;
    }

    /**
     * 初始化ossClient   创建存储空间及文件夹,增加防盗链功能;
     * 参照页必须是 https://*.ecamzone.cc/ 才允许访问由OSSClient 上传的文件;
     * 防止有人盗链导致的额外费用支出
     *
     * @param ossClientWrapper
     * @param prod
     */
    private static void init(AliOssClient ossClientWrapper, boolean prod) {
        if (!ossClientWrapper.doesBucketExist(prod ? AliOssConstant.PROD_BUCKET_NAME :
                AliOssConstant.TEST_BUCKET_NAME)) {
            ossClientWrapper.createBucket(prod ? AliOssConstant.PROD_BUCKET_NAME :
                    AliOssConstant.TEST_BUCKET_NAME);
        }
        if (!ossClientWrapper.doesObjectExist(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME,
                AliOssConstant.IOT_PRODUCT_FOLDER)) {
            ossClientWrapper.putObject(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME,
                    AliOssConstant.IOT_PRODUCT_FOLDER,
                    new ByteArrayInputStream(new byte[0])
            );
            ossClientWrapper.getObject(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME,
                    AliOssConstant.IOT_PRODUCT_FOLDER);
        }

        // 设置存储空间Referer列表。设为true表示Referer字段允许为空。
        BucketReferer br = new BucketReferer(false, AliOssConstant.REFERER_LIST);
        ossClientWrapper.setBucketReferer(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, br);

    }


}
