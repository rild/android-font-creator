package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
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

    private List<List<Point>> contour2point(List<MatOfPoint> contour) {
        List<List<Point>> points = new ArrayList<List<Point>>();
        for (int i = 0; i < contour.size(); i++) {
            points.add(contour.get(i).toList());
        }
        return points;
    }

    // test method
    public String makeGlyphString(Bitmap bmp) {
        return makeGlyphString(detectEdgePoints(bmp), "uni22", 584, "&#x22;");
    }

    public String makeGlyphString(Bitmap bmp, String glyphName, int horizAdvX, String unicode) {
        return makeGlyphString(detectEdgePoints(bmp), glyphName, horizAdvX, unicode);
    }

    // 画像から path を抽出する: bmp → points
    private List<List<Point>> detectEdgePoints(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.Canny(mat, mat, 110, 130);

        Mat mHierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);

        // These lines are in function onCameraFrame
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<List<Point>> points = contour2point(contours);
        return points;
    }


    // path から svg の文字列を生成する: points → svg string
    private String makeGlyphString(List<List<Point>> points, String glyphName, int horizAdvX, String unicode) {

        String out = "<glyph d=\"";
        for (int j = 0; j < points.size(); j++) {
            if (j == 0) continue; // 外周はスキップ
            out = out + "M";
            List<Point> pointList = points.get(j);
            for (int i = 0; i < pointList.size(); i++) {
                // Android のy座標系は下向きなのに対して、font-svg の glyph では上向き座標系なので座標系の変換を行う
                pointList.get(i).y = pointList.get(i).y * -1 + 1000;
                out = out + pointList.get(i).x + " " + pointList.get(i).y + " ";
            }

        }
        out = out + "Z"; // 終端文字？
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
