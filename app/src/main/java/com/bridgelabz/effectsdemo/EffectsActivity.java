package com.bridgelabz.effectsdemo;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bridgeit on 28/10/16.
 */

public class EffectsActivity extends Activity {
    private ImageView imageView;
    private Paint rectPaint;
    private Bitmap defaultBitmap;
    private Bitmap temporaryBitmap;
    private Bitmap eyePatchBitmap;
    private Canvas canvas;
    private int mPICK_FROM_FILE;
    private Uri mImageCaptureUri;
    private Cursor cursor;

  //  private RecyclerView recyclerView;
    private List<Integer> imageList = new ArrayList<>();
   // private ImageAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects);

        imageView = (ImageView) findViewById(R.id.image_view);

        // Extract parameters
        Bundle extras = getIntent().getExtras();
        mPICK_FROM_FILE = extras.getInt("PICK_FROM_FILE");
        mImageCaptureUri = extras.getParcelable("ImageCaptureUri");
        Log.i("Effects mPICK_FROM_FILE", "onCreate: .................................."+mPICK_FROM_FILE);
        Log.i("Effects imageCaptureUri", "onCreate: .................................."+mImageCaptureUri);

        /*recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mImageAdapter = new ImageAdapter(this,imageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mImageAdapter);
        prepareImageData();*/

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImage(imageView);
            }
        });
    }

    private void prepareImageData() {
        imageList.add(0, R.drawable.eye_patch);
        imageList.add(1, R.drawable.eye_patch);
        imageList.add(2, R.drawable.eye_patch);
        imageList.add(3, R.drawable.eye_patch);
      //  mImageAdapter.notifyDataSetChanged();
    }

    public void processImage(View view) {

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inMutable = true;

        initializeBitmap(bitmapOptions);
        createRectanglePaint();

        canvas = new Canvas(temporaryBitmap);
        canvas.drawBitmap(defaultBitmap, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face Detector could not be set up on your device :(")
                    .show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(defaultBitmap).build();
            SparseArray<Face> sparseArray = faceDetector.detect(frame);

            detectFaces(sparseArray);

           // recyclerView.setVisibility(View.VISIBLE);

            imageView.setImageDrawable(new BitmapDrawable(getResources(), temporaryBitmap));

            faceDetector.release();
        }
    }

    private void initializeBitmap(BitmapFactory.Options bitmapOptions) {
       /* defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.images_with_faces,
                bitmapOptions);*/
        //////////////////////////////////////////////////////
        // Let's read picked image path using content resolver
        String[] filePath = { MediaStore.Images.Media.DATA };
        Log.i("Effects filePath", "run: ......................................"+filePath);
        cursor = getContentResolver().query(mImageCaptureUri, filePath,
                null, null, null);
        Log.i("Effects cursor", "run: ................................."+cursor);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
        Log.i("Effects imagePath", "run: ...................................."+imagePath);
        cursor.close();
        defaultBitmap = BitmapFactory.decodeFile(imagePath);
        Log.i("Effects defaultBitmap", "run: ..............................."+defaultBitmap);
        //////////////////////////////////////////////////////////
        temporaryBitmap = Bitmap.createBitmap(defaultBitmap.getWidth(), defaultBitmap
                .getHeight(), Bitmap.Config.RGB_565);
        eyePatchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eye_patch,
                bitmapOptions);
    }

    private void createRectanglePaint() {
        rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.CYAN);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    private void detectFaces(SparseArray<Face> sparseArray) {

        for (int i = 0; i < sparseArray.size(); i++) {
            Face face = sparseArray.valueAt(i);

            float left = face.getPosition().x;
            float top = face.getPosition().y;
            float right = left + face.getWidth();
            float bottom = right + face.getHeight();
            float cornerRadius = 2.0f;

            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, rectPaint);

            detectLandmarks(face);
        }
    }

    private void detectLandmarks(Face face) {
        for (Landmark landmark : face.getLandmarks()) {

            int cx = (int) (landmark.getPosition().x);
            int cy = (int) (landmark.getPosition().y);

         //   canvas.drawCircle(cx, cy, 10, rectPaint);

          //  drawLandmarkType(landmark.getType(), cx, cy);

            drawEyePatchBitmap(landmark.getType(), cx, cy);
        }
    }

    private void drawLandmarkType(int landmarkType, float cx, float cy) {
        String type = String.valueOf(landmarkType);
        rectPaint.setTextSize(50);
        canvas.drawText(type, cx, cy, rectPaint);
    }

    private void drawEyePatchBitmap(int landmarkType, float cx, float cy) {

        if (landmarkType == 4) {
            // TODO: Optimize so that this calculation is not done for every face
            int scaledWidth = eyePatchBitmap.getScaledWidth(canvas);
            int scaledHeight = eyePatchBitmap.getScaledHeight(canvas);
            canvas.drawBitmap(eyePatchBitmap, cx - (scaledWidth / 2), cy - (scaledHeight / 2), null);
        }
    }
}
