package jp.naklab.assu.android.android_myownfont;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVEdgeDetectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvedge_detection);

        // これがないと n_Mat の読み込み？で落ちる
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "Failed");
        } else {
            Log.i("OpenCV", "successfully built !");
        }

        ImageView imageView = findViewById(R.id.imageview);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.equal_character_image_small);
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

//        List<MatOfPoint> tmp_contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(mat, tmp_contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//        for (int i = 0; i < tmp_contours.size(); i++) {
//            MatOfPoint2f ptmat2 = new MatOfPoint2f(tmp_contours.get(i).toArray());
//            MatOfPoint2f approx = new MatOfPoint2f();
//            MatOfPoint approxf1 = new MatOfPoint();
//
//            /* 輪郭線の周囲長を取得 */
//            double arclen = Imgproc.arcLength(ptmat2, true);
//            /* 直線近似 */
//            Imgproc.approxPolyDP(ptmat2, approx, 0.05 * arclen, false);
//            approx.convertTo(approxf1, CvType.CV_32S);
//
//            /* 輪郭情報を登録 */
//            contours.add(approxf1);
//        }


//        Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
//        Imgproc.drawContours(mat, contours, -1, CONTOUR_COLOR);


        Bitmap outBmp = BitmapFactory.decodeResource(getResources(), R.drawable.background_green);
        Mat out = new Mat(mat.size(), mat.type());
        Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
        Imgproc.drawContours(out, contours, -1, CONTOUR_COLOR);
        Utils.matToBitmap(out, outBmp);
        imageView.setImageBitmap(outBmp);

        List<List<Point>> points = contour2point(contours);
        String outString = parseContours2Path(points);
    }

    private List<List<Point>> contour2point(List<MatOfPoint> contour) {
        List<List<Point>> points = new ArrayList<List<Point>>();
        for (int i = 0; i < contour.size(); i++) {
            points.add(contour.get(i).toList());
        }
        return points;
    }

    private String parseContours2Path(List<List<Point>> contours) {

        MatOfPoint2f a;
        String out = "<glyph d=\"";
        for (int j = 0; j < contours.size(); j++) {
            out = out + "M";
            List<Point> points = contours.get(j);
            for (int i = 0; i < points.size(); i++) {
                out = out + points.get(i).x + " " + points.get(i).y + " ";
            }

        }

        String glyphName = "uni22";
        int horizAdvX = 584;
        String unicode = "&#x22;";
        out = out + "\" glyph-name=\"" + glyphName +
                "\" horiz-adv-x=\"" + horizAdvX +
                "\" unicode=\"" + unicode +
                "\" vert-adv-y=\"1000\" />";
        return out;
    }



}
