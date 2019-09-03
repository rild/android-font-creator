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

        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "Failed");
        } else {
            Log.i("OpenCV", "successfully built !");
        }

        ImageView imageView = findViewById(R.id.imageview);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a_character_image_small);
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(mat, mat, 110, 130);

//        Mat mHierarchy = new Mat();
        Mat mHierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);
        Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

// These lines are in function onCameraFrame
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        Imgproc.drawContours(mat, contours, -1, CONTOUR_COLOR);
        Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.background_green);
        Utils.matToBitmap(mat, bmp2);
        imageView.setImageBitmap(bmp2);
    }
}
