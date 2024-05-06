package com.pangan.kotatomohon;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.pangan.kotatomohon.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private String userRole; // Peran pengguna

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Mengambil peran pengguna dari Firebase Realtime Database
        getCurrentUserRole();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Menentukan visibilitas item menu berdasarkan peran pengguna
        if (userRole.equals("Admin")) {
            menu.findItem(R.id.menu_tambah_akun).setVisible(true);
            menu.findItem(R.id.menu_detail_akun).setVisible(true);
        } else if (userRole.equals("Enumerator")) {
            menu.findItem(R.id.nav_entry).setVisible(true);
            menu.findItem(R.id.nav_geomap).setVisible(true);
            menu.findItem(R.id.nav_mingguan).setVisible(true);
            menu.findItem(R.id.nav_bulanan).setVisible(true);
            menu.findItem(R.id.nav_entryform).setVisible(true);
        } else if (userRole.equals("Pimpinan")) {
            menu.findItem(R.id.nav_geomap).setVisible(true);
            menu.findItem(R.id.nav_mingguan).setVisible(true);
            menu.findItem(R.id.nav_bulanan).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Tidak perlu pemeriksaan peran di sini, akses ke "Home" dibolehkan
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_entry) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Enumerator")) {
                Intent entryIntent = new Intent(this, EntryActivity.class);
                startActivity(entryIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_geomap) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Enumerator") || userRole.equals("Pimpinan")) {
                Intent geomapIntent = new Intent(this, GeomapActivity.class);
                startActivity(geomapIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_entryform) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Enumerator")) {
                Intent entryIntent = new Intent(this, EntryFormActivity.class);
                startActivity(entryIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_tampil) {
            Intent tampilIntent = new Intent(this, TampilEntryActivity.class);
            startActivity(tampilIntent);
            return true;
        } else if (id == R.id.nav_kelola) {
            // Tindakan yang akan diambil saat item menu "Kelola" dipilih
            return true;
        } else if (id == R.id.nav_mingguan) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Enumerator") || userRole.equals("Pimpinan")) {
                Intent mingguIntent = new Intent(this, PelaporanActivity.class);
                startActivity(mingguIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_bulanan) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Enumerator") || userRole.equals("Pimpinan")) {
                Intent bulanIntent = new Intent(this, BulananActivity.class);
                startActivity(bulanIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_tambah_akun) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Admin")) {
                Intent tambahIntent = new Intent(this, TambahActivity.class);
                startActivity(tambahIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_detail_akun) {
            // Pemeriksaan peran dilakukan di sini seperti pada onCreateOptionsMenu
            if (userRole.equals("Admin")) {
                Intent detailIntent = new Intent(this, DetailActivity.class);
                startActivity(detailIntent);
            } else {
                // Handle akses yang tidak diizinkan dengan menampilkan pesan kesalahan
                Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin untuk mengakses halaman ini.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_logout) {
            // Lakukan proses logout di sini
            FirebaseAuth.getInstance().signOut();

            // Setelah logout, arahkan pengguna ke halaman login
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            startActivity(logoutIntent);
            finish(); // Menutup MainActivity agar tidak bisa kembali dengan tombol "back"
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Metode untuk mengambil peran pengguna dari Firebase Realtime Database
    private void getCurrentUserRole() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRoleRef = FirebaseDatabase.getInstance().getReference()
                    .child("User")
                    .child(currentUser.getUid())
                    .child("role");

            userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userRole = dataSnapshot.getValue(String.class);
                        invalidateOptionsMenu(); // Memperbarui tampilan menu
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}
