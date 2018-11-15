package com.acts.opencv.base;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
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

import com.acts.opencv.common.utils.Constants;
import com.acts.opencv.common.web.BaseController;


@Controller
@RequestMapping(value = "card2")
public class Card2Controller extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(Card2Controller.class);

	/**
	 * 答题卡识别
	 * @Author Songer
	 * @param response
	 * @param imagefile void
	 * @Date 2018年10月22日
	 * 更新日志
	 * 2018年10月22日 Songer  首次创建
	 *
	 */
	@RequestMapping(value = "cardMarking")
	public void cardMarking(HttpServletResponse response, String imagefile,Integer picno) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			long t1 = new Date().getTime();
			//String sourceimage1 = "D:\\test\\abc\\card\\test4.jpg";
			String sourceimage = Constants.PATH + imagefile;
			//表格检测，获取到表格内容，是查找识别区域的部分
			Mat mat = markingArea(sourceimage);
			String destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "cardResult_3.png";
			Highgui.imwrite(destPath, mat);
//			Highgui.imwrite("D:\\test\\abc\\card\\card111.png", mat);

			
			//具体的答题卡识别过程，主要就是答案识别部分了
			String result = cardResult(mat);
			renderString(response, result);
			long t2 = new Date().getTime();
			logger.info("===耗时"+(t2-t1));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("答题卡识别异常！", e);
		}

	}

	
	/**
	 * 此方法主要是通过边缘检测凸包，查找识别区域。即客观题的框
	 * @Author Songer
	 * @Date 2018年9月21日
	 * 更新日志
	 * 2018年9月21日 Songer  首次创建
	 *
	 */
	 public static Mat markingArea(String path){
			Mat source = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
			Mat img = new Mat();;
			// 彩色转灰度
			Mat result= source.clone();
			Imgproc.cvtColor(source, img, Imgproc.COLOR_BGR2GRAY);
//			//方式1：通过高斯滤波然后边缘检测膨胀来链接边缘，将轮廓连通便于轮廓识别
//			// 高斯滤波，降噪
//			Imgproc.GaussianBlur(img, img, new Size(3,3), 2, 2);
//			
//			// Canny边缘检测
//			Imgproc.Canny(img, img, 20, 60, 3, false);
//			// 膨胀，连接边缘
//			Imgproc.dilate(img, img, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));
//			
			//方式2:使用形态学梯度算法，此算法来保留物体的边缘轮廓很有效果
			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
			Imgproc.morphologyEx(img, img, Imgproc.MORPH_GRADIENT, element);
			Imgproc.threshold(img,img, 170, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
			
			String destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "cardResult_1.png";
			Highgui.imwrite(destPath, img);
//			Highgui.imwrite("D:\\test\\abc\\card\\card1.png", img);
			
			List<MatOfPoint> contours = new ArrayList<>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			
			
			// 找出轮廓对应凸包的四边形拟合
			List<MatOfPoint> squares = new ArrayList<>();
			List<MatOfPoint> hulls = new ArrayList<>();
			MatOfInt hull = new MatOfInt();
			MatOfPoint2f approx = new MatOfPoint2f();
			approx.convertTo(approx, CvType.CV_32F);

			for (MatOfPoint contour: contours) {
			    // 边框的凸包
			    Imgproc.convexHull(contour, hull);

			    // 用凸包计算出新的轮廓点
			    Point[] contourPoints = contour.toArray();
			    int[] indices = hull.toArray();
			    List<Point> newPoints = new ArrayList<>();
			    for (int index : indices) {
			        newPoints.add(contourPoints[index]);
			    }
			    MatOfPoint2f contourHull = new MatOfPoint2f();
			    contourHull.fromList(newPoints);

			    // 多边形拟合凸包边框(此时的拟合的精度较低)
			    Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true)*0.02, true);

			    // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
			    MatOfPoint approxf1 = new MatOfPoint();
			    approx.convertTo(approxf1, CvType.CV_32S);
			    if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 40000 &&
			            Imgproc.isContourConvex(approxf1)) {
			        double maxCosine = 0;
			        for (int j = 2; j < 5; j++) {
			            double cosine = Math.abs(getAngle(approxf1.toArray()[j%4], approxf1.toArray()[j-2], approxf1.toArray()[j-1]));
			            maxCosine = Math.max(maxCosine, cosine);
			        }
			        // 角度大概72度
			        if (maxCosine < 0.3) {
			            MatOfPoint tmp = new MatOfPoint();
			            contourHull.convertTo(tmp, CvType.CV_32S);
			            squares.add(approxf1);
			            hulls.add(tmp);
			        }
			    }
			}
			
			
			// 找出外接矩形最大的四边形
			int index = findLargestSquare(squares);
			MatOfPoint largest_square = squares.get(index);
			if (largest_square.rows() == 0 || largest_square.cols() == 0)
			    return result;
			
			// 找到这个最大的四边形对应的凸边框，再次进行多边形拟合，此次精度较高，拟合的结果可能是大于4条边的多边形
			MatOfPoint contourHull = hulls.get(index);
			
			
			MatOfPoint2f tmp = new MatOfPoint2f();
			contourHull.convertTo(tmp, CvType.CV_32F);
			Imgproc.approxPolyDP(tmp, approx, 3, true);
			List<Point> newPointList = new ArrayList<>();
			double maxL = Imgproc.arcLength(approx, true) * 0.02;
			
			// 找到高精度拟合时得到的顶点中 距离小于低精度拟合得到的四个顶点maxL的顶点，排除部分顶点的干扰
			for (Point p : approx.toArray()) {
			    if (!(getSpacePointToPoint(p, largest_square.toList().get(0)) > maxL &&
			            getSpacePointToPoint(p, largest_square.toList().get(1)) > maxL &&
			            getSpacePointToPoint(p, largest_square.toList().get(2)) > maxL &&
			            getSpacePointToPoint(p, largest_square.toList().get(3)) > maxL)) {
			        newPointList.add(p);
			    }
			}

			// 找到剩余顶点连线中，边长大于 2 * maxL的四条边作为四边形物体的四条边
			List<double[]> lines = new ArrayList<>();
			for (int i = 0; i < newPointList.size(); i++) {
			    Point p1 = newPointList.get(i);
			    Point p2 = newPointList.get((i+1) % newPointList.size());
			    if (getSpacePointToPoint(p1, p2) > 2 * maxL) {
			        lines.add(new double[]{p1.x, p1.y, p2.x, p2.y});
			        logger.info("p1x:"+p1.x+"  p1y:"+p1.y+"  p2x:"+p2.x+"  p2y:"+p2.y);
			        Core.line(source, new Point(p1.x, p1.y), new Point( p2.x,  p2.y), new Scalar(255, 0, 0),4);
			    }
			}
			
			destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "cardResult_2.png";
			Highgui.imwrite(destPath, source);
//			Highgui.imwrite("D:\\test\\abc\\card\\card2.png", source);
			
			// 计算出这四条边中 相邻两条边的交点，即物体的四个顶点
			List<Point> corners = new ArrayList<>();
			for (int i = 0; i < lines.size(); i++) {
			    Point corner = computeIntersect(lines.get(i),lines.get((i+1) % lines.size()));
			    corners.add(corner);
			}
			
			// 对顶点顺时针排序
			sortCorners(corners);

			// 计算目标图像的尺寸
			Point p0 = corners.get(0);
			Point p1 = corners.get(1);
			Point p2 = corners.get(2);
			Point p3 = corners.get(3);
			logger.info("   "+p0.x+"   "+p0.y);
			logger.info("   "+p1.x+"   "+p1.y);
			logger.info("   "+p2.x+"   "+p2.y);
			logger.info("   "+p3.x+"   "+p3.y);
			double space0 = getSpacePointToPoint(p0, p1);
			double space1 = getSpacePointToPoint(p1, p2);
			double space2 = getSpacePointToPoint(p2, p3);
			double space3 = getSpacePointToPoint(p3, p0);

			double imgWidth = space1 > space3 ? space1 : space3;
			double imgHeight = space0 > space2 ? space0 : space2;
			logger.info("imgWidth:"+imgWidth+"    imgHeight:"+imgHeight);
			// 如果提取出的图片宽小于高，则旋转90度
			if (imgWidth > imgHeight) {
				logger.info("----in");
			    double temp = imgWidth;
			    imgWidth = imgHeight;
			    imgHeight = temp;
			    Point tempPoint = p0.clone();
			    p0 = p1.clone();
			    p1 = p2.clone();
			    p2 = p3.clone();
			    p3 = tempPoint.clone();
			}

//			Mat quad = Mat.zeros((int)imgHeight * 2, (int)imgWidth * 2, CvType.CV_8UC3);
			Mat quad = Mat.zeros((int)imgHeight, (int)imgWidth, CvType.CV_8UC3);

			MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);
//			MatOfPoint2f quadMat = new MatOfPoint2f(new Point(imgWidth*0.4, imgHeight*1.6),
//			        new Point(imgWidth*0.4, imgHeight*0.4),
//			        new Point(imgWidth*1.6, imgHeight*0.4),
//			        new Point(imgWidth*1.6, imgHeight*1.6));
			MatOfPoint2f quadMat = new MatOfPoint2f(new Point(0, 0),
			        new Point(imgWidth, 0),
			        new Point(imgWidth, imgHeight),
			        new Point(0, imgHeight));

			// 提取图像
			Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
			Imgproc.warpPerspective(result, quad, transmtx, quad.size());
			return quad;
			
	 }

	// 根据三个点计算中间那个点的夹角   pt1 pt0 pt2
	 private static double getAngle(Point pt1, Point pt2, Point pt0){
	     double dx1 = pt1.x - pt0.x;
	     double dy1 = pt1.y - pt0.y;
	     double dx2 = pt2.x - pt0.x;
	     double dy2 = pt2.y - pt0.y;
	     return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
	 }
	 
	// 找到最大的正方形轮廓
	 private static int findLargestSquare(List<MatOfPoint> squares) {
	     if (squares.size() == 0)
	         return -1;
	     int max_width = 0;
	     int max_height = 0;
	     int max_square_idx = 0;
	     int currentIndex = 0;
	     for (MatOfPoint square : squares) {
	         Rect rectangle = Imgproc.boundingRect(square);
	         if (rectangle.width >= max_width && rectangle.height >= max_height) {
	             max_width = rectangle.width;
	             max_height = rectangle.height;
	             max_square_idx = currentIndex;
	         }
	         currentIndex++;
	     }
	     return max_square_idx;
	 }
	 
	// 点到点的距离
	 private static double getSpacePointToPoint(Point p1, Point p2) {
	     double a = p1.x - p2.x;
	     double b = p1.y - p2.y;
	     return Math.sqrt(a * a + b * b);
	 }

	 // 两直线的交点
	 private static Point computeIntersect(double[] a, double[] b) {
	     if (a.length != 4 || b.length != 4)
	         throw new ClassFormatError();
	     double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3], x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];
	     double d = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
	     if (d != 0) {
	         Point pt = new Point();
	         pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
	         pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
	         return pt;
	     }
	     else
	         return new Point(-1, -1);
	 }

	// 对多个点按顺时针排序
	 private static void sortCorners(List<Point> corners) {
	     if (corners.size() == 0) return;
	     Point p1 = corners.get(0);
	     int index = 0;
	     for (int i = 1; i < corners.size(); i++) {
	         Point point = corners.get(i);
	         if (p1.x > point.x) {
	             p1 = point;
	             index = i;
	         }
	     }

	     corners.set(index, corners.get(0));
	     corners.set(0, p1);

	     Point lp = corners.get(0);
	     for (int i = 1; i < corners.size(); i++) {
	         for (int j = i + 1; j < corners.size(); j++) {
	             Point point1 = corners.get(i);
	             Point point2 = corners.get(j);
	             if ((point1.y-lp.y*1.0)/(point1.x-lp.x)>(point2.y-lp.y*1.0)/(point2.x-lp.x)) {
	                 Point temp = point1.clone();
	                 corners.set(i, corners.get(j));
	                 corners.set(j, temp);
	             }
	         }
	     }
	 }
	 
	 
	 //答题卡识别
	 public String cardResult(Mat mat){
		 int cutsize = 20;
		 Mat img_cut = mat.submat(cutsize,mat.rows()-cutsize,cutsize,mat.cols()-cutsize);new Mat();
		 Mat img_gray = img_cut.clone();
		 Imgproc.cvtColor(img_cut, img_gray, Imgproc.COLOR_BGR2GRAY);
		 Imgproc.threshold(img_gray,img_gray, 170, 255, Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);
		 Mat temp = img_gray.clone();
//		 	//方式1：通过高斯滤波然后边缘检测膨胀来链接边缘，将轮廓连通便于轮廓识别
//			// 高斯滤波，降噪
//			Imgproc.GaussianBlur(temp, temp, new Size(3,3), 2, 2);
//			// Canny边缘检测
//			Imgproc.Canny(temp, temp, 20, 60, 3, false);
//			// 膨胀，连接边缘
//			Imgproc.dilate(temp, temp, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));
			
			//方式2:使用形态学梯度算法，此算法来保留物体的边缘轮廓很有效果
			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
			Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_GRADIENT, element);
		 Imgproc.threshold(temp, temp, 170, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
		 String destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "cardResult_4.png";
		 Highgui.imwrite(destPath, temp);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card3.png", temp);
		 
//		 logger.info(temp.cols());
		 Mat cut1 = temp.submat(0,temp.rows(),0,(int)(0.275*temp.cols()));
		 Mat cut_gray1 = img_gray.submat(0,img_gray.rows(),0,(int)(0.275*img_gray.cols()));
		 
		 Mat cut2 = temp.submat(0,temp.rows(),(int)(0.275*temp.cols()),(int)(0.518*temp.cols()));
		 Mat cut_gray2 = img_gray.submat(0,img_gray.rows(),(int)(0.275*img_gray.cols()),(int)(0.518*img_gray.cols()));
		 
		 Mat cut3 = temp.submat(0,temp.rows(),(int)(0.518*temp.cols()),(int)(0.743*temp.cols()));
		 Mat cut_gray3 = img_gray.submat(0,img_gray.rows(),(int)(0.518*img_gray.cols()),(int)(0.743*img_gray.cols()));
		 
		 Mat cut4 = temp.submat((int)(0.387*temp.rows()),temp.rows(),(int)(0.743*temp.cols()),temp.cols());
		 Mat cut_gray4 = img_gray.submat((int)(0.387*img_gray.rows()),img_gray.rows(),(int)(0.743*img_gray.cols()),img_gray.cols());
		 //学号
		 Mat cut5 = temp.submat(0,(int)(0.387*temp.rows()),(int)(0.743*temp.cols()),temp.cols());
		 Mat cut_gray5 = img_gray.submat(0,(int)(0.387*img_gray.rows()),(int)(0.743*img_gray.cols()),img_gray.cols());
		 
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut1.png", cut1);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut1_gary.png", cut_gray1);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut2.png", cut2);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut3.png", cut3);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut4.png", cut4);
//		 Highgui.imwrite("D:\\test\\abc\\card\\card_cut5.png", cut5);

		 List<String> resultList = new ArrayList<String>();
		 List<String> list1 = processByCol(cut1,cut_gray1,img_cut,5);
		 List<String> list2 = processByCol(cut2,cut_gray2,img_cut,5);
		 List<String> list3 = processByCol(cut3,cut_gray3,img_cut,4);
		 List<String> list4 = processByCol(cut4,cut_gray4,img_cut,4);
		 //学号单独处理
		 List<String> list5 = processByCol(cut5,cut_gray5,img_cut,4);
		 resultList.addAll(list1);
		 resultList.addAll(list2);
		 resultList.addAll(list3);
		 resultList.addAll(list4);
		 String studentNo = getStudentNo(list5);
		 logger.info("学生学号为："+studentNo);
		 StringBuffer result = new StringBuffer("学生学号为："+studentNo);
		for (int i = 0; i < resultList.size(); i++) {
			result.append(resultList.get(i));
			logger.info(resultList.get(i));
		}
		return result.toString();
		 
	 }

	/**
	 * 根据列单独处理
	 * @Author Songer
	 * @param cut1 传入的答案列，一般1-20一列
	 * @param cut_gary 传入的答案列未处理过
	 * @param temp 表格范围mat
	 * @param toIndex 列答案数，即每几个答案一组
	 * @Date 2018年9月20日
	 * 更新日志
	 * 2018年9月20日 Songer  首次创建
	 *
	 */
	private static List<String> processByCol(Mat cut1,Mat cut_gray,Mat temp,int answerCols) {
		 List<String> result = new ArrayList<String>();
		 List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		 List<MatOfPoint> answerList = new ArrayList<MatOfPoint>();
		 Mat hierarchy = new Mat();
		 Imgproc.findContours(cut1.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//		 logger.info(contours.size());
//		 logger.info("-----w------"+(temp.width()*80/2693-20));
//		 logger.info("-----h------"+(temp.height()*80/2764-20));
		 for(int i = 0;i<contours.size();i++){
     		MatOfPoint mop= contours.get(i);
     		Rect rect = Imgproc.boundingRect(mop);
//     		logger.info("-----------"+rect.width+"	"+rect.height);
     		//一个填图区域大概占整个表格的w:80/2733 h:0/2804,，所以排除掉太小的轮廓和过大的轮廓
//     		Imgproc.drawContours(cut1, contours, i, new Scalar(170), 2);
     		if(rect.width>(temp.width()*80/2693-20) && rect.height>(temp.height()*80/2764-20) && rect.width<temp.width()*0.05 && rect.height<temp.height()*0.05){
//     		if(rect.width>50&&rect.height>50&&rect.area()>2500&&rect.area()<10000){
//     			Core.rectangle(img_cut, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y
//						+ rect.height), new Scalar(0, 255, 0), 2);
     			answerList.add(mop);

     		}
     	}
		
		Collections.sort(answerList, new Comparator<MatOfPoint>() {
			 //按照y坐标升序排列
			@Override
			public int compare(MatOfPoint o1, MatOfPoint o2) {
				Rect rect1 = Imgproc.boundingRect(o1);
				Rect rect2 = Imgproc.boundingRect(o2);
	            if(rect1.y<rect2.y){
	                return -1;
		        }else if(rect1.y>rect2.y){
		            return 1;
		        }else{
		        	return 0;
		        }
			}
		});
		//每4/5个一组组成新list，并按x坐标排序
		int queno = 1;
		int totoSize = answerList.size();
		for (int i = 0; i < totoSize; i+=answerCols) {
			int toIndex = i+answerCols;
			if(toIndex>totoSize){
				toIndex = totoSize-1;
			}
			List<MatOfPoint> newList = answerList.subList(i,toIndex);
			Collections.sort(newList, new Comparator<MatOfPoint>() {
				 //按照y坐标升序排列
				@Override
				public int compare(MatOfPoint o1, MatOfPoint o2) {
					Rect rect1 = Imgproc.boundingRect(o1);
					Rect rect2 = Imgproc.boundingRect(o2);
		            if(rect1.x<rect2.x){
		                return -1;
			        }else if(rect1.x>rect2.x){
			            return 1;
			        }else{
			        	return 0;
			        }
				}
			});
			String resultChoose = "";
			for (int j = 0; j < newList.size(); j++) {
     			Imgproc.drawContours(cut_gray, newList, j, new Scalar(170), 2);
     			//掩模提取出轮廓
     			Mat mask = Mat.zeros(cut_gray.size(), CvType.CV_8UC1);
     			Imgproc.drawContours(mask, newList, j, new Scalar(255), -1);
     			Mat dst = new Mat();
     			Core.bitwise_and(cut_gray, mask, dst);
     			//获取填涂百分比
     			double p100 = Core.countNonZero(dst) * 100 / Core.countNonZero(mask);
     			String anno = index2ColName(j);
     			if(p100>80){
     				resultChoose += anno;
     				logger.info(p100+" 第"+queno+"行:选项（"+anno+"）填涂");
     			}else{
     				logger.info(p100+" 第"+queno+"行:选项（"+anno+"）未填涂");
     			}
//     			Highgui.imwrite("D:\\test\\abc\\card\\card_x"+i+j+".png", dst);
     			if(i==0&&j==0){
     				String destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "cardResult_5.png";
     				Highgui.imwrite(destPath, dst);
     			}
			}
			result.add(resultChoose);
			queno++;
		}
		
//		 Highgui.imwrite("D:\\test\\abc\\card\\card4.png", cut1);
		 return result;
	}

	//编号转答案0-A 1-B
	public static String index2ColName(int index){
	        if (index < 0) {
	            return null;
	        }
	        int num = 65;// A的Unicode码
	        String colName = "";
	        do {
	            if (colName.length() > 0) {
	                index--;
	            }
	            int remainder = index % 26;
	            colName = ((char) (remainder + num)) + colName;
	            index = (int) ((index - remainder) / 26);
	        } while (index > 0);
	        return colName;
	  }
	
	  /**根据表元的列名转换为列号
     * @param colName 列名, 从A开始
     * @return A1->0; B1->1...AA1->26
     */
    public static int colName2Index(String colName) {
        int index = -1;
        int num = 65;// A的Unicode码
        int length = colName.length();
        for (int i = 0; i < length; i++) {
            char c = colName.charAt(i);
            if (Character.isDigit(c)) break;// 确定指定的char值是否为数字
            index = (index + 1) * 26 + (int)c - num;
        }
        return index;
    }
	
    /**
     * 根据返回的结果集转换为学号
     * 因为学号部分的处理跟答案一样是一横行进行处理的，同时返回的是ABCDE等选项结果
     * 转换公式为：学生学号=遍历list的index值*（A=1000,B=100,C=10,D=1）相加
     * @Author Songer
     * @param resultList
     * @return String
     * @Date 2018年9月20日
     * 更新日志
     * 2018年9月20日 Songer  首次创建
     *
     */
    public static String getStudentNo(List<String> resultList){
    	int studentNo = 0;
    	for (int i = 0; i < resultList.size(); i++) {
    		String result = resultList.get(i);
    		if (result.contains("A")) {
				studentNo += 1000 * i;
			} 
			if (result.contains("B")) {
				studentNo += 100 * i;
			} 
			if (result.contains("C")) {
				studentNo += 10 * i;
			}
			if (result.contains("D")) {
				studentNo += i;
			}
		}
    	NumberFormat formatter = new DecimalFormat("0000");
        String number = formatter.format(studentNo);
    	return number;
    }

}
