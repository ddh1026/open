package com.acts.opencv.base;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.acts.opencv.common.utils.CommonUtil;
import com.acts.opencv.common.utils.Constants;
import com.acts.opencv.common.utils.OpenCVUtil;
import com.acts.opencv.common.web.BaseController;
import com.acts.opencv.demo.DemoController;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


@Controller
@RequestMapping(value = "base")
public class BaseMethodController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

	/**
	 * 二值化方法测试
	 * 创建者 Songer
	 * 创建时间	2018年3月9日
	 */
	@RequestMapping(value = "binary")
	public void binary(HttpServletResponse response, String imagefile, Integer binaryType, Double thresh, Double maxval) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 二值化方法");

		// 灰度化
		// Imgproc.cvtColor(source, destination, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		// 加载为灰度图显示
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		logger.info("binaryType:{},thresh:{},maxval:{}", binaryType, thresh, maxval);
		switch (binaryType) {
		case 0:
			binaryType = Imgproc.THRESH_BINARY;
			break;
		case 1:
			binaryType = Imgproc.THRESH_BINARY_INV;
			break;
		case 2:
			binaryType = Imgproc.THRESH_TRUNC;
			break;
		case 3:
			binaryType = Imgproc.THRESH_TOZERO;
			break;
		case 4:
			binaryType = Imgproc.THRESH_TOZERO_INV;
			break;
		default:
			break;
		}
		Imgproc.threshold(source, destination, Double.valueOf(thresh), Double.valueOf(maxval), binaryType);
//		Imgproc.adaptiveThreshold(source, destination, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 31, 15);
//		Imgproc.threshold(source, destination, 170, 255, Imgproc.THRESH_BINARY_INV);
//		Imgproc.threshold(source, destination, 127, 255, Imgproc.THRESH_TOZERO);
//		Imgproc.threshold(source, destination, 0, 255, Imgproc.THRESH_TOZERO_INV);

		// String filename = imagefile.substring(imagefile.lastIndexOf("/"), imagefile.length());
		// String filename_end = filename.substring(filename.lastIndexOf("."), filename.length());
		// String filename_pre = filename.substring(0, filename.lastIndexOf("."));
		// System.out.println(filename_pre);
		// System.out.println(filename_end);
		// filename = filename_pre + "_" + binaryType + "_" + thresh + "_" + maxval + "_" + filename_end;

		// 原方式1生成图片后，页面读取的方式，但是实时性不好改为方式2
		// String destPath = Constants.DIST_IMAGE_PATH + filename;
		// File dstfile = new File(destPath);
		// if (StringUtils.isNotBlank(filename) && dstfile.isFile() && dstfile.exists()) {
		// dstfile.delete();
		// logger.info("删除图片：" + filename);
		// }
		// Highgui.imwrite(destPath, destination);
		// logger.info("生成目标图片==============" + destPath);
		// renderString(response, filename);
		// renderString(response, Constants.SUCCESS);
		// 方式1end//

		// 方式2，回写页面图片流
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 自适用二值化
	 * @Author 王嵩
	 * @param response
	 * @param imagefile
	 * @param binaryType 二值化类型
	 * @param blockSize 附近区域面积
	 * @param constantC 它只是一个常数，从平均值或加权平均值中减去的常数
	 * @Date 2018年4月9日
	 * 更新日志
	 * 2018年4月9日 王嵩  首次创建
	 */
	@RequestMapping(value = "adaptiveBinary")
	public void adaptiveBinary(HttpServletResponse response, String imagefile, Integer adaptiveMethod,
			Integer binaryType, Integer blockSize,
			Double constantC) {
		//
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 自适用二值化方法");

		// 灰度化
		// Imgproc.cvtColor(source, destination, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		// 加载为灰度图显示
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		logger.info("binaryType:{},blockSize:{},constantC:{}", binaryType, blockSize, constantC);
		switch (adaptiveMethod) {
		case 0:
			adaptiveMethod = Imgproc.ADAPTIVE_THRESH_MEAN_C;
			break;
		case 1:
			adaptiveMethod = Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
			break;
		}

		switch (binaryType) {
		case 0:
			binaryType = Imgproc.THRESH_BINARY;
			break;
		case 1:
			binaryType = Imgproc.THRESH_BINARY_INV;
			break;
		case 2:
			binaryType = Imgproc.THRESH_TRUNC;
			break;
		case 3:
			binaryType = Imgproc.THRESH_TOZERO;
			break;
		case 4:
			binaryType = Imgproc.THRESH_TOZERO_INV;
			break;
		default:
			break;
		}
		Imgproc.adaptiveThreshold(source, destination, 255, adaptiveMethod, binaryType,
				blockSize, constantC);

		// 方式2，回写页面图片流
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 自适用二值化+zxing识别条形码
	 * @Author 王嵩
	 * @param response
	 * @param imagefile
	 * @param binaryType 二值化类型
	 * @param blockSize 附近区域面积
	 * @param constantC 它只是一个常数，从平均值或加权平均值中减去的常数
	 * @Date 2018年5月17日
	 * 更新日志
	 * 2018年5月17日 王嵩  首次创建
	 */
	@RequestMapping(value = "zxing")
	public void zxing(HttpServletResponse response, String imagefile, Integer adaptiveMethod, Integer binaryType,
			Integer blockSize, Double constantC) {
		//
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 自适用二值化方法");

		// 灰度化
		// Imgproc.cvtColor(source, destination, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		// 加载为灰度图显示
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		logger.info("binaryType:{},blockSize:{},constantC:{}", binaryType, blockSize, constantC);
		switch (adaptiveMethod) {
		case 0:
			adaptiveMethod = Imgproc.ADAPTIVE_THRESH_MEAN_C;
			break;
		case 1:
			adaptiveMethod = Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
			break;
		}

		switch (binaryType) {
		case 0:
			binaryType = Imgproc.THRESH_BINARY;
			break;
		case 1:
			binaryType = Imgproc.THRESH_BINARY_INV;
			break;
		case 2:
			binaryType = Imgproc.THRESH_TRUNC;
			break;
		case 3:
			binaryType = Imgproc.THRESH_TOZERO;
			break;
		case 4:
			binaryType = Imgproc.THRESH_TOZERO_INV;
			break;
		default:
			break;
		}
		// Imgproc.adaptiveThreshold(source, destination, 255, adaptiveMethod, binaryType, blockSize, constantC);
		Imgproc.threshold(source, destination, 190, 255, Imgproc.THRESH_BINARY);
		String result = parseCode(destination);

		renderString(response, result);

	}

	private static String parseCode(Mat mat) {
		String resultText = "无法识别！！！";
		try {
			MultiFormatReader formatReader = new MultiFormatReader();
			// if (!file.exists()) {
			// System.out.println("nofile");
			// return;
			// }
			// BufferedImage image = ImageIO.read(file);

			BufferedImage image = OpenCVUtil.toBufferedImage(mat);
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			Binarizer binarizer = new HybridBinarizer(source);
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

			Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
			hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

			Result result = formatReader.decode(binaryBitmap, hints);
			StringBuffer sbuffer = new StringBuffer();
			sbuffer.append("解析结果 = " + result.toString() + "\n");
			sbuffer.append("二维码格式类型 = " + result.getBarcodeFormat() + "\n");
			sbuffer.append("二维码文本内容 = " + result.getText() + "\n");
			resultText = sbuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultText;
	}

	/**
	 * 高斯滤波方法测试
	 * 创建者 Songer
	 * 创建时间	2018年3月9日
	 */
	@RequestMapping(value = "gaussian")
	public void gaussian(HttpServletResponse response, String imagefile, String kwidth, String kheight, String sigmaX,
			String sigmaY) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 二值化方法");

		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		logger.info("kwidth:{},kheight:{},sigmaX:{},sigmaY:{}", kwidth, kheight, sigmaX, sigmaY);
		Imgproc.GaussianBlur(source, destination,
				new Size(2 * Integer.valueOf(kwidth) + 1, 2 * Integer.valueOf(kheight) + 1),
				Integer.valueOf(sigmaX), Integer.valueOf(sigmaY));
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图像锐化操作
	 * @Author 王嵩
	 * @param response
	 * @param imagefile
	 * @param ksize 中值滤波内核size
	 * @param alpha 控制图层src1的透明度
	 * @param beta 控制图层src2的透明度
	 * @param gamma gamma越大合并的影像越明亮 void
	 * @Date 2018年5月18日
	 * 更新日志
	 * 2018年5月18日 王嵩  首次创建
	 *
	 */
	@RequestMapping(value = "sharpness")
	public void sharpness(HttpServletResponse response, String imagefile, int ksize, double alpha, double beta,
			double gamma) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 锐化操作");

		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		// 先进行中值滤波操作
		Imgproc.medianBlur(source, destination, 2 * ksize + 1);
		// 通过合并图层的方式进行效果增强 alpha控制src1的透明度，beta控制src2 的透明图；gamma越大合并的影像越明亮
		// public static void addWeighted(Mat src1, double alpha, Mat src2, double beta, double gamma, Mat dst)
		Core.addWeighted(source, alpha, destination, beta, 0, destination);

		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图片缩放方法测试
	 * 创建者 Songer
	 * 创建时间	2018年3月15日
	 */
	@RequestMapping(value = "resize")
	public void resize(HttpServletResponse response, String imagefile, Double rewidth, Double reheight,
			Integer resizeType) {
		// 默认都是放大
		double width = rewidth;
		double height = reheight;

		if (resizeType == 2) {
			width = 1 / width;
			height = 1 / height;
		}
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 图片缩放方法测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		logger.info("resizeType:{},rewidth:{},reheight:{}", resizeType, rewidth, reheight);
		Imgproc.resize(source, destination, new Size(0, 0), width, height, 0);
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 腐蚀膨胀测试
	 * 创建者 Songer
	 * 创建时间	2018年3月15日
	 */
	@RequestMapping(value = "erodingAndDilation")
	public void erodingAndDilation(HttpServletResponse response, String imagefile, Double kSize, Integer operateType,
			Integer shapeType, boolean isBinary) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 腐蚀膨胀测试测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		if (isBinary) {
			source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			// Imgproc.threshold(source, source, 100, 255, Imgproc.THRESH_BINARY);
		}
		double size = Double.valueOf(kSize);
		int shape = 0;
		switch (shapeType) {
		case 0:
			shape = Imgproc.MORPH_RECT;
			break;
		case 1:
			shape = Imgproc.MORPH_CROSS;
			break;
		case 2:
			shape = Imgproc.MORPH_ELLIPSE;
			break;
		}
		Mat element = Imgproc.getStructuringElement(shape, new Size(2 * size + 1, 2 * size + 1));
		logger.info("kSize:{},operateType:{},shapeType:{},isBinary:{}", kSize, operateType, shapeType, isBinary);
		if (operateType == 1) {// 腐蚀
			Imgproc.erode(source, destination, element);
		} else {// 膨胀
			Imgproc.dilate(source, destination, element);
		}
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 腐蚀膨胀使用进阶
	 * 更高级的形态学变换处理：morphologyEx
	 * 创建者 Songer
	 * 创建时间	2018年3月15日
	 */
	@RequestMapping(value = "morphologyEx")
	public void morphologyEx(HttpServletResponse response, String imagefile, Double kSize, Integer operateType,
			Integer shapeType, boolean isBinary) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 腐蚀膨胀测试测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		if (isBinary) {
			source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			// Imgproc.threshold(source, source, 100, 255, Imgproc.THRESH_BINARY);
		}
		double size = Double.valueOf(kSize);
		int shape = 0;
		switch (shapeType) {
		case 0:
			shape = Imgproc.MORPH_RECT;
			break;
		case 1:
			shape = Imgproc.MORPH_CROSS;
			break;
		case 2:
			shape = Imgproc.MORPH_ELLIPSE;
			break;
		}

		int op = 2;
		switch (operateType) {// 主要是为了方便查看参数是哪一个
		case 2:
			op = Imgproc.MORPH_OPEN;
			break;
		case 3:
			op = Imgproc.MORPH_CLOSE;
			break;
		case 4:
			op = Imgproc.MORPH_GRADIENT;
			break;
		case 5:
			op = Imgproc.MORPH_TOPHAT;
			break;
		case 6:
			op = Imgproc.MORPH_BLACKHAT;
			break;
		case 7:
			op = Imgproc.MORPH_HITMISS;
			break;
		}

		Mat element = Imgproc.getStructuringElement(shape, new Size(2 * size + 1, 2 * size + 1));
		logger.info("kSize:{},operateType:{},shapeType:{},isBinary:{}", kSize, operateType, shapeType, isBinary);

		Imgproc.morphologyEx(source, destination, op, element);
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 边缘检测Canny
	 * 创建者 Songer
	 * 创建时间	2018年3月15日
	 */
	@RequestMapping(value = "canny")
	public void canny(HttpServletResponse response, String imagefile, Double threshold1, Double threshold2,
			boolean isBinary) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 边缘检测测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Imgproc.Canny(source, destination, threshold1, threshold2);
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 霍夫线变换
	 * 创建者 Songer
	 * 创建时间	2018年3月19日
	 */
	@RequestMapping(value = "houghline")
	public void houghline(HttpServletResponse response, String imagefile, Double threshold1, Double threshold2,
			Integer threshold, Double minLineLength, Double maxLineGap) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 霍夫线变换测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source1 = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);// 彩色图
		Mat source2 = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);// 灰度图
		Mat lineMat = new Mat(source2.rows(), source2.cols(), source2.type());
		Mat destination = new Mat(source2.rows(), source2.cols(), source2.type());
		Imgproc.Canny(source2, destination, threshold1, threshold2);
		Imgproc.HoughLinesP(destination, lineMat, 1, Math.PI / 180, threshold, minLineLength, maxLineGap);
		int[] a = new int[(int) lineMat.total() * lineMat.channels()]; // 数组a存储检测出的直线端点坐标
		lineMat.get(0, 0, a);
		for (int i = 0; i < a.length; i += 4) {
			// new Scalar(255, 0, 0) blue
			// new Scalar(0, 255, 0) green
			// new Scalar(0, 0, 255) red
			Core.line(source1, new Point(a[i], a[i + 1]), new Point(a[i + 2], a[i + 3]), new Scalar(0, 255, 0), 2);
		}

		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(source1);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 霍夫圆变换
	 * 创建者 Songer
	 * 创建时间	2018年3月20日
	 */
	@RequestMapping(value = "houghcircle")
	public void houghcircle(HttpServletResponse response, String imagefile, Double minDist, Double param1,
			Double param2, Integer minRadius, Integer maxRadius) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 霍夫圆变换测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source1 = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);// 彩色图
		Mat source2 = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);// 灰度图
		Mat circleMat = new Mat(source2.rows(), source2.cols(), source2.type());

		Imgproc.HoughCircles(source2, circleMat, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDist, param1, param2, minRadius,
				maxRadius);// 霍夫变换检测圆
		System.out.println("----------------" + circleMat.cols());
		int cols = circleMat.cols();
		// Point anchor01 = new Point();
		if (cols > 0) {
			for (int i = 0; i < cols; i++) {
				double vCircle[] = circleMat.get(0, i);
				Point center = new Point(vCircle[0], vCircle[1]);
				int radius = (int) Math.round(vCircle[2]);
				Core.circle(source1, center, 3, new Scalar(0, 255, 0), -1, 8, 0);// 绿色圆心
				Core.circle(source1, center, radius, new Scalar(0, 0, 255), 3, 8, 0);// 红色圆边
				// anchor01.x = vCircle[0];
				// anchor01.y = vCircle[1];

			}
		}
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(source1);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 颜色识别测试
	 * 创建者 Songer
	 * 创建时间	2018年3月20日
	 */
	@RequestMapping(value = "findcolor")
	public void findcolor(HttpServletResponse response, String imagefile, Integer color,
			Integer colorType) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 查找颜色测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		if (colorType == 1) {// 1为RGB方式，2为HSV方式
			double B = 0;
			double G = 0;
			double R = 0;
			switch (color) {
			case 1:// red
				B = 0;
				G = 0;
				R = 255;
				break;
			case 2:// blue
				B = 255;
				G = 0;
				R = 0;
				break;
			case 3:// green
				B = 0;
				G = 255;
				R = 0;
				break;
			case 4:// yellow
				B = 0;
				G = 255;
				R = 255;
				break;
			}
			Core.inRange(source, new Scalar(B, G, R), new Scalar(B, G, R), destination);
		} else {// HSV方式
			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			double min = 0;
			double max = 0;
			// 泛红色系(176,90,90)-(0, 90, 90)-(20,255,255) 简易：0-20
			// 泛蓝色系(100, 90, 90)-(120,255,255)
			// 泛绿色系(60, 90, 90)-(80,255,255)
			// 泛黄色系(23, 90, 90)-(38,255,255)
			switch (color) {
			case 1:// red
				min = 0;
				max = 20;
				break;
			case 2:// blue
				min = 100;
				max = 120;
				break;
			case 3:// green
				min = 60;
				max = 80;
				break;
			case 4:// yellow
				min = 23;
				max = 38;
				break;
			}
			Core.inRange(source, new Scalar(min, 90, 90), new Scalar(max, 255, 255), destination);
		}
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 轮廓识别测试
	 * 创建者 Songer
	 * 创建时间	2018年3月20日
	 */
	@RequestMapping(value = "contours")
	public void contours(HttpServletResponse response, String imagefile, Integer mode, Integer method,
			Integer contourNum) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 轮廓识别测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		logger.info("mode:{},method:{}", mode, method);

		switch (mode) {
		case 0:
			mode = Imgproc.RETR_EXTERNAL;
			break;
		case 1:
			mode = Imgproc.RETR_LIST;
			break;
		case 2:
			mode = Imgproc.RETR_CCOMP;
			break;
		case 3:
			mode = Imgproc.RETR_TREE;
			break;
		}
		switch (method) {
		case 0:
			method = Imgproc.CV_CHAIN_CODE;
			break;
		case 1:
			method = Imgproc.CHAIN_APPROX_NONE;
			break;
		case 2:
			method = Imgproc.CHAIN_APPROX_SIMPLE;
			break;
		case 3:
			method = Imgproc.CHAIN_APPROX_TC89_L1;
			break;
		case 4:
			method = Imgproc.CHAIN_APPROX_TC89_KCOS;
			break;
		case 5:
			method = Imgproc.CV_LINK_RUNS;
			break;
		}

		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		// Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Mat destination = Mat.zeros(source.size(), CvType.CV_8UC3);
		Mat hierarchy = new Mat(source.rows(), source.cols(), CvType.CV_8UC1, new Scalar(0));
		Vector<MatOfPoint> contours = new Vector<MatOfPoint>();
		Imgproc.findContours(source, contours, hierarchy, mode, method, new Point());
		System.out.println(contours.size());
		logger.info("轮廓数量为：{}，当前请求要展现第{}个轮廓", contours.size(), contourNum);
		// contourNum因为轮廓计数是从0开始
		if (contourNum == -1 || (contourNum + 1) > contours.size()) {
			logger.info("轮廓数量已经超出，默认显示所有轮廓，轮廓数量：{}", contours.size());
			contourNum = -1;
		}
		Imgproc.drawContours(destination, contours, contourNum, new Scalar(0, 255, 0), 2);
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(destination);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 模板查找测试
	 * 创建者 Songer
	 * 创建时间	2018年3月21日
	 */
	@RequestMapping(value = "findtemplate")
	public void findtemplate(HttpServletResponse response, String imagefile, Integer method, Integer imageType,
			Double x1, Double y1, Double x2, Double y2, Double width, Double height) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 模板查找测试");
		String sourcePath = Constants.PATH + imagefile;
		logger.info("url==============" + sourcePath);
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
		// Mat destination = new Mat(source.rows(), source.cols(), source.type());
		// String templateimage = Constants.SOURCE_IMAGE_PATH + "/template.png";
		// System.out.println(templateimage);
		// Mat matchtemp = Highgui.imread(templateimage);
		// 优化代码，模板图像直接通过前端截取或取得，而不是写死，此处用到了OpenCV的截取图像功能
		logger.info("{},{},{},{}", x1, y1, width, height);
		Mat matchtemp = source.submat(new Rect(Integer.valueOf(CommonUtil.setScare(x1.toString(), 0)), Integer
				.valueOf(CommonUtil.setScare(y1.toString(), 0)), Integer.valueOf(CommonUtil.setScare(width.toString(),
				0)), Integer.valueOf(CommonUtil.setScare(height.toString(), 0))));

		int result_cols = source.cols() - matchtemp.cols() + 1;
		int result_rows = source.rows() - matchtemp.rows() + 1;
		Mat destination = new Mat(result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(source, matchtemp, destination, method);
		// 矩阵归一化处理
		Core.normalize(destination, destination, 0, 255, Core.NORM_MINMAX, -1, new Mat());
		// minMaxLoc(imagematch, minVal, maxVal2, minLoc, maxLoc01, new Mat());
		MinMaxLocResult minmaxLoc = Core.minMaxLoc(destination);
		logger.info("相似值=================：最大：" + minmaxLoc.maxVal + "    最小：" + minmaxLoc.minVal);
		Point matchLoc = new Point();
		switch (method) {
		case 0:
			// method = Imgproc.TM_SQDIFF;
			matchLoc = minmaxLoc.minLoc;
			break;
		case 1:
			// method = Imgproc.TM_SQDIFF_NORMED;
			matchLoc = minmaxLoc.minLoc;
			break;
		case 2:
			// method = Imgproc.TM_CCORR;
			matchLoc = minmaxLoc.maxLoc;
			break;
		case 3:
			// method = Imgproc.TM_CCORR_NORMED;
			matchLoc = minmaxLoc.maxLoc;
			break;
		case 4:
			// method = Imgproc.TM_CCOEFF;
			matchLoc = minmaxLoc.maxLoc;
			break;
		case 5:
			// method = Imgproc.TM_CCOEFF_NORMED;
			matchLoc = minmaxLoc.maxLoc;
			break;
		default:
			// method = Imgproc.TM_SQDIFF;
			matchLoc = minmaxLoc.minLoc;
			break;
		}

		if (imageType == 0) {// 显示过程图片
			source = destination;
		} else {// 显示最终框选结果
			Core.rectangle(source, matchLoc, new Point(matchLoc.x + matchtemp.cols(), matchLoc.y + matchtemp.rows()),
					new Scalar(0, 255, 0), 2);
		}
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(source);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 灰度直方图
	 * @Author 王嵩
	 * @param response
	 * @param imagefile
	 * @param cols
	 * @return Mat
	 * @Date 2018年4月2日
	 * 更新日志
	 * 2018年4月2日 王嵩  首次创建
	 *
	 */
	@RequestMapping(value = "grayHistogram")
	public void grayHistogram(HttpServletResponse response, String imagefile, Integer cols, Integer imageW,
			Integer imageH, Integer imageKedu, boolean isShow) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		logger.info("\n 灰度直方图测试");
		String sourcePath = Constants.PATH + imagefile;
		Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		List<Mat> images = new ArrayList<Mat>();
		images.add(source);
		MatOfInt channels = new MatOfInt(0); // 图像通道数，0表示只有一个通道
		MatOfInt histSize = new MatOfInt(cols); // CV_8U类型的图片范围是0~255，共有256个灰度级
		Mat histogramOfGray = new Mat(); // 输出直方图结果，共有256行，行数的相当于对应灰度值，每一行的值相当于该灰度值所占比例
		MatOfFloat histRange = new MatOfFloat(0, 255);
		Imgproc.calcHist(images, channels, new Mat(), histogramOfGray, histSize, histRange, false); // 计算直方图
		MinMaxLocResult minmaxLoc = Core.minMaxLoc(histogramOfGray);
		// 按行归一化
		// Core.normalize(histogramOfGray, histogramOfGray, 0, histogramOfGray.rows(), Core.NORM_MINMAX, -1, new Mat());

		// 创建画布
		int histImgRows = imageH;
		int histImgCols = imageW;
		int colStep = (int) Math.floor((histImgCols) / histSize.get(0, 0)[0]);
		Mat histImg = new Mat(histImgRows, histImgCols, CvType.CV_8UC3, new Scalar(255, 255, 255)); // 重新建一张图片，绘制直方图

		int max = (int) minmaxLoc.maxVal;
		System.out.println("max--------" + max);
		double bin_u = (double) (histImg.height() - 20) / max; // max: 最高条的像素个数，则 bin_u 为单个像素的高度
		int kedu = 0;
		for (int i = 1; kedu <= minmaxLoc.maxVal; i++) {
			kedu = i * max / 10;
			// 在图像中显示文本字符串
			Core.putText(histImg, kedu + "", new Point(0, histImg.height() - 5 - kedu * bin_u), 1, 1, new Scalar(255,0, 0));
			if (isShow) {
				// 附上高度坐标线，因为高度在画图时-了20，此处也减掉
				Core.line(histImg, new Point(0, histImg.height() - 20 - kedu * bin_u),
						new Point(imageW, histImg.height() - 20 - (kedu + 1) * bin_u), new Scalar(255, 0, 0), 1, 8, 0);
			}
		}

		System.out.println("灰度级:" + histSize.get(0, 0)[0]);
		for (int i = 0; i < histSize.get(0, 0)[0]; i++) { // 画出每一个灰度级分量的比例，注意OpenCV将Mat最左上角的点作为坐标原点
			Core.rectangle(histImg, new Point(colStep * i, histImgRows - 20), new Point(colStep * (i + 1), histImgRows
					- bin_u * Math.round(histogramOfGray.get(i, 0)[0]) - 20), new Scalar(0, 0, 0), 1, 8, 0);
			// if (i % 10 == 0) {
			// Core.putText(histImg, Integer.toString(i), new Point(colStep * i, histImgRows - 5), 1, 1, new Scalar(255,
			// 0, 0)); // 附上x轴刻度
			// }
			// 每隔10画一下刻度,方式2
			kedu = i * imageKedu;
			Core.rectangle(histImg, new Point(colStep * kedu, histImgRows - 20), new Point(colStep * (kedu + 1),
					histImgRows - 20), new Scalar(255, 0, 0), 2, 8, 0);
			Core.putText(histImg, kedu + "", new Point(histImgCols / 256 * kedu, histImgRows - 5), 1, 1, new Scalar(
					255, 0, 0)); // 附上x轴刻度
		}
		try {
			byte[] imgebyte = OpenCVUtil.covertMat2Byte1(histImg);
			renderImage(response, imgebyte);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
