package com.cyberwalkabout.foodtrucks.overlays;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;

import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TwitterProfilesOverlay extends ItemizedOverlay<TwitterProfileItem> {
    private static final long timestamp = 5 * 3600000;// hours

    private MapView map;
    private List<TwitterProfileItem> items;
    private Context ctx;

    public TwitterProfilesOverlay(MapView map, Context context, List<TwitterStatus> profiles) {
        super(null);
        this.ctx = context;
        this.items = new ArrayList<TwitterProfileItem>();
        this.map = map;

        for (TwitterStatus profile : profiles) {
            items.add(new TwitterProfileItem(profile, context, map));
        }
        populate();
    }

    @Override
    protected TwitterProfileItem createItem(int i) {
        return items.get(i);
    }


    @Override
    public int size() {
        return items == null ? 0 : items.size();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    public int getCount() {
        return items.size();
    }

    @Override
    public boolean onTap(int index) {
        InfoOverlay infoOverlay = new InfoOverlay(ctx, items.get(index).getProfile());
        List<Overlay> mapOverlays = map.getOverlays();
        mapOverlays.add(infoOverlay);
        map.invalidate();
        return true;
    }

}
