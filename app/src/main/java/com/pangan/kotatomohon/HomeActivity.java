package com.pangan.kotatomohon;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {

    private String userRole;
    private Menu optionsMenu;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getUserRoleFromFirebase();

        TextView dateTextView = findViewById(R.id.dateTextView);
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        // Display the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String currentDate = dateFormat.format(new Date());
        String fullText = getString(R.string.tanggal_hari_ini, currentDate);
        dateTextView.setText(fullText);

        // Reference to the Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pangan");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayout.removeAllViews();

                TableRow headerRow = new TableRow(HomeActivity.this);

                TextView numberTitle = new TextView(HomeActivity.this);
                numberTitle.setText("No.");

                TextView komoditiTitle = new TextView(HomeActivity.this);
                komoditiTitle.setText("Komoditi");

                TextView hargaTitle = new TextView(HomeActivity.this);
                hargaTitle.setText("Harga");

                numberTitle.setTextColor(getResources().getColor(R.color.black));
                komoditiTitle.setTextColor(getResources().getColor(R.color.black));
                hargaTitle.setTextColor(getResources().getColor(R.color.black));

                Typeface typeface = Typeface.create("poppinssemibold", Typeface.BOLD);
                numberTitle.setTypeface(typeface);
                komoditiTitle.setTypeface(typeface);
                hargaTitle.setTypeface(typeface);

                numberTitle.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                numberTitle.setPadding(2, 8, 2, 8);
                numberTitle.setTextSize(12);
                numberTitle.setGravity(Gravity.CENTER);

                komoditiTitle.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                komoditiTitle.setPadding(2, 8, 2, 8);
                komoditiTitle.setTextSize(12);
                komoditiTitle.setGravity(Gravity.CENTER);

                hargaTitle.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                hargaTitle.setPadding(2, 8, 2, 8);
                hargaTitle.setTextSize(12);
                hargaTitle.setGravity(Gravity.CENTER);

                headerRow.addView(numberTitle);
                headerRow.addView(komoditiTitle);
                headerRow.addView(hargaTitle);

                tableLayout.addView(headerRow);
                int number = 1;

                Map<String, Pair<Double, Integer>> commodityMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String komoditi = snapshot.child("spinnerKomoditi").getValue(String.class);
                    String harga = snapshot.child("editTextHarga").getValue(String.class);
                    String dataTanggal = snapshot.child("tanggalData").getValue(String.class);

                    if (dataTanggal != null && dataTanggal.equals(currentDate)) {
                        // Calculate the average price for each commodity
                        if (commodityMap.containsKey(komoditi)) {
                            Pair<Double, Integer> pair = commodityMap.get(komoditi);
                            double total = pair.first + Double.parseDouble(harga);
                            int count = pair.second + 1;
                            commodityMap.put(komoditi, new Pair<>(total, count));
                        } else {
                            commodityMap.put(komoditi, new Pair<>(Double.parseDouble(harga), 1));
                        }
                        number++;
                    }
                }
                int nomor = 1;
                // Display the average price for each commodity
                for (Map.Entry<String, Pair<Double, Integer>> entry : commodityMap.entrySet()) {
                    String komoditi = entry.getKey();
                    Pair<Double, Integer> pair = entry.getValue();
                    double averagePrice = pair.first / pair.second;

                    TableRow row = new TableRow(HomeActivity.this);

                    TextView numberTextView = new TextView(HomeActivity.this);
                    numberTextView.setText(String.valueOf(nomor));

                    numberTextView.setTypeface(Typeface.create("poppinssemibold", Typeface.BOLD));
                    numberTextView.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.black));

                    numberTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                    numberTextView.setPadding(2, 8, 2, 8);
                    numberTextView.setTextSize(12);
                    numberTextView.setGravity(Gravity.CENTER);

                    TextView komoditiTextView = new TextView(HomeActivity.this);
                    komoditiTextView.setText(komoditi);

                    komoditiTextView.setTypeface(Typeface.create("poppinssemibold", Typeface.BOLD));
                    komoditiTextView.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.black));

                    komoditiTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                    komoditiTextView.setPadding(2, 8, 2, 8);
                    komoditiTextView.setTextSize(12);
                    komoditiTextView.setGravity(Gravity.CENTER);

                    TextView hargaTextView = new TextView(HomeActivity.this);
                    hargaTextView.setText(String.format(Locale.getDefault(), "%.2f", averagePrice));

                    hargaTextView.setTypeface(Typeface.create("poppinssemibold", Typeface.BOLD));
                    hargaTextView.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.black));

                    hargaTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f));
                    hargaTextView.setPadding(2, 8, 2, 8);
                    hargaTextView.setTextSize(12);
                    hargaTextView.setGravity(Gravity.CENTER);

                    row.addView(numberTextView);
                    row.addView(komoditiTextView);
                    row.addView(hargaTextView);

                    tableLayout.addView(row);
                    nomor++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
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
                    Intent entryIntent = new Intent(HomeActivity.this, EntryActivity.class);
                    startActivity(entryIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(HomeActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(HomeActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(HomeActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(HomeActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(HomeActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(HomeActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(HomeActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(HomeActivity.this, DetailActivity.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

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
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Close HomeActivity
            return true;
        }
        if (id == R.id.grafik) {
            // Redirect to login page when "Login" menu item is clicked
            Intent grafikIntent = new Intent(HomeActivity.this, GrafikActivity.class);
            startActivity(grafikIntent);
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
