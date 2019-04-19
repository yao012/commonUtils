package oss;

import com.aliyun.oss.OSSClient;

/**
 * 对阿里云提供的client进一步封装,实现AutoCloseable,避免每次调用后手动关闭
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:33
 */
public class AliOssClient extends OSSClient implements AutoCloseable{

    public AliOssClient(String endpoint, String accessKeyId, String secretAccessKey){
        super(endpoint, accessKeyId, secretAccessKey);
    }

    public void close() throws Exception {
        super.shutdown();
    }
}
