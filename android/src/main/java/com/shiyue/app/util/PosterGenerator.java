package com.shiyue.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.util.Base64;
import com.shiyue.app.model.EventItem;

import java.io.ByteArrayOutputStream;

public class PosterGenerator {

    public static Bitmap generatePoster(Context context, EventItem event, boolean isM3Style, String palette) {
        int width = 1080;
        int height = 1440;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        EventItem.CalculationResult calc = event.calculate();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (isM3Style) {
            // Material You Dynamic Gradient
            int startColor = 0xFF1E1235;
            int endColor = 0xFF3B2363;
            if ("blue".equals(palette)) {
                startColor = 0xFF001D32;
                endColor = 0xFF004A75;
            } else if ("green".equals(palette)) {
                startColor = 0xFF002111;
                endColor = 0xFF005232;
            } else if ("amber".equals(palette)) {
                startColor = 0xFF2E1500;
                endColor = 0xFF6A3A00;
            }

            LinearGradient grad = new LinearGradient(0, 0, width, height, startColor, endColor, Shader.TileMode.CLAMP);
            paint.setShader(grad);
            canvas.drawRect(0, 0, width, height, paint);
            paint.setShader(null);

            // Inner Card (Round 48px)
            paint.setColor(0x1AFFFFFF);
            RectF cardRect = new RectF(60, 60, width - 60, height - 60);
            canvas.drawRoundRect(cardRect, 48, 48, paint);

            // Category & Badge
            paint.setColor(0xFFD0BCFF);
            paint.setTextSize(32);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("⏳ 时约 · " + event.category, 110, 150, paint);

            paint.setColor(0x99FFFFFF);
            paint.setTextSize(26);
            canvas.drawText(calc.badgeText, 760, 150, paint);

            // Divider
            paint.setColor(0x26FFFFFF);
            paint.setStrokeWidth(2);
            canvas.drawLine(110, 180, width - 110, 180, paint);

            // Title
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(64);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(event.title, 110, 270, paint);

            // Giant Number
            paint.setColor(0xFFEADDFF);
            paint.setTextSize(210);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(String.valueOf(calc.absDays), 110, 490, paint);

            // Meta
            paint.setColor(0xB3FFFFFF);
            paint.setTextSize(34);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(calc.label + " · " + calc.subLabel, 110, 560, paint);

            paint.setTextSize(26);
            canvas.drawText("目标日期: " + calc.targetDateStr + " (" + calc.displayLunarStr + ")", 110, 610, paint);

            // Photo or Notes
            if (event.photos != null && !event.photos.isEmpty() && event.photos.get(0).dataUrl != null) {
                try {
                    String dataUrl = event.photos.get(0).dataUrl;
                    if (dataUrl.contains(",")) dataUrl = dataUrl.split(",")[1];
                    byte[] bytes = Base64.decode(dataUrl, Base64.DEFAULT);
                    Bitmap photoBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (photoBmp != null) {
                        RectF photoRect = new RectF(110, 660, width - 110, 1180);
                        canvas.save();
                        canvas.clipRect(photoRect);
                        canvas.drawBitmap(photoBmp, null, photoRect, paint);
                        canvas.restore();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.notes != null && !event.notes.isEmpty()) {
                paint.setColor(0xD9FFFFFF);
                paint.setTextSize(34);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                canvas.drawText("“ " + event.notes + " ”", 110, 780, paint);
            }

            // Footer
            paint.setColor(0x26FFFFFF);
            paint.setStrokeWidth(2);
            canvas.drawLine(110, 1260, width - 110, 1260, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(32);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("时约 · CHRONO", 110, 1320, paint);

            paint.setColor(0x99FFFFFF);
            paint.setTextSize(22);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText("守候每一个值得铭记的时刻", 640, 1320, paint);

        } else {
            // Swiss Minimalist Black & White
            paint.setColor(0xFF121212);
            canvas.drawRect(0, 0, width, height, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawRect(50, 50, width - 50, height - 50, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(0xFF888888);
            paint.setTextSize(24);
            canvas.drawText("SHIYUE // TIMELINE ARCHIVE", 90, 120, paint);
            canvas.drawText("INDEX // " + event.category, 750, 120, paint);

            paint.setColor(0xFF333333);
            paint.setStrokeWidth(2);
            canvas.drawLine(90, 150, width - 90, 150, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(64);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(event.title, 90, 240, paint);

            paint.setTextSize(230);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(String.valueOf(calc.absDays), 90, 480, paint);

            paint.setColor(0xFFAAAAAA);
            paint.setTextSize(36);
            canvas.drawText(calc.label, 90, 545, paint);
            paint.setTextSize(26);
            canvas.drawText(calc.subLabel + " · " + calc.displayLunarStr, 90, 595, paint);

            // Footer
            paint.setColor(0xFF333333);
            canvas.drawLine(90, 1260, width - 90, 1260, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(32);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("时约 // SWISS STYLE", 90, 1320, paint);

            paint.setColor(0xFF888888);
            paint.setTextSize(22);
            canvas.drawText("守候每一个值得铭记的时刻", 640, 1320, paint);
        }

        return bitmap;
    }

    public static String saveToGallery(Context context, Bitmap bmp, String title) {
        try {
            return MediaStore.Images.Media.insertImage(
                context.getContentResolver(), bmp, "时约海报-" + title, "由时约 App 纯原生生成"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
