package jp.naklab.assu.android.android_myownfont;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

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

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.b_character_small);
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
//        Imgproc.Canny(mat, mat, 110, 130);

        Mat mHierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);

        // These lines are in function onCameraFrame
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


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
        String out = "<glyph d=\"";
        for (int j = 0; j < contours.size(); j++) {
            // 画像の輪郭はスキップ
            if (j == 0) continue;
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
