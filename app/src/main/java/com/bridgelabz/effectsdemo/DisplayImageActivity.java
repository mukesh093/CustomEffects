package com.bridgelabz.effectsdemo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;

/**
 * Created by bridgeit on 26/10/16.
 */

public class DisplayImageActivity extends Activity {

    private View imageCrop, imageCrop1;
    private int viewHeight, viewWidth;
    private android.media.FaceDetector myFaceDetect;
    private android.media.FaceDetector.Face[] myFace;
    private float myEyesDistance;
    private Cursor cursor;
    private int mPICK_FROM_FILE;
    private Uri mImageCaptureUri;

    private CameraSource mCameraSource = null;
    String imagePath;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image);

        imageCrop1 = (View) findViewById(R.id.display_image1);
        imageCrop = (View) findViewById(R.id.imageCrop);

        // Extract parameters
        Bundle extras = getIntent().getExtras();
        mPICK_FROM_FILE = extras.getInt("PICK_FROM_FILE");
        mImageCaptureUri = extras.getParcelable("ImageCaptureUri");
        Log.i("mPICK_FROM_FILE", "onCreate: .................................."+mPICK_FROM_FILE);
        Log.i("mImageCaptureUri", "onCreate: .................................."+mImageCaptureUri);

        // Proceed as normal...

        Log.i("crop image", "onCreate: ..................................");
        FaceTrackerActivity faceTrackerActivity = new FaceTrackerActivity();
        faceTrackerActivity.selectPicture(this, mPICK_FROM_FILE);
        final Intent data = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        data.setType("image/*");

        ViewTreeObserver vto = imageCrop1.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewWidth = imageCrop1.getMeasuredWidth();
                viewHeight = imageCrop1.getMeasuredHeight();
                Log.i("imageCrop1 height.....", "run: ................................."+viewHeight);
                Log.i("imageCrop1 width......", "run: ................................."+viewWidth);
                // handle viewWidth here...
                try {

                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    //////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Log.i("filePath ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);
                    Log.i("cursor", "run: ................................."+cursor);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Log.i("imagePath", "run: ...................................."+imagePath);
                    cursor.close();
                    Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
                    Log.i("bitmapOrg", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    int targetWidth = bitmapOrg.getWidth();
                    int targetHeight = bitmapOrg.getHeight();

                    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                            targetHeight, Bitmap.Config.ARGB_8888);

                    RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

                    Canvas canvas = new Canvas(targetBitmap);
                    Path path = new Path();

                    path.addRect(rectf, Path.Direction.CW);
                    canvas.clipPath(path);

                    canvas.drawBitmap(
                            bitmapOrg,
                            new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg
                                    .getHeight()), new Rect(0, 0, targetWidth,
                                    targetHeight), paint);

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);

                    BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                    bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    ///////////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    filePath = new String[]{MediaStore.Images.Media.DATA};
                    Log.i("filePath1 ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);

                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    // imagePath = mImageCaptureUri.getPath();
                    Log.i("imagePath1..", "run: ...................................."+imagePath);
                    cursor.close();
                    bitmapOrg = BitmapFactory.decodeFile(imagePath,bitmapFactoryOptions);
                    Log.i("bitmapOrg1 ..", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    myFace = new android.media.FaceDetector.Face[5];
                    myFaceDetect = new android.media.FaceDetector(targetWidth, targetHeight,
                            5);
                    int numberOfFaceDetected = myFaceDetect.findFaces(
                            bitmapOrg, myFace);
                    Bitmap resizedBitmap = null;
                    if (numberOfFaceDetected > 0) {
                        PointF myMidPoint = null;
                        android.media.FaceDetector.Face face = myFace[0];
                        myMidPoint = new PointF();
                        face.getMidPoint(myMidPoint);
                        myEyesDistance = face.eyesDistance();

                        if (myMidPoint.x + viewWidth > targetWidth) {
                            while (myMidPoint.x + viewWidth > targetWidth) {
                                myMidPoint.x--;
                            }
                        }
                        if (myMidPoint.y + viewHeight > targetHeight) {
                            while (myMidPoint.y + viewHeight > targetHeight) {
                                myMidPoint.y--;
                            }
                        }
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg,
                                (int) (myMidPoint.x - myEyesDistance),
                                (int) (myMidPoint.y - myEyesDistance),
                                viewWidth, viewHeight, matrix, true);
                    } else {
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                viewWidth, viewHeight, matrix, true);
                    }

                    BitmapDrawable bd = new BitmapDrawable(resizedBitmap);
                    imageCrop1.setBackgroundDrawable(bd);

                } catch (Exception e) {
                    System.out.println("Error1 : " + e.getMessage()
                            + e.toString());
                }
            }
        });

        vto = imageCrop.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewWidth = imageCrop.getMeasuredWidth();
                viewHeight = imageCrop.getMeasuredHeight();
                Log.i("imageCrop1 height.....", "run: ................................." + viewHeight);
                Log.i("imageCrop1 width......", "run: ................................." + viewWidth);
                // handle viewWidth here...
                try {

                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);

                    //////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Log.i("filePath ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);
                    Log.i("cursor", "run: ................................."+cursor);
                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Log.i("imagePath", "run: ...................................."+imagePath);
                    cursor.close();
                    Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
                    Log.i("bitmapOrg", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    int targetWidth = bitmapOrg.getWidth();
                    int targetHeight = bitmapOrg.getHeight();
                    Log.i("bitmapOrg height", "run: ..............................."+targetHeight);
                    Log.i("bitmapOrg width", "run: ..............................."+targetWidth);

                    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                            targetHeight, Bitmap.Config.ARGB_8888);

                    RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

                    Canvas canvas = new Canvas(targetBitmap);
                    Path path = new Path();

                    path.addRect(rectf, Path.Direction.CW);
                    canvas.clipPath(path);

                    canvas.drawBitmap(bitmapOrg,
                            new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg
                                    .getHeight()), new Rect(0, 0, targetWidth,
                                    targetHeight), paint);

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);

                    BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                    bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    ///////////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    filePath = new String[]{MediaStore.Images.Media.DATA};
                    Log.i("filePath1 ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);

                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    // imagePath = mImageCaptureUri.getPath();
                    Log.i("imagePath1..", "run: ...................................."+imagePath);
                    cursor.close();
                    bitmapOrg = BitmapFactory.decodeFile(imagePath,bitmapFactoryOptions);
                    Log.i("bitmapOrg1 ..", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    myFace = new android.media.FaceDetector.Face[5];
                    myFaceDetect = new android.media.FaceDetector(targetWidth, targetHeight,
                            5);
                    int numberOfFaceDetected = myFaceDetect.findFaces(
                            bitmapOrg, myFace);
                    Bitmap resizedBitmap = null;
                    if (numberOfFaceDetected > 0) {
                        PointF myMidPoint = null;
                        android.media.FaceDetector.Face face = myFace[0];
                        myMidPoint = new PointF();
                        face.getMidPoint(myMidPoint);
                        myEyesDistance = face.eyesDistance() + 20;

                        if (myMidPoint.x + viewWidth > targetWidth) {
                            while (myMidPoint.x + viewWidth > targetWidth) {
                                myMidPoint.x--;
                            }
                        }
                        if (myMidPoint.y + viewHeight > targetHeight) {
                            while (myMidPoint.y + viewHeight > targetHeight) {
                                myMidPoint.y--;
                            }
                        }
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg,
                                (int) (myMidPoint.x - myEyesDistance),
                                (int) (myMidPoint.y - myEyesDistance),
                                viewWidth, viewHeight, matrix, true);
                    } else {
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                viewWidth, viewHeight, matrix, true);
                    }

                    BitmapDrawable bd = new BitmapDrawable(resizedBitmap);

                    //for getting circular image
                  /*  imageCrop.setBackgroundDrawable(new BitmapDrawable(
                            getCroppedBitmap(bd.getBitmap())));*/

                    //for getting square image
                     imageCrop.setBackgroundDrawable(bd);



                } catch (Exception e) {
                   /* ImageView i = (ImageView) imageCrop;
                    i.setImageBitmap(BitmapFactory.decodeFile(imagePath));*/
                    System.out.println("Error1 : " + e.getMessage()
                            + e.toString());
                }
            }
        });

        findViewById(R.id.addEffects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build extras with passed in parameters
                Bundle extras = new Bundle();
                extras.putInt("PICK_FROM_FILE", mPICK_FROM_FILE);
                extras.putParcelable("ImageCaptureUri", mImageCaptureUri);

                Intent i = new Intent(DisplayImageActivity.this, EffectsActivity.class);
                i.putExtras(extras);
                startActivity(i);
            }
        });

        findViewById(R.id.saveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCroppedImage();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {

        int targetWidth = bitmap.getWidth();
        int targetHeight = bitmap.getHeight();
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth), ((float) targetHeight)) /    2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = bitmap;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
                sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
                targetHeight), null);
        return targetBitmap;

    }

    private void saveCroppedImage() {
        File imageFile;

        imageCrop.setDrawingCacheEnabled(true);
        imageCrop.buildDrawingCache();
        Bitmap bitmap = imageCrop.getDrawingCache();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] bytes=stream.toByteArray();
        Log.i("byte[] ", "onClick: ....................................."+bytes.length);

        try {
            // convert byte array into bitmap
            Bitmap loadedImage = null;
            Bitmap rotatedBitmap = null;
            loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                    bytes.length);

            Matrix rotateMatrix = new Matrix();

            rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                    loadedImage.getWidth(), loadedImage.getHeight(),
                    rotateMatrix, false);

            File dir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "MyPhotos");

            boolean success = true;
            if (!dir.exists())
            {
                success = dir.mkdirs();
            }
            if (success) {
                java.util.Date date = new java.util.Date();
                imageFile = new File(dir.getAbsolutePath()
                        + File.separator
                        + new Timestamp(date.getTime()).toString()
                        + "Image.jpg");

                imageFile.createNewFile();
                Toast.makeText(getBaseContext(), "Image Captured",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Image Not saved",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            // save image into gallery
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());

            DisplayImageActivity.this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //saveToInternalStorage(loadedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

    /* imageCrop1.post(new Runnable() {
            @Override
            public void run() {
                viewHeight = 300;
                viewWidth = 500;
              //    viewHeight = imageCrop1.getMeasuredHeight();
             //     viewWidth = imageCrop1.getMeasuredWidth();
                Log.i("imageCrop1 height..", "run: ................................."+viewHeight);
                Log.i("imageCrop1 width..", "run: ................................."+viewWidth);
                try {

                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    //////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Log.i("filePath ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);
                    Log.i("cursor", "run: ................................."+cursor);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Log.i("imagePath", "run: ...................................."+imagePath);
                    cursor.close();
                    Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
                    Log.i("bitmapOrg", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    int targetWidth = bitmapOrg.getWidth();
                    int targetHeight = bitmapOrg.getHeight();

                    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                            targetHeight, Bitmap.Config.ARGB_8888);

                    RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

                    Canvas canvas = new Canvas(targetBitmap);
                    Path path = new Path();

                    path.addRect(rectf, Path.Direction.CW);
                    canvas.clipPath(path);

                    canvas.drawBitmap(
                            bitmapOrg,
                            new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg
                                    .getHeight()), new Rect(0, 0, targetWidth,
                                    targetHeight), paint);

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);

                    BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                    bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    ///////////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    filePath = new String[]{MediaStore.Images.Media.DATA};
                    Log.i("filePath1 ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);

                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    // imagePath = mImageCaptureUri.getPath();
                    Log.i("imagePath1..", "run: ...................................."+imagePath);
                    cursor.close();
                    bitmapOrg = BitmapFactory.decodeFile(imagePath,bitmapFactoryOptions);
                    Log.i("bitmapOrg1 ..", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    myFace = new android.media.FaceDetector.Face[5];
                    myFaceDetect = new android.media.FaceDetector(targetWidth, targetHeight,
                            5);
                    int numberOfFaceDetected = myFaceDetect.findFaces(
                            bitmapOrg, myFace);
                    Bitmap resizedBitmap = null;
                    if (numberOfFaceDetected > 0) {
                        PointF myMidPoint = null;
                        android.media.FaceDetector.Face face = myFace[0];
                        myMidPoint = new PointF();
                        face.getMidPoint(myMidPoint);
                        myEyesDistance = face.eyesDistance();

                        if (myMidPoint.x + viewWidth > targetWidth) {
                            while (myMidPoint.x + viewWidth > targetWidth) {
                                myMidPoint.x--;
                            }
                        }
                        if (myMidPoint.y + viewHeight > targetHeight) {
                            while (myMidPoint.y + viewHeight > targetHeight) {
                                myMidPoint.y--;
                            }
                        }
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg,
                                (int) (myMidPoint.x - myEyesDistance),
                                (int) (myMidPoint.y - myEyesDistance),
                                viewWidth, viewHeight, matrix, true);
                    } else {
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                viewWidth, viewHeight, matrix, true);
                    }

                    BitmapDrawable bd = new BitmapDrawable(resizedBitmap);
                    imageCrop1.setBackgroundDrawable(bd);

                } catch (Exception e) {
                    System.out.println("Error1 : " + e.getMessage()
                            + e.toString());
                }
            }
        });

        imageCrop.post(new Runnable() {
            @Override
            public void run() {
                viewHeight = 250;
                viewWidth = 250;
            //     viewHeight = imageCrop.getMeasuredHeight();
            //    viewWidth = imageCrop.getMeasuredWidth();
                Log.i("imageCrop height", "run: ................................."+viewHeight);
                Log.i("imageCrop width", "run: ................................."+viewWidth);

                try {

                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);

                    //////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Log.i("filePath ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);
                    Log.i("cursor", "run: ................................."+cursor);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Log.i("imagePath", "run: ...................................."+imagePath);
                    cursor.close();
                    Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
                    Log.i("bitmapOrg", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    int targetWidth = bitmapOrg.getWidth();
                    int targetHeight = bitmapOrg.getHeight();
                    Log.i("bitmapOrg height", "run: ..............................."+targetHeight);
                    Log.i("bitmapOrg width", "run: ..............................."+targetWidth);

                    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                            targetHeight, Bitmap.Config.ARGB_8888);

                    RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

                    Canvas canvas = new Canvas(targetBitmap);
                    Path path = new Path();

                    path.addRect(rectf, Path.Direction.CW);
                    canvas.clipPath(path);

                    canvas.drawBitmap(bitmapOrg,
                            new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg
                                    .getHeight()), new Rect(0, 0, targetWidth,
                                    targetHeight), paint);

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);

                    BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                    bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    ///////////////////////////////////////////////////////////
                    // Let's read picked image path using content resolver
                    filePath = new String[]{MediaStore.Images.Media.DATA};
                    Log.i("filePath1 ", "run: .............................."+filePath);
                    cursor = getContentResolver().query(mImageCaptureUri, filePath,
                            null, null, null);

                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    // imagePath = mImageCaptureUri.getPath();
                    Log.i("imagePath1..", "run: ...................................."+imagePath);
                    cursor.close();
                    bitmapOrg = BitmapFactory.decodeFile(imagePath,bitmapFactoryOptions);
                    Log.i("bitmapOrg1 ..", "run: ..............................."+bitmapOrg);
                    //////////////////////////////////////////////////////////

                    myFace = new android.media.FaceDetector.Face[5];
                    myFaceDetect = new android.media.FaceDetector(targetWidth, targetHeight,
                            5);
                    int numberOfFaceDetected = myFaceDetect.findFaces(
                            bitmapOrg, myFace);
                    Bitmap resizedBitmap = null;
                    if (numberOfFaceDetected > 0) {
                        PointF myMidPoint = null;
                        android.media.FaceDetector.Face face = myFace[0];
                        myMidPoint = new PointF();
                        face.getMidPoint(myMidPoint);
                        myEyesDistance = face.eyesDistance() + 20;

                        if (myMidPoint.x + viewWidth > targetWidth) {
                            while (myMidPoint.x + viewWidth > targetWidth) {
                                myMidPoint.x--;
                            }
                        }
                        if (myMidPoint.y + viewHeight > targetHeight) {
                            while (myMidPoint.y + viewHeight > targetHeight) {
                                myMidPoint.y--;
                            }
                        }
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg,
                                (int) (myMidPoint.x - myEyesDistance),
                                (int) (myMidPoint.y - myEyesDistance),
                                viewWidth, viewHeight, matrix, true);
                    } else {
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                viewWidth, viewHeight, matrix, true);
                    }

                    BitmapDrawable bd = new BitmapDrawable(resizedBitmap);

                    //for getting circular image
                    imageCrop.setBackgroundDrawable(new BitmapDrawable(
                            getCroppedBitmap(bd.getBitmap())));

                    //for getting square image
                   *//* imageCrop.setBackgroundDrawable(bd);*//*

                } catch (Exception e) {
                    System.out.println("Error1 : " + e.getMessage()
                            + e.toString());
                }
            }
        });*/