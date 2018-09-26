import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class SecurityCodeUtil {

	static {
		// 以 headless 模式初始化 AWT 组件
		System.setProperty("java.awt.headless", "true");
	}

	/*private static final int DEFAULT_CODE_LENGTH = 6;
	private static final int DEFAULT_FONT_SIZE = 30;
	private static final int DEFAULT_LINE_NUMBER = 3;
	private static final int BASE_PADDING_LEFT = 5;
	private static final int RANGE_PADDING_LEFT = 10;
	private static final int BASE_PADDING_TOP = 15;
	private static final int RANGE_PADDING_TOP = 10;
	private static final int DEFAULT_WIDTH = 140;
	private static final int DEFAULT_HEIGHT = 39;*/
	
	private static final int DEFAULT_CODE_LENGTH = 4;
	//private static final int DEFAULT_FONT_SIZE = 18;
	private static final int DEFAULT_LINE_NUMBER = 5;
	//private static final int BASE_PADDING_LEFT = 1;
	//private static final int RANGE_PADDING_LEFT = 1;
	//private static final int BASE_PADDING_TOP = 1;
	//private static final int RANGE_PADDING_TOP = 1;
	private static final int DEFAULT_WIDTH = 90;
	private static final int DEFAULT_HEIGHT = 35;

//	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6',
//			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
//			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
//			'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
//			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
//			'X', 'Y', 'Z' };

	private static final char[] CHARS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z' };

	private static SecurityCodeUtil securityCodeUtil = null;

	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	//private int base_padding_left = BASE_PADDING_LEFT;
	//private int base_padding_top = BASE_PADDING_TOP;
	//private int range_padding_left = RANGE_PADDING_LEFT;
	//private int range_padding_top = RANGE_PADDING_TOP;
	private int codeLength = DEFAULT_CODE_LENGTH;
	private int lineNumber = DEFAULT_LINE_NUMBER;
	//private int fontSize = DEFAULT_FONT_SIZE;

	private String code;
	//private int padding_left;
	//private int padding_top;
	private Random random = new Random();

	private SecurityCodeUtil() {
	}

	public static SecurityCodeUtil getInstance() {
		if (securityCodeUtil == null) {
			securityCodeUtil = new SecurityCodeUtil();
		}
		return securityCodeUtil;
	}

	public String createCode() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < codeLength; i++) {
			buffer.append(CHARS[random.nextInt(CHARS.length)]);
		}
		return buffer.toString();
	}

	private int rand(int min, int max){
		if(min == max){
			return min;
		}else if(min>max){
			return  random.nextInt(max)%(min-max+1) + min;
		}else{
			return  random.nextInt(max)%(max-min+1) + min;
		}
	}

	Color randomColorNear(int red, int green, int blue){
		int maxRed = Math.min(red + 30, 255);
		int minRed  =Math.max(0, red - 30);
		int maxGreen = Math.min(red + 30, 255);
		int minGreen = Math.max(0, green - 30);
		int maxBlue = Math.min(red + 30, 255);
		int minBlue = Math.max(0, blue - 30);

		random.nextInt(red + 30);
		return new Color(rand(minRed, maxRed), rand(minGreen, maxGreen), rand(minBlue, maxBlue));
	}

	public BufferedImage createCodeBitmap() {

		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
			
		//padding_left = 0;
		//base_padding_left = width / codeLength;
		code = createCode();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);	
		//paint.setAntiAlias(true);
		//paint.setTextSize(fontSize);
		//paint.setColor(Color.BLUE);

		int colorBound = 200;

		int red = random.nextInt(200);
		int green = random.nextInt(200);
		int blue = random.nextInt(200);
		Color fontColor = new Color(red, green, blue);
		g.setColor(fontColor);
		Font font = new Font("Algerian", Font.ITALIC, 28);

		/** TODO, 字体文件不存在, 会导致验证码显示不正常. 为了绝对避免这个问题, 本系统应当自带字体文件. */
		//Font.createFont();

		g.setFont(font);
		for (int i = 0; i < code.length(); i++) {
			randomStyle(g, i);
			g.drawString(String.valueOf(code.charAt(i)), 20 * i + 5, 25);
			randomPadding(i);
		}
		for (int i = 0; i < lineNumber; i++) {
			g.setColor(randomColorNear(red, green, blue));
			drawLine(g);
		}
		for (int i = 0; i < 200; i++) {
			g.setColor(randomColorNear(red, green, blue));
			drawPoints(g);
		}
		return image;
	}

	public String getCode() {
		return code;
	}

	private void randomStyle(Graphics2D g, int i){

		AffineTransform trans = new AffineTransform(); 
		double radius = random.nextInt(25)*3.14/180;
		radius = random.nextBoolean()?radius:-radius;
		trans.rotate(radius, 0 , 0) ;  
		float scaleSize = random.nextFloat() + 0.8f ;  
        if(scaleSize>1f)  
            scaleSize = 1f ;  
        trans.scale(scaleSize, scaleSize) ;  
        //g.setTransform(trans);
        //g.setClip( 20 * i + 10, 25, 20, 35);
        //g.rotate(radius, 0, 0);
		//g.setFont(new Font(null, random.nextBoolean()?Font.BOLD:Font.PLAIN, 28));
	}

	private Color randomColor() {
		int red = random.nextInt(200);
		int green = random.nextInt(200);
		int blue = random.nextInt(200);
		return new Color(red, green, blue);
	}

	private Color randomColor(int bound) {
		int red = random.nextInt(bound);
		int green = random.nextInt(bound);
		int blue = random.nextInt(bound);
		return new Color(red, green, blue);
	}

	private void randomPadding(int i) {
		//padding_left = base_padding_left * i
		//		+ random.nextInt(range_padding_left);
	}

	private void drawLine(Graphics2D g) {
		//Color color = randomColor();
		int startX = random.nextInt(width);
		int startY = random.nextInt(height);
		int stopX = random.nextInt(width);
		int stopY = random.nextInt(height);
		//g.setColor(color);
		g.drawLine(startX, startY, stopX, stopY);
	}
	
	private void drawPoints(Graphics2D g){
		//Color color = randomColor();
		int stopX = random.nextInt(width);
		int stopY = random.nextInt(height);
		//g.setColor(color);
		g.drawLine(stopX, stopY, stopX, stopY);
	}

}
