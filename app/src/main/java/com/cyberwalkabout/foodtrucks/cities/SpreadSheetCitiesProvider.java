package com.cyberwalkabout.foodtrucks.cities;

import android.content.Context;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;
import com.uki.common.http.HttpClientConfig;
import com.uki.common.http.HttpUtils;
import com.cyberwalkabout.gss.AtomPublicSpreadsheetsClient;
import com.cyberwalkabout.gss.SpreadSheet;
import com.cyberwalkabout.gss.Worksheet;
import org.apache.http.client.HttpClient;

public class SpreadSheetCitiesProvider {

    private static final String TAG = SpreadSheetCitiesProvider.class.getSimpleName();

    private AtomPublicSpreadsheetsClient spreadsheetsClient;
    private String spreadsheetKey;
    private DatabaseHelper dbHelper;

    public SpreadSheetCitiesProvider(String spreadsheetKey, Context ctx) {
        this.spreadsheetsClient = new AtomPublicSpreadsheetsClient();
        this.spreadsheetKey = spreadsheetKey;
        this.dbHelper = DatabaseHelper.get(ctx);
    }

    public boolean load() {
        boolean successful = true;
        try {
            HttpClient httpClient = HttpUtils.createHttpClient(HttpClientConfig.createConfig(10000));
            SpreadSheet spreadSheet = spreadsheetsClient.getSpreadSheet(spreadsheetKey, httpClient);
            if (spreadSheet != null) {
                for (Worksheet entry : spreadSheet.getWorkSheets()) {
                    City city = new City();
                    city.setId(entry.getShortId());

                    String[] values = entry.getTitle().split("\\|");

                    if (values.length == 3) {
                        city.setName(values[0]);
                        city.setLatitude(Double.parseDouble(values[1]));
                        city.setLongitude(Double.parseDouble(values[2]));

                        dbHelper.updateCity(city);
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            successful = false;

            BugSenseHandler.sendException(e);
        }
        return successful;
    }

}
