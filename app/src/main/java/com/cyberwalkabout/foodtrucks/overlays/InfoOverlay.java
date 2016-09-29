package com.cyberwalkabout.foodtrucks.overlays;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.MotionEvent;

import com.uki.foodtrucks.R;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.uki.common.util.ConvertUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class InfoOverlay extends Overlay {

    private static Paint textPaint;

    static {
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.LTGRAY);
        textPaint.setStrokeWidth(0.5f);
    }

    private Drawable baloon;

    private int baloonWidth;
    private int baloonHeight;
    private int baloonXoffset;
    private int baloonYoffset;

    private int textXoffset;
    private int textYoffset;
    private int textLineYoffset;

    private TwitterStatus mInfo;

    private int maxLineWidth = 45;

    int balloonLeft;
    int balloonTop;
    int balloonRight;
    int balloonBottom;

    public InfoOverlay(Context ctx, TwitterStatus item) {

        this.mInfo = item;

        Resources r = ctx.getResources();

        baloonWidth = (int) ConvertUtils.dipToPixels(r, 90);
        baloonHeight = (int) ConvertUtils.dipToPixels(r, 30);

        baloonXoffset = (int) ConvertUtils.dipToPixels(r, 0);
        baloonYoffset = (int) ConvertUtils.dipToPixels(r, 0);

        textPaint.setTextSize(ConvertUtils.dipToPixels(r, 11));

        textXoffset = (int) ConvertUtils.dipToPixels(r, 8);
        textYoffset = (int) ConvertUtils.dipToPixels(r, 16);
        textLineYoffset = (int) ConvertUtils.dipToPixels(r, 14);

        this.baloon = r.getDrawable(R.drawable.baloon);
    }

    @Override
    public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
        super.draw(canvas, mv, shadow);
        drawBaloon(mv, canvas);
        return true;
    }

    private void drawBaloon(MapView mv, Canvas canvas) {

        Point point = new Point();
        GeoPoint geo = ConvertUtils.toGeoPoint(mInfo.getLatitude(), mInfo.getLongitude());

        mv.getProjection().toPixels(geo, point);

        String text = Html.fromHtml(mInfo.getFromUser() + ": " + mInfo.getText()).toString();
        List<String> lines = breakText(text);

        float longestLine = 0;
        for (String line : lines) {
            float temp = textPaint.measureText(line);
            if (temp > longestLine)
                longestLine = temp;
        }

        baloonWidth = (int) (longestLine + textXoffset * 2);
        int h = baloonHeight * (lines.size() / 2) + textYoffset * 2;
        balloonLeft = point.x - baloonXoffset;
        balloonTop = point.y - h - baloonYoffset;
        balloonRight = point.x + baloonWidth - baloonXoffset;
        balloonBottom = point.y - baloonYoffset;

        baloon.setBounds(balloonLeft, balloonTop, balloonRight, balloonBottom);
        baloon.draw(canvas);

        int dy = 0;
        for (String line : lines) {
            canvas.drawText(line, balloonLeft + textXoffset, balloonTop + textYoffset + dy, textPaint);
            dy += textLineYoffset;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
        if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
            if (checkHit(mapView, (int) motionEvent.getX(), (int) motionEvent.getY())) {
                mapView.getOverlays().remove(this);
            }
        }
        return super.onTouchEvent(motionEvent, mapView);
    }

    protected boolean checkHit(MapView mapView, int hitX, int hitY) {

        Point eventPos = new Point(hitX, hitY);
        Point point = new Point();
        GeoPoint geo = ConvertUtils.toGeoPoint(mInfo.getLatitude(), mInfo.getLongitude());

        mapView.getProjection().toPixels(geo, point);
        if ((eventPos.x <= balloonRight && eventPos.x >= balloonLeft) && (eventPos.y <= balloonBottom && eventPos.y >= balloonTop)) {
            return true;
        }
        return false;
    }

    private List<String> breakText(String text) {
        List<String> lines = new ArrayList<String>();
        if (text.length() < maxLineWidth) {
            lines.add(text);
            return lines;
        }

        while (text.length() > 0) {
            int nextBreak = text.indexOf(" ", maxLineWidth / 2);

            if (nextBreak < 1)
                nextBreak = text.length();

            lines.add(text.substring(0, nextBreak).trim());
            text = text.substring(nextBreak);

        }

        return lines;
    }
}
