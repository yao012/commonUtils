package oss;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:44
 */
public class AliOssClientUtils {

    private static final Logger logger = LogManager.getLogger(AliOssClientUtils.class);


    /**
     * 创建存储空间
     *
     * @param ossClient
     * @param prod      当前是否生产环境
     * @return 新创建的存储空间的名称
     */
    public static String createBucketName(OSSClient ossClient, boolean prod) {
        if (!ossClient.doesBucketExist(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME)) {
            Bucket bucket = ossClient.createBucket(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME);
            return bucket.getName();
        }
        return prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME;
    }

    /**
     * 设置防盗链
     *
     * @param ossClient
     * @param prod
     */
    public static void setReferer(OSSClient ossClient, boolean prod) {
        // 设置存储空间Referer列表。设为true表示Referer字段允许为空。
        BucketReferer br = new BucketReferer(false, AliOssConstant.REFERER_LIST);
        ossClient.setBucketReferer(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, br);
    }

    /**
     * 获取防盗链
     *
     * @param ossClient
     * @param prod
     */
    public static List<String> getReferer(OSSClient ossClient, boolean prod) {
        // 设置存储空间Referer列表。设为true表示Referer字段允许为空。
        new BucketReferer(false, AliOssConstant.REFERER_LIST);
        BucketReferer br = ossClient.getBucketReferer(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME);
        return br.getRefererList();
    }

    /**
     * 清空防盗链
     *
     * @param ossClient
     * @param prod
     */
    public static void clearReferer(OSSClient ossClient, boolean prod) {
        // 防盗链不能直接清空，需要新建一个允许空Referer的规则来覆盖之前的规则。
        BucketReferer br = new BucketReferer();
        ossClient.setBucketReferer(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, br);
    }

    /**
     * 创建文件夹
     *
     * @param ossClient
     * @param prod      当前是否生产环境
     * @param folder    必须以/结尾
     * @return
     */
    public String createFolder(OSSClient ossClient, String folder, boolean prod) {
        if (StringUtils.isBlank(folder) || !folder.endsWith("/")) {
            throw new RuntimeException("the folder name must end with '/' ,Please check and try again");
        }

        if (!ossClient.doesObjectExist(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, folder)) {
            ossClient.putObject(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, folder, new ByteArrayInputStream(new byte[0]));
            OSSObject ossObject = ossClient.getObject(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, folder);
            String fileDir = ossObject.getKey();
            return fileDir;
        }

        return folder;
    }

    /**
     * 慎用此操作(删除存储空间前必须先删除文件和未完成的分片上传任务,
     * 否则会抛出异常'com.aliyun.oss.OSSException: The bucket you tried to delete is not empty.')
     * 删除存储空间
     *
     * @param ossClient
     * @param prod      当前是否生产环境
     */
    public static boolean deleteBucket(OSSClient ossClient, boolean prod) {
        try {
            ossClient.deleteBucket(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME);
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * 删除文件夹下的指定文件
     *
     * @param ossClient
     * @param prod      当前是否生产环境
     * @param folder
     * @param key
     */
    public static void deleteFile(OSSClient ossClient, String folder, String key, boolean prod) {
        if (StringUtils.isBlank(folder) || !folder.endsWith("/")) {
            throw new RuntimeException("the folder name must end with '/' ,Please check and try again");
        }
        ossClient.deleteObject(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME, folder + key);
    }

    /**
     * 批量删除文件夹下的文件
     *
     * @param ossClient
     * @param folder
     * @param keys
     * @param prod
     */
    public static List<String> deleteFiles(OSSClient ossClient, String folder, List<String> keys, boolean prod) {
        // 删除操作的请求对象
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(prod ? AliOssConstant.PROD_BUCKET_NAME : AliOssConstant.TEST_BUCKET_NAME);
        // 设置需要删除的key集合
        deleteObjectsRequest.withKeys(keys);
        // 设置返回模式为简单模式,返回删除失败的文件列表;如果是true表示详细模式,返回删除成功的文件列表
        deleteObjectsRequest.withQuiet(true);
        DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
        // 删除的结果  详细模式下为删除成功的;简单模式下为删除失败的文件列表。
        List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
        return deletedObjects;
    }

    /**
     * 上传对象文件到指定存储空间的指定文件夹下并返回访问链接
     *
     * @param ossClient
     * @param file
     * @param prod
     * @param SSL
     * @return
     */
    public static String uploadFileToOSS(OSSClient ossClient, File file, boolean prod, boolean SSL) {
        return uploadObjectToOSS(ossClient, file, null, prod, SSL, false);
    }

    /**
     * 上传对象文件到指定存储空间的指定文件夹下并返回访问链接
     *
     * @param ossClient
     * @param multipartFile
     * @param prod
     * @param SSL
     * @return
     */
    public static String uploadMultipartFileToOSS(OSSClient ossClient, MultipartFile multipartFile, boolean prod, boolean SSL) {
        return uploadObjectToOSS(ossClient, null, multipartFile, prod, SSL, true);
    }


    /**
     * 上传对象文件到指定存储空间的指定文件夹下并返回访问链接
     *
     * @param ossClient
     * @param file
     * @param prod      当前是否生产环境
     * @param SSL       返回Https://xxx 还是http://xxx 由该参数决定
     * @return
     */
    private static String uploadObjectToOSS(OSSClient ossClient, File file, MultipartFile multipartFile,
                                            boolean prod, boolean SSL, boolean isMultipart) {

        // 文件名
        String fileName = isMultipart ? multipartFile.getOriginalFilename() : file.getName();
        // 文件大小
        long fileSize = isMultipart ? multipartFile.getSize() : file.length();

        String accessUrl = null;
        // 创建上传对象的metaData
        ObjectMetadata metadata = new ObjectMetadata();
        // 指定该Object被下载时的网页的缓存行为
        metadata.setCacheControl("no-cache");
        // 指定该Object下设置header
        metadata.setHeader("Pragma", "no-cache");
        // 指定该Object被下载时的内容编码格式
        metadata.setContentEncoding(StandardCharsets.UTF_8.name());
        //文件的MIME，定义文件的类型及网页编码，决定浏览器将以什么形式、什么编码读取文件。如果用户没有指定则根据Key或文件名的扩展名生成，
        //如果没有扩展名则填默认值application/octet-stream
        metadata.setContentType(getContentType(fileName));
        //指定该Object被下载时的名称（指示MINME用户代理如何显示附加的文件，打开或下载，及文件名称）
        metadata.setContentDisposition("filename/filesize=" + fileName + "/" + fileSize + "Byte.");
        String ossKey = null;
        try {
            ossKey = AliOssConstant.IOT_PRODUCT_FOLDER + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            if (isMultipart) {
                try (InputStream is = multipartFile.getInputStream()) {
                    metadata.setContentLength(is.available());
                    //上传文件   (上传文件流的形式)
                    PutObjectResult putResult = ossClient.putObject(prod ? AliOssConstant.PROD_BUCKET_NAME :
                            AliOssConstant.TEST_BUCKET_NAME, ossKey, is, metadata);
                    if (putResult != null) {
                        accessUrl = (SSL ? "https://" : "http://") + (prod ? AliOssConstant.PROD_BUCKET_NAME :
                                AliOssConstant.TEST_BUCKET_NAME) + "." + AliOssConstant.ENDPOINT + "/" + ossKey;
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                try (InputStream is = new FileInputStream(file)) {
                    // 上传文件的长度
                    metadata.setContentLength(is.available());
                    //上传文件   (上传文件流的形式)
                    PutObjectResult putResult = ossClient.putObject(prod ? AliOssConstant.PROD_BUCKET_NAME :
                            AliOssConstant.TEST_BUCKET_NAME, ossKey, is, metadata);
                    if (putResult != null) {
                        accessUrl = (SSL ? "https://" : "Http://") + (prod ? AliOssConstant.PROD_BUCKET_NAME :
                                AliOssConstant.TEST_BUCKET_NAME) + "." + AliOssConstant.ENDPOINT + "/" + ossKey;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return accessUrl;
    }

    /**
     * 获取内容类型
     *
     * @param fileName
     * @return
     */
    public static String getContentType(String fileName) {
        //文件的后缀名
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        switch (fileExtension) {
            case ".bmp":
                return ContentType.IMAGE_BMP.value;
            case ".gif":
                return ContentType.IMAGE_GIF.value;
            case ".jpg":
                return ContentType.IMAGE_JPG.value;
            case ".png":
                return ContentType.IMAGE_PNG.value;
            case ".html":
                return ContentType.TEXT_HTML.value;
            case ".txt":
                return ContentType.TEXT_PLAIN.value;
            case ".xml":
                return ContentType.TEXT_XML.value;
            case ".vsd":
                return ContentType.APPLICATION_VND_VISIO.value;
            case ".ppt":
            case ".pptx":
                return ContentType.APPLICATION_VND_MS_POWERPOINT.value;
            case ".doc":
            case ".docx":
                return ContentType.APPLICATION_MS_WORD.value;
            default:
                return ContentType.IMAGE_JPEG.value;
        }
    }


    /**
     * 部分内容类型
     */
    public enum ContentType {
        IMAGE_BMP("image/bmp"),
        IMAGE_GIF("image/gif"),
        IMAGE_JPEG("image/jpeg"),
        IMAGE_JPG("image/jpg"),
        IMAGE_PNG("image/png"),
        TEXT_HTML("text/html"),
        TEXT_PLAIN("text/plain"),
        TEXT_XML("text/xml"),
        APPLICATION_VND_VISIO("application/vnd.visio"),
        APPLICATION_VND_MS_POWERPOINT("application/vnd.ms-powerpoint"),
        APPLICATION_MS_WORD("application/msword");

        public final String value;

        ContentType(String contentType) {
            this.value = contentType;
        }
    }

    public static void main(String[] args) {
        try (AliOssClient ossClient = AliOssClientFactory.getOSSClient(false)) {
            //初始化OSSClient
            File files = new File("F:\\testOSS\\" + 1 + ".jpg");
            String accessUrl = AliOssClientUtils.uploadFileToOSS(ossClient, files, false, true);
            System.out.println("上传后的文件访问链接:" + accessUrl);
            List<String> list = getReferer(ossClient, false);
            System.out.println(JSON.toJSONString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
