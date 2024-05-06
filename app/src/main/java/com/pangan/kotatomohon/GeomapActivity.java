package com.pangan.kotatomohon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeomapActivity extends AppCompatActivity {
    private MapView mapView;
    private DatabaseReference lokasiPanganRef;
    private DatabaseReference stokPanganRef;
    private Menu optionsMenu;
    private String userRole;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geomap);

        // Firebase database references
        lokasiPanganRef = FirebaseDatabase.getInstance().getReference("LokasiPangan");
        stokPanganRef = FirebaseDatabase.getInstance().getReference("StokPangan");

        // Configure osmdroid
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);

        IMapController mapController = mapView.getController();
        mapController.setZoom(12.0);
        mapController.setCenter(new GeoPoint(1.3144, 124.8327)); // Tomohon, Sulawesi Utara

        mapView.setMultiTouchControls(true);

        // Fetch data from Firebase and display markers
        fetchAndDisplayMarkers();
    }

    private void fetchAndDisplayMarkers() {
        lokasiPanganRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<GeoPoint> geoPoints = new ArrayList<>();

                // Clear existing overlays
                mapView.getOverlayManager().clear();

                for (DataSnapshot lokasiSnapshot : dataSnapshot.getChildren()) {
                    double latitude = lokasiSnapshot.child("latitude").getValue(Double.class);
                    double longitude = lokasiSnapshot.child("longitude").getValue(Double.class);

                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                    geoPoints.add(geoPoint);

                    // Fetch and display info window for each location
                    String namaLokasi = lokasiSnapshot.child("namaLokasi").getValue(String.class);
                    fetchAndDisplayInfoWindow(geoPoint, namaLokasi);
                }

                // Display markers after fetching info windows
                displayMarkers(geoPoints);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data from LokasiPangan: " + databaseError.getMessage());
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

                if (id == R.id.nav_home) {
                    Intent homeIntent = new Intent(GeomapActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_entry && hasPermission("Enumerator")) {
                    Intent geomapIntent = new Intent(GeomapActivity.this, EntryActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(GeomapActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(GeomapActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(GeomapActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(GeomapActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(GeomapActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(GeomapActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(GeomapActivity.this, DetailActivity.class);
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

    private void fetchAndDisplayInfoWindow(final GeoPoint geoPoint, final String namaLokasi) {
        stokPanganRef.child(namaLokasi).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch and display StokPangan data for the location
                    fetchStokPanganDataAndDisplayInfoWindow(geoPoint, namaLokasi);
                } else {
                    Log.e("FirebaseData", "Tidak ada data StokPangan ditemukan untuk namaLokasi: " + namaLokasi);
                    // Tangani kasus di mana tidak ada data StokPangan ditemukan untuk namaLokasi tertentu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error mengambil data dari StokPangan: " + databaseError.getMessage());
            }
        });
    }


    private void fetchStokPanganDataAndDisplayInfoWindow(final GeoPoint geoPoint, final String namaLokasi) {
        stokPanganRef.child(namaLokasi).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, StokInfo> latestStokInfoMap = new HashMap<>();

                    for (DataSnapshot stokSnapshot : dataSnapshot.getChildren()) {
                        String komoditi = stokSnapshot.child("komoditi").getValue(String.class);
                        int stok = stokSnapshot.child("stok").getValue(Integer.class);
                        String tanggal = stokSnapshot.child("tanggal").getValue(String.class);

                        // Jika komoditi sudah ada dalam map dan tanggalnya lebih baru, update stokInfo
                        if (latestStokInfoMap.containsKey(komoditi)) {
                            StokInfo stokInfo = latestStokInfoMap.get(komoditi);
                            if (tanggal.compareTo(stokInfo.getTanggal()) > 0) {
                                stokInfo.setStok(stok);
                                stokInfo.setTanggal(tanggal);
                            }
                        } else {
                            // Jika komoditi belum ada dalam map, tambahkan ke map
                            latestStokInfoMap.put(komoditi, new StokInfo(komoditi, stok, tanggal));
                        }
                    }

                    StringBuilder infoTextBuilder = new StringBuilder();
                    for (StokInfo stokInfo : latestStokInfoMap.values()) {
                        String stokInfoText = getMarkerInfoText(stokInfo.getKomoditi(), stokInfo.getStok(), stokInfo.getTanggal());
                        infoTextBuilder.append(stokInfoText).append("\n");
                    }

                    // Tampilkan marker dengan info window kustom
                    displayMarkerWithInfoWindow(geoPoint, infoTextBuilder.toString());
                } else {
                    Log.e("FirebaseData", "Tidak ada data StokPangan ditemukan untuk lokasi: " + namaLokasi);
                    // Tangani kasus di mana tidak ada data StokPangan ditemukan untuk lokasi tertentu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error mengambil data dari StokPangan: " + databaseError.getMessage());
            }
        });
    }


    private void displayMarkerWithInfoWindow(GeoPoint geoPoint, String infoText) {
        // Create marker
        Marker marker = new Marker(mapView);
        marker.setPosition(geoPoint);
        mapView.getOverlayManager().add(marker);

        Log.d("FirebaseData", "Info Text: " + infoText);

        // Create custom info window
        CustomInfoWindow customInfoWindow = new CustomInfoWindow(R.layout.custom_info_window, mapView, GeomapActivity.this, infoText);

        // Connect custom info window with the marker
        marker.setInfoWindow(customInfoWindow);

        // Set marker as the object related to the info window
        customInfoWindow.setRelatedObject(marker);
    }

    private String getMarkerInfoText(String komoditi, int stok, String tanggal) {
        // Customize this method to format the information as per your requirement
        return "Komoditi: " + komoditi + "\nStok: " + stok + "\nTanggal: " + tanggal;
    }

    private void displayMarkers(List<GeoPoint> geoPoints) {
        mapView.getOverlayManager().clear(); // Bersihkan overlay yang sudah ada

        for (GeoPoint geoPoint : geoPoints) {
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            mapView.getOverlayManager().add(marker);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private boolean hasPermission(String... roles) {
        for (String role : roles) {
            if (userRole != null && userRole.equals(role)) {
                return true;
            }
        }
        return false;
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

    @Override
    protected void onResume() {
        super.onResume();
        getUserRoleFromFirebase(); // Get the user role when the activity resumes
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