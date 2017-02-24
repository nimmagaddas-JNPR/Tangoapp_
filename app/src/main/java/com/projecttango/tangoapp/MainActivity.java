//Created by Ramya on 11/4/2016
package com.projecttango.tangoapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.BORDER_DEFAULT;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny, imgSobel, imgthresh;
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                }
            }
        }
    };

    static {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        javaCameraView = (JavaCameraView) findViewById(R.id.JavaCameraView);
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        Button button = (Button) findViewById(R.id.capture_button);


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Do something in response to button click

                // Take the screenshot

                Bitmap screenShot = TakeScreenShot(rl);

                // Save the screenshot on device gallery
                MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        screenShot,
                        "Image.png",
                        "Captured ScreenShot"
                );

                // Notify the user that screenshot taken.
                Toast.makeText(getApplicationContext(), "Screen Captured", Toast.LENGTH_LONG).show();


            }
        });
    }




    private Bitmap TakeScreenShot(View JavaCameraView) {
        // Screenshot taken for the specified root view and its child elements.

        Bitmap bitmap = Bitmap.createBitmap(JavaCameraView.getWidth(), JavaCameraView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        JavaCameraView.draw(canvas);
        return bitmap;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV  loaded successfully");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
        imgSobel = new Mat(height, width, CvType.CV_8UC1);
        imgthresh = new Mat(height, width, CvType.CV_8UC1);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // Converts RGB to Gray scale iamge

        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);



        //Canny is for detection of edges especially in noise state by applying thresholding method.
        Imgproc.Canny(imgGray, imgCanny, 50, 150);

        // Sobel operator is it can detect edges and their orientations.
        //Sobel( input img, output img, ddepth, x_order, y_order, scale, delta, BORDER_DEFAULT );
        Imgproc.Sobel(imgGray, imgSobel, -1, 0, 1, 3, 1, 0, BORDER_DEFAULT);

        //Thresolding means dividing the complete image into a set of pixels in such a way that the pixels in each set have some common characteristics.
        // Image segmentation is highly useful in defining objects and their boundaries.
        Imgproc.threshold(imgGray, imgthresh, 127, 255, Imgproc.THRESH_BINARY);

        return imgthresh;
    }






}