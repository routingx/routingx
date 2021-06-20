package routingx.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomUtils;

public class CaptchaUtils {

	public static final String CAPTCHA_CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";// 优乐欢迎大哥小妹帅男美女来陪姐弟一起玩人多力量大手足中文二三四五六七八九十";
	private static Random random = new Random();

	private static String[] fontNames;
	private static String[] fontNamesChinese;

	static {
		initFontName();
	}

	private static void initFontName() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNames = e.getAvailableFontFamilyNames();
		List<String> fontsChinese = new ArrayList<>();
		for (String name : fontNames) {
			if (isChinese(name)) {
				fontsChinese.add(name);
			}
		}
		fontNames = new String[] { "Viner Hand ITC", "Algerian", "Verdana", "Times New Roman", "Tekton Pro", "Snap ITC",
				"Segoe Print" };
		fontNamesChinese = fontsChinese.toArray(new String[fontsChinese.size()]);
	}

	public static boolean isChinese(String string) {
		int n = 0;
		for (int i = 0; i < string.length(); i++) {
			n = (int) string.charAt(i);
			if (!(19968 <= n && n < 40869)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isChinese(char c) {
		int n = (int) c;
		if ((19968 <= n && n < 40869)) {
			return true;
		}
		return false;
	}

	public static String randomNumber(int captchaSize) {
		int endExclusive = 1;
		for (int i = 0; i < captchaSize; i++) {
			endExclusive = endExclusive * 10;
		}
		return Long.toString(RandomUtils.nextLong(endExclusive / 10, endExclusive));
	}

	/**
	 * * 使用系统默认字符源生成验证码 * @param captchaSize 验证码长度 * @return
	 */
	public static String random(int captchaSize) {
		return random(captchaSize, CAPTCHA_CODES);
	}

	/**
	 * * 使用指定源生成验证码 * @param captchaSize 验证码长度 * @param sources 验证码字符源 * @return
	 */
	public static String random(int captchaSize, String sources) {
		if (sources == null || sources.length() == 0) {
			sources = CAPTCHA_CODES;
		}
		if (fontNamesChinese.length == 0) {
			initFontName();
		}
		int codesLen = sources.length();
//		if (fontNamesChinese.length == 0) {
//			codesLen = 34;
//		}
		Random rand = new Random(System.currentTimeMillis());
		StringBuilder captcha = new StringBuilder(captchaSize);
		for (int i = 0; i < captchaSize; i++) {
			captcha.append(sources.charAt(rand.nextInt(codesLen - 1)));
		}
		return captcha.toString();
	}

	/**
	 * * 生成随机验证码文件,并返回验证码值 * @param w * @param h * @param outputFile * @param
	 * captchaSize * @return * @throws IOException
	 */
	public static String outputImage(int w, int h, File outputFile, int captchaSize) throws IOException {
		String captcha = random(captchaSize);
		outputImage(w, h, outputFile, captcha);
		return captcha;
	}

	/**
	 * * 输出随机验证码图片流,并返回验证码值 * @param w * @param h * @param os * @param captchaSize
	 * * @return * @throws IOException
	 */
	public static String outputImage(int w, int h, OutputStream os, int captchaSize) throws IOException {
		String verifyCode = random(captchaSize);
		outputImage(w, h, os, verifyCode);
		return verifyCode;
	}

	/**
	 * * 生成指定验证码图像文件 * @param w * @param h * @param outputFile * @param code
	 * * @throws IOException
	 */
	public static void outputImage(int w, int h, File outputFile, String code) throws IOException {
		if (outputFile == null) {
			return;
		}
		File dir = outputFile.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		outputFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(outputFile);
		outputImage(w, h, fos, code);
		fos.close();
	}

	/**
	 * * 输出指定验证码图片流 * @param w * @param h * @param os * @param code * @throws
	 * IOException
	 */
	public static void outputImage(int w, int h, OutputStream os, String code) throws IOException {
		int captchaSize = code.length();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Random rand = new Random();
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color[] colors = new Color[5];
		Color[] colorSpaces = new Color[] { Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA,
				Color.ORANGE, Color.PINK, Color.YELLOW };
		float[] fractions = new float[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)];
			fractions[i] = rand.nextFloat();
		}
		Arrays.sort(fractions);

		g2.setColor(Color.WHITE);// 设置边框色
		g2.fillRect(0, 0, w, h);

		Color c = getRandColor(240, 250);
		g2.setColor(c);// 设置背景色
		g2.fillRect(0, 2, w, h - 4);

		// 绘制干扰线
		g2.setColor(getRandColor(200, 240));// 设置线条的颜色
		for (int i = 0; i < 60; i++) {
			int x = random.nextInt(w - 1);
			int y = random.nextInt(h - 1);
			int xl = random.nextInt(6) + 1;
			int yl = random.nextInt(12) + 1;
			g2.drawLine(x, y, x + xl + 40, y + yl + 20);
		}

		// 添加噪点
		float yawpRate = 0.05f;// 噪声率
		int area = (int) (yawpRate * w * h);
		for (int i = 0; i < area; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int rgb = getRandomIntColor();
			image.setRGB(x, y, rgb);
		}
		// shear(g2, w, h, c);// 使图片扭曲
		char[] chars = code.toCharArray();
		for (int i = 0; i < captchaSize; i++) {
			g2.setColor(getRandColor(100, 200));
			// int fontSize = h - 4;
			int fontSize = RandomUtils.nextInt(h - h / 3, h + h / code.length());
			String fontName = "宋体";
			if (isChinese(code.charAt(i))) {
				if (fontNamesChinese.length > 0) {
					fontName = fontNamesChinese[RandomUtils.nextInt(0, fontNamesChinese.length)];
				}
			} else if (fontNames.length > 0) {
				fontName = fontNames[RandomUtils.nextInt(0, fontNames.length)];
			}
			Font font = new Font(fontName, Font.ITALIC, fontSize);
			g2.setFont(font);
			AffineTransform affine = new AffineTransform();
			affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
					(w / captchaSize) * i + fontSize / 2, h / 2);
			g2.setTransform(affine);
			g2.drawChars(chars, i, 1, ((w - 5) / captchaSize) * i + 5, h / 2 + fontSize / 2 - 5);
		}

		yawpRate = 0.02f;// 噪声率
		area = (int) (yawpRate * w * h);
		for (int i = 0; i < area; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int rgb = getRandomIntColor();
			image.setRGB(x, y, rgb);
		}

		g2.dispose();
		ImageIO.write(image, "jpg", os);
	}

	private static Color getRandColor(int fc, int bc) {
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private static int getRandomIntColor() {
		int[] rgb = getRandomRgb();
		int color = 0;
		for (int c : rgb) {
			color = color << 8;
			color = color | c;
		}
		return color;
	}

	private static int[] getRandomRgb() {
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = random.nextInt(255);
		}
		return rgb;
	}

	protected static void shear(Graphics g, int w1, int h1, Color color) {
		shearX(g, w1, h1, color);
		shearY(g, w1, h1, color);
	}

	private static void shearX(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(2);

		boolean borderGap = true;
		int frames = 1;
		int phase = random.nextInt(2);

		for (int i = 0; i < h1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}

	}

	private static void shearY(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(40) + 10; // 50;

		boolean borderGap = true;
		int frames = 20;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}

		}

	}
}
