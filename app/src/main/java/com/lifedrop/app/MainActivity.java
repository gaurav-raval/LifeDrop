package com.lifedrop.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 101;

    private LinearLayout nearbyContent, requestsContent, centersContainer, requestsContainer, bloodTypeContainer;
    private TextView tabNearby, tabRequests, navExploreIcon, navRequestsIcon, notice, clearSearch;
    private EditText searchInput;

    private final List<Center> centers = Arrays.asList(
            new Center("City Care Blood Center", "Blood bank", "0.8 mi", "24 Oak Street", "Open now", "Closes at 7:00 PM", true),
            new Center("St. Mary's Hospital", "Hospital donor unit", "1.4 mi", "188 Waverly Ave", "Open now", "Closes at 6:00 PM", false),
            new Center("Red Cross Donation Point", "Donation center", "2.1 mi", "5 Market Square", "Opens 9:00 AM", "Tomorrow", false)
    );

    private String selectedType = "All";
    private boolean requestsMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupBloodTypes();
        renderCenters();
        renderRequests();
        setupListeners();
    }

    private void bindViews() {
        nearbyContent = findViewById(R.id.nearbyContent);
        requestsContent = findViewById(R.id.requestsContent);
        centersContainer = findViewById(R.id.centersContainer);
        requestsContainer = findViewById(R.id.requestsContainer);
        bloodTypeContainer = findViewById(R.id.bloodTypeContainer);
        tabNearby = findViewById(R.id.tabNearby);
        tabRequests = findViewById(R.id.tabRequests);
        navExploreIcon = findViewById(R.id.navExploreIcon);
        navRequestsIcon = findViewById(R.id.navRequestsIcon);
        notice = findViewById(R.id.notice);
        searchInput = findViewById(R.id.searchInput);
        clearSearch = findViewById(R.id.clearSearch);
    }

    private void setupListeners() {
        findViewById(R.id.tabNearby).setOnClickListener(v -> showNearby());
        findViewById(R.id.tabRequests).setOnClickListener(v -> showRequests());
        findViewById(R.id.navExplore).setOnClickListener(v -> showNearby());
        findViewById(R.id.navRequests).setOnClickListener(v -> showRequests());

        findViewById(R.id.notificationButton).setOnClickListener(v ->
                showNotice("You're all caught up."));

        findViewById(R.id.locationButton).setOnClickListener(v -> requestLocation());

        findViewById(R.id.mapViewButton).setOnClickListener(v ->
                showNotice("Map view is centered on your area."));

        clearSearch.setOnClickListener(v -> searchInput.setText(""));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                renderCenters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBloodTypes() {
        String[] types = {"All", "A+", "A−", "B+", "O+", "O−"};
        for (String type : types) {
            TextView pill = new TextView(this);
            pill.setText(type);
            pill.setTextSize(12);
            pill.setGravity(Gravity.CENTER);
            pill.setTypeface(null, android.graphics.Typeface.BOLD);
            pill.setPadding(dp(14), 0, dp(14), 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(type.equals("All") ? 48 : 52), dp(36));
            lp.setMargins(0, 0, dp(8), 0);
            pill.setLayoutParams(lp);
            pill.setOnClickListener(v -> {
                selectedType = ((TextView) v).getText().toString();
                updateBloodTypeStyles();
            });
            bloodTypeContainer.addView(pill);
        }
        updateBloodTypeStyles();
    }

    private void updateBloodTypeStyles() {
        for (int i = 0; i < bloodTypeContainer.getChildCount(); i++) {
            TextView pill = (TextView) bloodTypeContainer.getChildAt(i);
            boolean selected = pill.getText().toString().equals(selectedType);
            pill.setBackgroundResource(selected ? R.drawable.bg_dark_pill : R.drawable.bg_outline_button);
            pill.setTextColor(selected ? Color.WHITE : Color.rgb(115, 113, 107));
        }
    }

    private void renderCenters() {
        centersContainer.removeAllViews();
        String q = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.US);

        int count = 0;
        for (Center center : centers) {
            if (!q.isEmpty() &&
                    !center.name.toLowerCase(Locale.US).contains(q) &&
                    !center.address.toLowerCase(Locale.US).contains(q)) {
                continue;
            }
            count++;
            centersContainer.addView(createCenterCard(center));
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No centers found.");
            empty.setTextColor(Color.rgb(130, 128, 122));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(28), 0, dp(28));
            centersContainer.addView(empty);
        }

        centersContainer.setPadding(0, dp(2), 0, dp(16));
    }

    private View createCenterCard(Center center) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(15), dp(15), dp(13));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, dp(10), 0, 0);
        card.setLayoutParams(cardLp);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = makeText("✓", 17, Color.rgb(201, 55, 55));
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_icon_soft);
        top.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -2, 1);
        titleLp.setMargins(dp(10), 0, dp(8), 0);
        top.addView(titleBox, titleLp);

        TextView name = makeText(center.name, 13, Color.rgb(28,29,27));
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        titleBox.addView(name);

        TextView type = makeText(center.type + " · " + center.distance, 11, Color.rgb(146,144,138));
        LinearLayout.LayoutParams typeLp = new LinearLayout.LayoutParams(-2, -2);
        typeLp.topMargin = dp(2);
        titleBox.addView(type, typeLp);

        TextView status = makeText(center.status, 10, center.status.equals("Open now") ? Color.rgb(63,139,90) : Color.rgb(138,135,127));
        status.setGravity(Gravity.CENTER);
        status.setTypeface(null, android.graphics.Typeface.BOLD);
        status.setPadding(dp(9), dp(5), dp(9), dp(5));
        status.setBackgroundResource(center.status.equals("Open now") ? R.drawable.bg_green : R.drawable.bg_gray_pill);
        top.addView(status, new LinearLayout.LayoutParams(-2, dp(27)));

        card.addView(top);

        TextView info = makeText("⌖  " + center.address + "   ·   ◷  " + center.hours, 11, Color.rgb(119,116,110));
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(-1, -2);
        infoLp.topMargin = dp(12);
        info.setPadding(0, dp(11), 0, 0);
        card.addView(info, infoLp);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(10), 0, 0);

        Button directions = makeButton("➤  Directions", false);
        Button call = makeButton("☎  Call center", true);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, dp(38), 1);
        blp.setMargins(0, 0, dp(5), 0);
        buttons.addView(directions, blp);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(38), 1);
        clp.setMargins(dp(5), 0, 0, 0);
        buttons.addView(call, clp);

        directions.setOnClickListener(v -> openDirections(center.address));
        call.setOnClickListener(v -> showNotice("Calling " + center.name));

        card.addView(buttons);
        return card;
    }

    private void renderRequests() {
        requestsContainer.removeAllViews();
        addRequest("O−", "St. Mary's Hospital", "1.4 mi away", "Posted 18 min ago");
        addRequest("A+", "City Care Blood Center", "0.8 mi away", "Posted 42 min ago");
    }

    private void addRequest(String type, String place, String distance, String time) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(15), dp(13), dp(12), dp(13));
        card.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(78));
        lp.setMargins(0, dp(10), 0, 0);
        card.setLayoutParams(lp);

        TextView blood = makeText(type, 14, Color.WHITE);
        blood.setGravity(Gravity.CENTER);
        blood.setTypeface(null, android.graphics.Typeface.BOLD);
        blood.setBackgroundResource(R.drawable.bg_request_type);
        card.addView(blood, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, -2, 1);
        infoLp.setMargins(dp(10), 0, dp(8), 0);
        card.addView(info, infoLp);

        TextView p = makeText(place, 13, Color.rgb(28,29,27));
        p.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(p);
        TextView d = makeText(distance + " · " + time, 11, Color.rgb(146,144,138));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-2, -2);
        dlp.topMargin = dp(3);
        info.addView(d, dlp);

        Button help = makeButton("Help", false);
        help.setBackgroundResource(R.drawable.bg_dark_pill);
        help.setTextColor(Color.WHITE);
        help.setMinWidth(0);
        help.setOnClickListener(v -> showNotice("Thank you for offering to help."));
        card.addView(help, new LinearLayout.LayoutParams(dp(58), dp(36)));

        requestsContainer.addView(card);
    }

    private Button makeButton(String text, boolean red) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(null, android.graphics.Typeface.BOLD);
        b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setBackgroundResource(red ? R.drawable.bg_red_button : R.drawable.bg_outline_button);
        b.setTextColor(red ? Color.WHITE : Color.rgb(75,73,68));
        return b;
    }

    private TextView makeText(String text, float size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private void showNearby() {
        requestsMode = false;
        nearbyContent.setVisibility(View.VISIBLE);
        requestsContent.setVisibility(View.GONE);
        tabNearby.setBackgroundResource(R.drawable.bg_tab_selected);
        tabRequests.setBackgroundResource(android.R.color.transparent);
        tabNearby.setTextColor(Color.rgb(41,42,39));
        tabRequests.setTextColor(Color.rgb(150,148,141));
        navExploreIcon.setTextColor(Color.rgb(201,55,55));
        navRequestsIcon.setTextColor(Color.rgb(160,157,150));
    }

    private void showRequests() {
        requestsMode = true;
        nearbyContent.setVisibility(View.GONE);
        requestsContent.setVisibility(View.VISIBLE);
        tabRequests.setBackgroundResource(R.drawable.bg_tab_selected);
        tabNearby.setBackgroundResource(android.R.color.transparent);
        tabRequests.setTextColor(Color.rgb(41,42,39));
        tabNearby.setTextColor(Color.rgb(150,148,141));
        navRequestsIcon.setTextColor(Color.rgb(201,55,55));
        navExploreIcon.setTextColor(Color.rgb(160,157,150));
    }

    private void showNotice(String message) {
        notice.setText(message);
        notice.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> notice.setVisibility(View.GONE), 2800);
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            showNotice("Location access is ready.");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);
        }
    }

    private void openDirections(String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "geo:0,0?q=" + Uri.encode(address)));
            startActivity(intent);
        } catch (Exception e) {
            showNotice("Maps app is not available.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotice("Location permission granted.");
            } else {
                showNotice("Location permission was not granted.");
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    static class Center {
        String name, type, distance, address, status, hours;
        boolean urgent;
        Center(String name, String type, String distance, String address,
               String status, String hours, boolean urgent) {
            this.name = name; this.type = type; this.distance = distance;
            this.address = address; this.status = status; this.hours = hours;
            this.urgent = urgent;
        }
    }
}
