package com.bridgelabz.effectsdemo;

/**
 * Created by bridgeit on 17/10/16.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    Paint landmarksPaint;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;


    private Bitmap eyePatchBitmap;


    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        landmarksPaint = new Paint();
        landmarksPaint.setStrokeWidth(10);
        landmarksPaint.setColor(selectedColor);
        landmarksPaint.setStyle(Paint.Style.STROKE);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
       // canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        RectF rectf = new RectF(left, top, right, bottom);
        Path path = new Path();
        path.addRect(rectf, Path.Direction.CW);
        canvas.clipPath(path);


     /*   RectF rectF = new RectF(left, top, right, bottom);
        float cornerRadius = 2.0f;
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mBoxPaint);

        for (Landmark landmark : face.getLandmarks()) {
            int x1 = (int) (landmark.getPosition().x);
            int y1 = (int) (landmark.getPosition().y);
          //  canvas.drawCircle(x1, y1, FACE_POSITION_RADIUS, mFacePositionPaint);
            String type = String.valueOf(landmark.getType());
            mFacePositionPaint.setTextSize(50);
            canvas.drawText(type, x1, y1, mFacePositionPaint);
        }*/




      /*  RectF rectF = new RectF(left, top, right, bottom);
        float cornerRadius = 2.0f;
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mBoxPaint);
*/

           /* float x1 = (float) (landmark.getPosition().x);
            float y1 = (float) (landmark.getPosition().y);
*/

           /* float x2 = x1 + face.getWidth();
            float y2 = y1 + face.getHeight();
            */
           /* float x2 = x1 + canvas.getDensity();
            float y2 = y1 + canvas.getDensity();*/

           /* int x1 = (int) (landmark.getPosition().x);
            int y1 = (int) (landmark.getPosition().y);*/
        //  canvas.drawCircle(x1, y1, FACE_POSITION_RADIUS, mFacePositionPaint);

        // canvas.drawPoint(pos.x, pos.y, landmarksPaint);

          /* for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);*/


        for (Landmark landmark : mFace.getLandmarks()) {

            PointF pos = landmark.getPosition();
            String type = String.valueOf(landmark.getType());
            mFacePositionPaint.setTextSize(20);
       //     canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);

                for (Landmark landmark1 : face.getLandmarks()) {
                    switch (landmark1.getType()) {
                        case Landmark.BOTTOM_MOUTH:
                          /*  type = String.valueOf(Landmark.BOTTOM_MOUTH);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.LEFT_CHEEK:
                           /* type = String.valueOf(Landmark.LEFT_CHEEK);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.LEFT_EAR:
                           /* type = String.valueOf(Landmark.LEFT_EAR);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.LEFT_EAR_TIP:
                           /* type = String.valueOf(Landmark.LEFT_EAR_TIP);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.LEFT_EYE:
                         /*   landmarksPaint.setTextSize(30);
                            canvas.drawText( "1", pos.x, pos.y, landmarksPaint);*/
                         /*   type = String.valueOf(Landmark.LEFT_EYE);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.LEFT_MOUTH:
                         /*   type = String.valueOf(Landmark.LEFT_MOUTH);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.NOSE_BASE:
                            /*type = String.valueOf(Landmark.NOSE_BASE);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.RIGHT_CHEEK:
                          /*  type = String.valueOf(Landmark.RIGHT_CHEEK);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.RIGHT_EAR:
                           /* type = String.valueOf(Landmark.RIGHT_EAR);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.RIGHT_EAR_TIP:
                      /*      type = String.valueOf(Landmark.RIGHT_EAR_TIP);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.RIGHT_EYE:
                          /*  float prox=right-(FACE_POSITION_RADIUS/2);
                            float proy=right-(FACE_POSITION_RADIUS/2);
                            landmarksPaint.setTextSize(30);
                            canvas.drawText( "2", prox-11.0f, proy-11.0f, landmarksPaint);*/
                      /*      type = String.valueOf(Landmark.RIGHT_EYE);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        case Landmark.RIGHT_MOUTH:
                          /*  type = String.valueOf(Landmark.RIGHT_MOUTH);
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                        default:
                       /*     type = String.valueOf(landmark.getType());
                            mFacePositionPaint.setTextSize(20);
                            canvas.drawText(type, pos.x, pos.y, mFacePositionPaint);*/
                            break;

                    }
                }
        }
    }
}

