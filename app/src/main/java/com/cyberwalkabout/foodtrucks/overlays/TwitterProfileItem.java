package com.cyberwalkabout.foodtrucks.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.uki.foodtrucks.R;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.uki.common.util.ConvertUtils;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class TwitterProfileItem extends OverlayItem {
    private TwitterStatus profile;
    private LayoutInflater li;
    private MapView map;
    private AQuery aq;

    TwitterProfileItem(TwitterStatus profile, Context ctx, MapView map) {
        super(ConvertUtils.toGeoPoint(profile.getLatitude(), profile.getLongitude()), profile.getFromUser(), profile.getFromUser());
        this.profile = profile;
        this.li = LayoutInflater.from(ctx);
        this.aq = new AQuery(ctx);
        this.map = map;
    }

    @Override
    public Drawable getMarker(final int stateBitset) {

        final LinearLayout baloon = (LinearLayout) li.inflate(R.layout.food_truck_map_item, null);
        aq = new AQuery(baloon);
        aq.id(R.id.profile_image).image(profile.getImageUrl(), true, true, 0, R.drawable.stub_image, new BitmapAjaxCallback() {

            @Override
            public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                iv.setImageBitmap(bm);
                Drawable marker = prepareMarkerBitmap(baloon);
                setState(marker, stateBitset);
                map.postInvalidate();
            }

        });

        Drawable marker = prepareMarkerBitmap(baloon);
        setState(marker, stateBitset);
        return marker;
    }

    public TwitterStatus getProfile() {
        return this.profile;
    }

    private Drawable prepareMarkerBitmap(LinearLayout baloon) {
        baloon.measure(0, 0);
        baloon.layout(0, 0, baloon.getMeasuredWidth(), baloon.getMeasuredHeight());
        baloon.setDrawingCacheEnabled(true);
        Bitmap bitmap = baloon.getDrawingCache();
        Drawable marker = new BitmapDrawable(bitmap);
        marker.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmap = null;
        return marker;
    }
}
