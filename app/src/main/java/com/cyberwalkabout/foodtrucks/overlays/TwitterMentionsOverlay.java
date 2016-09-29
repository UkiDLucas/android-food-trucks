package com.cyberwalkabout.foodtrucks.overlays;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TwitterMentionsOverlay extends ItemizedOverlay<TwitterMentionMapItem> {

    private MapView map;
    private List<TwitterMentionMapItem> items;
    private Drawable marker;
    private Context ctx;

    public TwitterMentionsOverlay(MapView map, Context context, List<TwitterStatus> mentions) {
        super(null);
        this.ctx = context;
        this.items = new ArrayList<TwitterMentionMapItem>();
        this.map = map;
        this.marker = context.getResources().getDrawable(com.uki.foodtrucks.R.drawable.crowd_bullet);

        for (TwitterStatus status : mentions) {
            items.add(new TwitterMentionMapItem(status, marker));
        }
        populate();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    protected TwitterMentionMapItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items == null ? 0 : items.size();
    }

    public int getCount() {
        return items.size();
    }

    @Override
    public boolean onTap(int index) {
        InfoOverlay infoOverlay = new InfoOverlay(ctx, items.get(index).getTwitterStatus());
        List<Overlay> mapOverlays = map.getOverlays();
        mapOverlays.add(infoOverlay);
        map.invalidate();
        return true;
    }

}
