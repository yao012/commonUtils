package common;

import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * 滑块验证码的生成
 *
 * @author : zhenguo.yao
 * @date : 2019/4/19 0019 14:18
 */
public class SliderCodeUtil {

    /**
     * 源文件宽度
     */
    private static final int ORI_WIDTH = 590;
    /**
     * 源文件高度
     */
    private static final int ORI_HEIGHT = 360;
    /**
     * 模板图宽度
     */
    private int WIDTH;
    /**
     * 模板图高度
     */
    private int HEIGHT;


    /**
     * 根据模板切图
     *
     * @param templateFile
     * @param targetFile
     * @param templateType
     * @param targetType
     * @return
     * @throws Exception
     */
    public Map<String, byte[]> pictureTemplatesCut(File templateFile, File targetFile, String templateType,
                                                   String targetType, int X, int Y) throws Exception {
        Map<String, byte[]> pictureMap = new HashMap<>();
        // 文件类型
        String templateFiletype = templateType;
        String oriFiletype = targetType;
        if (StringUtils.isEmpty(templateFiletype) || StringUtils.isEmpty(oriFiletype)) {
            throw new RuntimeException("file type is empty");
        }
        // 源文件流
        File Orifile = targetFile;
        try (InputStream oriis = new FileInputStream(Orifile)) {
            // 模板图
            BufferedImage imageTemplate = ImageIO.read(templateFile);
            WIDTH = imageTemplate.getWidth();
            HEIGHT = imageTemplate.getHeight();
            // 最终图像
            BufferedImage newImage = new BufferedImage(WIDTH, HEIGHT, imageTemplate.getType());
            Graphics2D graphics = newImage.createGraphics();
            graphics.setBackground(Color.white);

            int bold = 5;
            // 获取感兴趣的目标区域
            BufferedImage targetImageNoDeal = getTargetArea(X, Y, WIDTH, HEIGHT, oriis, oriFiletype);

            // 根据模板图片抠图
            newImage = DealCutPictureByTemplate(targetImageNoDeal, imageTemplate, newImage);

            // 设置“抗锯齿”的属性
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setStroke(new BasicStroke(bold, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            graphics.drawImage(newImage, 0, 0, null);
            graphics.dispose();

            // 新建流。
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                //利用ImageIO类提供的write方法，将bi以png图片的数据模式写入流。
                ImageIO.write(newImage, "png", os);
                byte[] newImages = os.toByteArray();
                pictureMap.put("newImage", newImages);

                // 源图生成遮罩
                BufferedImage oriImage = ImageIO.read(Orifile);
                // 获取抠图后的原图
                byte[] oriCopyImages = DealOriPictureByTemplate(oriImage, imageTemplate, X, Y);
                pictureMap.put("oriCopyImage", oriCopyImages);
            }
            return pictureMap;
        }
    }

    /**
     * 抠图后原图生成
     *
     * @param oriImage
     * @param templateImage
     * @param x
     * @param y
     * @return
     * @throws Exception
     */
    private byte[] DealOriPictureByTemplate(BufferedImage oriImage, BufferedImage templateImage, int x,
                                            int y) throws Exception {

        // 源文件备份图像矩阵 支持alpha通道的rgb图像
        BufferedImage ori_copy_image = new BufferedImage(oriImage.getWidth(), oriImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        // 源文件图像矩阵
        int[][] oriImageData = getData(oriImage);
        // 模板图像矩阵
        int[][] templateImageData = getData(templateImage);

        //copy 源图做不透明处理
        for (int i = 0; i < oriImageData.length; i++) {
            for (int j = 0; j < oriImageData[0].length; j++) {
                if (i < oriImage.getWidth() && j < oriImage.getHeight()) {
                    int rgb = oriImage.getRGB(i, j);
                    int r = (0xff & rgb);
                    int g = (0xff & (rgb >> 8));
                    int b = (0xff & (rgb >> 16));
                    //无透明处理
                    rgb = r + (g << 8) + (b << 16) + (255 << 24);
                    ori_copy_image.setRGB(i, j, rgb);
                }
            }
        }

        for (int i = 0; i < templateImageData.length; i++) {
            for (int j = 0; j < templateImageData[0].length; j++) {
                int rgb = templateImage.getRGB(i, j);
                //对源文件备份图像(x+i,y+j)坐标点进行透明处理
                if (rgb != 16777215 && rgb <= 0) {
                    if (x + i < ori_copy_image.getWidth() && y + j < ori_copy_image.getHeight()) {
                        int rgb_ori = ori_copy_image.getRGB(x + i, y + j);
                        int r = (0xff & rgb_ori);
                        int g = (0xff & (rgb_ori >> 8));
                        int b = (0xff & (rgb_ori >> 16));
                        rgb_ori = r + (g << 8) + (b << 16) + (150 << 24);
                        ori_copy_image.setRGB(x + i, y + j, rgb_ori);
                    }
                } else {
                    //do nothing
                }
            }
        }
        byte[] b;
        //新建流。
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            //利用ImageIO类提供的write方法，将bi以png图片的数据模式写入流。
            ImageIO.write(ori_copy_image, "png", os);
            //从流中获取数据数组。
            b = os.toByteArray();
        }
        return b;
    }


    /**
     * 根据模板图片抠图
     *
     * @param oriImage
     * @param templateImage
     * @return
     */

    private BufferedImage DealCutPictureByTemplate(BufferedImage oriImage, BufferedImage templateImage,
                                                   BufferedImage targetImage) throws Exception {
        // 源文件图像矩阵
        int[][] oriImageData = getData(oriImage);
        // 模板图像矩阵
        int[][] templateImageData = getData(templateImage);

        // 模板图像宽度
        for (int i = 0; i < templateImageData.length; i++) {
            // 模板图片高度
            for (int j = 0; j < templateImageData[0].length; j++) {
                // 如果模板图像当前像素点不是白色 copy源文件信息到目标图片中
                int rgb = templateImageData[i][j];
                if (rgb != 16777215 && rgb <= 0) {
                    // 避免出现数组下标越界
                    if (i < oriImageData.length && j < oriImageData[i].length) {
                        targetImage.setRGB(i, j, oriImageData[i][j]);
                    } else {
                        // 下标越界的情况下可能出现获取到的抠图不完整,用户再次获取即可,不加判定会抛出异常,导致获取图片失败
                    }
                }
            }
        }
        return targetImage;
    }


    /**
     * 获取目标区域
     *
     * @param x            随机切图坐标x轴位置
     * @param y            随机切图坐标y轴位置
     * @param targetWidth  切图后目标宽度
     * @param targetHeight 切图后目标高度
     * @param ois          源文件输入流
     * @return
     * @throws Exception
     */
    private BufferedImage getTargetArea(int x, int y, int targetWidth, int targetHeight, InputStream ois,
                                        String fileType) throws Exception {
        /**
         * 返回包含所有当前已注册 ImageReader 的 Iterator
         */
        Iterator<ImageReader> imageReaderList = ImageIO.getImageReadersByFormatName(fileType);

        ImageReader imageReader = imageReaderList.next();

        // 获取图片流
        try (ImageInputStream iis = ImageIO.createImageInputStream(ois)) {
            /**
             * iis:读取源.
             * true:只向前搜索
             * 将它标记为 '只向前搜索'。
             * 此设置意味着包含在输入源中的图像将只按顺序读取,可能允许 reader
             * 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
             */
            imageReader.setInput(iis, true);
            /**
             * 描述如何对流进行解码的类.
             * 用于指定如何在输入时从 Java Image I/O
             * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件
             * 将从其 ImageReader 实现的 getDefaultReadParam 方法中返回
             * ImageReadParam 的实例。
             */
            ImageReadParam param = imageReader.getDefaultReadParam();
            /**
             * 图片裁剪区域。
             * Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
             * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
             */
            Rectangle rec = new Rectangle(x, y, targetWidth, targetHeight);

            /**
             *  提供一个 BufferedImage，将其用作解码像素数据的目标。
             */
            param.setSourceRegion(rec);

            /**
             * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将
             * 它作为一个完整的 BufferedImage 返回。
             */
            BufferedImage targetImage = imageReader.read(0, param);
            return targetImage;
        }
    }

    /**
     * 生成图像矩阵
     *
     * @param
     * @return
     * @throws Exception
     */
    private int[][] getData(BufferedImage bufferedImage) {
        int[][] data = new int[bufferedImage.getWidth()][bufferedImage.getHeight()];
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                data[i][j] = bufferedImage.getRGB(i, j);
            }
        }
        return data;
    }

    /**
     * 图片转base64
     *
     * @param image
     * @return
     * @throws Exception
     */
    private String imageToBase64(BufferedImage image) throws Exception {
        byte[] imageData = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", byteArrayOutputStream);
            imageData = byteArrayOutputStream.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            String BASE64IMAGE = encoder.encodeBuffer(imageData).trim();
            //删除 \r\n
            BASE64IMAGE = BASE64IMAGE.replaceAll("\n", "").replaceAll("\r", "");
            return BASE64IMAGE;
        }
    }

    /**
     * byte数组转base64
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public String byteToBase64(byte[] bytes) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        String BASE64IMAGE = encoder.encodeBuffer(bytes).trim();
        //删除 \r\n
        BASE64IMAGE = BASE64IMAGE.replaceAll("\n", "").replaceAll("\r", "");
        return BASE64IMAGE;
    }


    /**
     * base64转图片
     *
     * @param base64String
     * @return
     */
    private BufferedImage base64StringToImage(String base64String) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 随机生成抠图坐标,并添加到数组中
     * 下标及其对应的数值含义:
     * <p>
     * 0:X轴长度           int
     * 1:Y轴长度           int
     * 2:X轴滑动轨迹百分比  float
     * 3:Y轴滑动轨迹百分比  float
     */
    public Object[] generateCutoutCoordinates() {
        Object[] coordinate = new Object[4];
        int X, Y;
        float xPercent, yPercent;
        Random random = new Random();
        int widthDifference = ORI_WIDTH - WIDTH;
        int heightDifference = ORI_HEIGHT - HEIGHT;

        if (widthDifference <= 0) {
            X = 5;
        } else {
            X = random.nextInt(ORI_WIDTH - WIDTH) + 5;
        }
//        if (heightDifference <= 0) {
//            Y = 5;
//        } else {
//            Y = random.nextInt(ORI_HEIGHT - HEIGHT) + 5;
//        }
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        xPercent = Float.parseFloat(numberFormat.format((float) X / (float) ORI_WIDTH));
        coordinate[0] = X;
        coordinate[1] = 0;
        coordinate[2] = xPercent;
        coordinate[3] = 0.0f;
        return coordinate;
    }


}
