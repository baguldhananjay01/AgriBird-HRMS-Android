package com.agribird.hrmsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;

public class CameraActivity extends AppCompatActivity {

    PreviewView previewView;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;

    private boolean eyesWereOpen=false;
    private boolean blinkDetected=false;
    private boolean photoCaptured=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        previewView=findViewById(R.id.previewView);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){

            startCamera();
        }else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    101
            );

        }

        FaceDetectorOptions options=new FaceDetectorOptions.Builder()
                .setPerformanceMode(
                        FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build();

        faceDetector = FaceDetection.getClient(options);

    }

    @ExperimentalGetImage
    private void startCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                imageCapture=new ImageCapture.Builder().build();


                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();


                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageAnalysis.setAnalyzer(
                        getMainExecutor(),
                        image -> {

                            if (image.getImage() == null) {
                                image.close();
                                return;
                            }

                            InputImage inputImage =
                                    InputImage.fromMediaImage(
                                            image.getImage(),
                                            image.getImageInfo().getRotationDegrees()
                                    );

                            faceDetector.process(inputImage)
                                    .addOnSuccessListener(faces -> {

                                        if (faces.isEmpty()) {

                                            Log.d("FaceDetection", "No Face");

                                        } else {

                                            Face face = faces.get(0);
                                            Log.d("FaceDetection", "Face Detected");

                                            Float leftEye = face.getLeftEyeOpenProbability();
                                            Float rightEye = face.getRightEyeOpenProbability();

                                            //Log.d("Eye", "Left = " + leftEye + " Right = " + rightEye);

                                            if(leftEye==null || rightEye==null){
                                                image.close();
                                                return;
                                            }
                                            if(leftEye>0.7f && rightEye>0.6f){
                                                eyesWereOpen=true;
                                            }

                                            if(eyesWereOpen && leftEye<0.25f && rightEye<0.25f){
                                                blinkDetected=true;
                                            }
                                            if(blinkDetected && !photoCaptured){
                                                photoCaptured=true;
                                                Log.d("Blink", "Blink Detected");
                                                capturePhoto();
                                            }

                                        }

                                        image.close();

                                    })
                                    .addOnFailureListener(e -> {

                                        Log.e("FaceDetection", String.valueOf(e.getMessage()));
                                        image.close();
                                    });
                        });



                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, getMainExecutor());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startCamera();

            } else {

                Toast.makeText(this,
                        "Camera Permission Required",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    }
    private void capturePhoto() {

        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(
                getExternalFilesDir(null),
                "selfie_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile)
                        .build();

        imageCapture.takePicture(
                outputOptions,
                getMainExecutor(),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Log.d("Capture", "Photo Saved : " + photoFile.getAbsolutePath());

                        Intent intent=new Intent();
                        intent.putExtra("SELFIE_VERIFIED",true);
                        intent.putExtra("PHOTO_PATH",photoFile.getAbsolutePath());
                        setResult(RESULT_OK,intent);
                        finish();

                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception) {

                        Log.e("Capture", exception.getMessage());

                    }
                });
    }
}
