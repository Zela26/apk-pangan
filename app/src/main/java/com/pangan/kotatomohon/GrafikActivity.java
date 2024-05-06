package com.pangan.kotatomohon;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafikActivity extends AppCompatActivity {

    private WebView webView;
    private Spinner spinnerKomoditi, spinnerBulan, spinnerTahun;
    private String userRole;
    private Menu optionsMenu;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private static class PriceData {
        private int total;
        private int count;

        public PriceData(int total, int count) {
            this.total = total;
            this.count = count;
        }

        public void addPrice(int price) {
            total += price;
            count++;
        }

        public int getAverage() {
            if (count == 0) {
                return 0;
            }
            return total / count;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        getUserRoleFromFirebase();
        webView = findViewById(R.id.graph);
        spinnerKomoditi = findViewById(R.id.spinnerKomoditi);
        spinnerBulan = findViewById(R.id.spinnerBulan);
        spinnerTahun = findViewById(R.id.spinnerTahun);

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the HTML file from assets folder
        webView.loadUrl("file:///android_asset/chart.html");

        // Ensure WebView can execute JavaScript function
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebView", "Page finished loading: " + url);
                if (url.equals("file:///android_main/assets/chart.html")) {
                    // WebView has finished loading the chart.html file
                    // You can perform additional actions here if needed
                }
            }
        });


        spinnerKomoditi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle spinner selection
                loadChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        spinnerBulan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle spinner selection
                loadChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerTahun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle spinner selection
                loadChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_entry && hasPermission("Enumerator")) {
                    Intent entryIntent = new Intent(GrafikActivity.this, EntryActivity.class);
                    startActivity(entryIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(GrafikActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(GrafikActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(GrafikActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_home) {
                    Intent homeIntent = new Intent(GrafikActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(GrafikActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(GrafikActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(GrafikActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(GrafikActivity.this, DetailActivity.class);
                    startActivity(detailIntent);
                    return true;
                } else if (id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    checkLoginStatus();
                    return true;
                }

                // If the item ID is not recognized or the user does not have permission, show an error message
                showErrorDialog("Akses tidak diizinkan");

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void loadChartData() {
        String komoditi = spinnerKomoditi.getSelectedItem().toString();
        String bulan = spinnerBulan.getSelectedItem().toString();
        String tahun = spinnerTahun.getSelectedItem().toString();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Pangan");
        Query query = databaseReference.orderByChild("spinnerKomoditi").equalTo(komoditi);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> labels = new ArrayList<>();
                List<Integer> data = new ArrayList<>();
                HashMap<String, PriceData> priceDataMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String tanggal = snapshot.child("tanggalData").getValue(String.class);
                    String[] parts = tanggal.split(" ");
                    if (parts.length >= 3 && parts[1].equals(bulan) && parts[2].equals(tahun)) {
                        String harga = snapshot.child("editTextHarga").getValue(String.class);
                        if (!TextUtils.isEmpty(harga) && TextUtils.isDigitsOnly(harga)) {
                            if (!priceDataMap.containsKey(tanggal)) {
                                priceDataMap.put(tanggal, new PriceData(Integer.parseInt(harga), 1));
                            } else {
                                PriceData existingData = priceDataMap.get(tanggal);
                                existingData.addPrice(Integer.parseInt(harga));
                                priceDataMap.put(tanggal, existingData);
                            }
                        }
                    }
                }

                for (Map.Entry<String, PriceData> entry : priceDataMap.entrySet()) {
                    labels.add(entry.getKey());
                    data.add(entry.getValue().getAverage());
                }

                Log.d("FirebaseData", "Labels: " + labels.toString());
                Log.d("FirebaseData", "Data: " + data.toString());

                if (labels.isEmpty() || data.isEmpty()) {
                    Log.d("FirebaseData", "No data found for the selected criteria.");
                }

                String jsonData = "{ \"labels\": " + new Gson().toJson(labels) + ", \"data\": " + new Gson().toJson(data) + " }";
                String javascript = "javascript:updateChart('" + jsonData + "')";
                webView.evaluateJavascript(javascript, null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Set visibility of menu items based on login status
        setMenuVisibility(FirebaseAuth.getInstance().getCurrentUser() != null);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle other menu item clicks here if needed
        if (id == R.id.action_settings) {
            // Redirect to login page when "Login" menu item is clicked
            Intent loginIntent = new Intent(GrafikActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Close HomeActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void checkLoginStatus() {
        // Check login status
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // If the user is not logged in, decide what action to take
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Finish the HomeActivity or not, depending on your requirements
        } else {
            // If the user is logged in, do whatever needs to be done upon login
        }
    }





    @Override
    protected void onResume() {
        super.onResume();
            loadChartData();
    }

    private boolean hasPermission(String... roles) {
        for (String role : roles) {
            if (userRole != null && userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog when the "OK" button is clicked
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getUserRoleFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User")
                    .child(currentUser.getUid())
                    .child("role");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userRole = dataSnapshot.getValue(String.class);
                        Log.d("UserRole", "User role from Firebase: " + userRole);
                        setMenuVisibility(true); // Pengguna sudah login
                    } else {
                        setMenuVisibility(false); // Pengguna belum login
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("UserRole", "Failed to retrieve user role from Firebase: " + databaseError.getMessage());
                    // Handle errors if needed
                }
            });
        } else {
            // Handle the case when the user is not authenticated
            // You may want to redirect the user to the login page or take appropriate action
            Log.e("UserRole", "User is not authenticated");
        }
    }
    private boolean isLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null;
    }

    @Override
    public void onBackPressed() {
        if (!isLoggedIn()) {
            // Pengguna telah logout, arahkan ke tampilan menu handphone
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            super.onBackPressed(); // Jalankan fungsi kembali standar
        }
    }


    private void setMenuVisibility(boolean isLoggedIn) {
        if (optionsMenu != null) {
            for (int i = 0; i < optionsMenu.size(); i++) {
                MenuItem item = optionsMenu.getItem(i);
                // Semua item menu akan ditampilkan jika pengguna sudah login
                item.setVisible(isLoggedIn);
            }
        }

        // Update ActionBarDrawerToggle based on login status
        boolean isUserLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        actionBarDrawerToggle.setDrawerIndicatorEnabled(isUserLoggedIn);
        actionBarDrawerToggle.syncState();
    }

}

