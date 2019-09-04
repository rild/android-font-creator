package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVEdgeDetector {
    public void onImageScan(Context context) {
        /* ①：画像読み込み */
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.a_character_image_small);
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);

        /* ②：画像を二値化 */
//        mat = getThreshold(mat);

        /* ③：輪郭の座標を取得 */
//        List<MatOfPoint> contours = getContour(mat);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(mat, mat, 100, 200);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Mat hierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        List<List<Point>> points = contour2point(contours);
    }

    public Mat matImageScan(Context context) {
        /* ①：画像読み込み */
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.a_character_image_small);
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);

        /* ②：画像を二値化 */
        mat = getThreshold(mat);

        /* ③：輪郭の座標を取得 */
        List<MatOfPoint> contours = getContour(mat);
        List<List<Point>> points = contour2point(contours);

        return mat;
    }

    public void onImageScan(Bitmap bitmap) {
        /* ①：画像読み込み */
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat, true);

        /* ②：画像を二値化 */
        mat = getThreshold(mat);

        /* ③：輪郭の座標を取得 */
        List<MatOfPoint> contours = getContour(mat);
        List<List<Point>> points = contour2point(contours);

        for (int i = 0; i < contours.size(); i++) {
            DebugUtils.print("Countours", contours.get(i).toString());
        }
    }

    private Mat getThreshold(Mat mat) {
        /* ②-1-1：RGB空間チャネルの取得 */
        Mat mat_rgb = mat.clone();
        List<Mat> channels_rgb = new ArrayList<Mat>();
        Core.split(mat_rgb, channels_rgb);

        /* ②-1-2：RGB空間 → グレースケール変換 → 二値化 */
        Imgproc.cvtColor(mat_rgb, mat_rgb, Imgproc.COLOR_RGB2GRAY);
        Core.subtract(channels_rgb.get(0), mat_rgb, channels_rgb.get(0));
        Core.subtract(channels_rgb.get(1), mat_rgb, channels_rgb.get(1));
        Core.subtract(channels_rgb.get(2), mat_rgb, channels_rgb.get(2));
        Imgproc.threshold(channels_rgb.get(0), channels_rgb.get(0), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_rgb.get(1), channels_rgb.get(1), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_rgb.get(2), channels_rgb.get(2), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        /* ②-1-3：RGBの輪郭を取得 */
        List<MatOfPoint> contour_rgb0 = getContour(channels_rgb.get(0));
        List<MatOfPoint> contour_rgb1 = getContour(channels_rgb.get(1));
        List<MatOfPoint> contour_rgb2 = getContour(channels_rgb.get(2));


        /* ②-2-1：YUV空間チャネルの取得 */
        Mat mat_yuv = mat.clone();
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_BGR2YUV);
        List<Mat> channels_yuv = new ArrayList<Mat>();
        Core.split(mat_yuv, channels_yuv);

        /* ②-2-2：YUV空間 → グレースケール変換 → 二値化 */
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_RGB2GRAY);
        Core.subtract(channels_yuv.get(0), mat_yuv, channels_yuv.get(0));
        Core.subtract(channels_yuv.get(1), mat_yuv, channels_yuv.get(1));
        Core.subtract(channels_yuv.get(2), mat_yuv, channels_yuv.get(2));
        Imgproc.threshold(channels_yuv.get(0), channels_yuv.get(0), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_yuv.get(1), channels_yuv.get(1), 0.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_yuv.get(2), channels_yuv.get(2), 0.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        /* ②-2-3：YUVの輪郭を取得 */
        List<MatOfPoint> contour_yuv0 = getContour(channels_yuv.get(0));
        List<MatOfPoint> contour_yuv1 = getContour(channels_yuv.get(1));
        List<MatOfPoint> contour_yuv2 = getContour(channels_yuv.get(2));


        /* ②-3：マスク画像に輪郭を合成 */
        Mat mat_mask = new Mat(mat.size(), CvType.CV_8UC4, Scalar.all(255));
        Scalar color = new Scalar(0, 0, 0);
        Imgproc.drawContours(mat_mask, contour_rgb0, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_rgb1, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_rgb2, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv0, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv1, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv2, -1, color, -1);

        Imgproc.cvtColor(mat_mask, mat_mask, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat_mask, mat_mask, 128.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        return mat_mask;
    }

    private List<MatOfPoint> getContour(Mat mat) {
        List<MatOfPoint> contour = new ArrayList<MatOfPoint>();

        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Mat hierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);
        Imgproc.findContours(mat, contour, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

//        List<MatOfPoint> tmp_contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(mat, tmp_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//        for (int i = 0; i < tmp_contours.size(); i++) {
//            MatOfPoint2f ptmat2 = new MatOfPoint2f(tmp_contours.get(i).toArray());
//            MatOfPoint2f approx = new MatOfPoint2f();
//            MatOfPoint approxf1 = new MatOfPoint();
//
//            /* 輪郭線の周囲長を取得 */
//            double arclen = Imgproc.arcLength(ptmat2, true);
//            /* 直線近似 */
//            Imgproc.approxPolyDP(ptmat2, approx, 0.02 * arclen, true);
//            approx.convertTo(approxf1, CvType.CV_32S);
//
//            /* 輪郭情報を登録 */
//            contour.add(approxf1);
//        }

        return contour;
    }

    private List<List<Point>> contour2point(List<MatOfPoint> contour) {
        List<List<Point>> points = new ArrayList<List<Point>>();
        for (int i = 0; i < contour.size(); i++) {
            points.add(contour.get(i).toList());
        }
        return points;
    }

    public String makeGlyphString(Bitmap bmp) {
        return makeGlyphString(detectEdgePoints(bmp), "uni22", 584, "&#x22;");
    }

    // 画像から path を抽出する: bmp → points
    private List<List<Point>> detectEdgePoints(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(mat, mat, 110, 130);

        Mat mHierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);

        // These lines are in function onCameraFrame
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        List<List<Point>> points = contour2point(contours);
        return points;
    }

    // path から svg の文字列を生成する: points → svg string
    private String makeGlyphString(List<List<Point>> points, String glyphName, int horizAdvX, String unicode) {
        String out = "<glyph d=\"";
        for (int j = 0; j < points.size(); j++) {
            out = out + "M";
            List<Point> pointList = points.get(j);
            for (int i = 0; i < pointList.size(); i++) {
                out = out + pointList.get(i).x + " " + pointList.get(i).y + " ";
            }

        }
        out = out + "\" glyph-name=\"" + glyphName +
                "\" horiz-adv-x=\"" + horizAdvX +
                "\" unicode=\"" + unicode +
                "\" vert-adv-y=\"1000\" />";
        return out;
    }

    public Bitmap makeGlyphBitmap(Context context, Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(mat, mat, 110, 130);

//        Mat mHierarchy = new Mat();
        Mat mHierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);

// These lines are in function onCameraFrame
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        Bitmap outBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.background_green);
        Mat out = new Mat(mat.size(), mat.type());
        Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
        Imgproc.drawContours(out, contours, -1, CONTOUR_COLOR);
        Utils.matToBitmap(out, outBmp);
        return outBmp;
    }
}
