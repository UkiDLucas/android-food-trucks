package com.cyberwalkabout.foodtrucks.overlays;

import android.graphics.drawable.Drawable;

import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.uki.common.util.ConvertUtils;
import com.google.android.maps.OverlayItem;

public class TwitterMentionMapItem extends OverlayItem {
    private Drawable marker = null;
    private TwitterStatus status;

    TwitterMentionMapItem(TwitterStatus status, Drawable marker) {
        super(ConvertUtils.toGeoPoint(status.getLatitude(), status.getLongitude()), status.getFromUser(), status.getText());
        this.marker = marker;
        this.status = status;
    }

    @Override
    public Drawable getMarker(int stateBitset) {
        setState(marker, stateBitset);
        return marker;
    }

    public TwitterStatus getTwitterStatus() {
        return this.status;
    }


}
