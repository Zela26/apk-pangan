package com.pangan.kotatomohon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.pangan.kotatomohon.ui.home.entry.ModelEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntryActivity extends AppCompatActivity {

    private Spinner spinnerKomoditi;
    private EditText editTextJumlahPasokan;
    private EditText editTextAsalPasokan;
    private EditText editTextHargaPasokan;
    private EditText editTextStok;
    private EditText editTextHarga;
    private Spinner spinnerKet;
    private Button buttonSimpan;
    private Menu optionsMenu;
    private String userRole;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private List<String> komoditiList;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        TextView tanggalTextView = findViewById(R.id.tanggalTextView);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String currentDate = dateFormat.format(new Date());
        String fullText = getString(R.string.tanggal_hari_ini, currentDate);
        tanggalTextView.setText(fullText);

        // Inisialisasi komponen tampilan
        spinnerKomoditi = findViewById(R.id.spinnerKomoditi);
        editTextJumlahPasokan = findViewById(R.id.editTextJumlahPasokan);
        editTextAsalPasokan = findViewById(R.id.editTextAsalPasokan);
        editTextHargaPasokan = findViewById(R.id.editTextHargaPasokan);
        editTextStok = findViewById(R.id.editTextStok);
        editTextHarga = findViewById(R.id.editTextHarga);
        spinnerKet = findViewById(R.id.spinnerKet);
        buttonSimpan = findViewById(R.id.buttonSimpan);

        buttonSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ambil tanggal hari ini dalam format yang sesuai
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                String currentDate = dateFormat.format(new Date());

                // Ambil nilai dari komponen tampilan
                String getSpinnerKomoditi = spinnerKomoditi.getSelectedItem().toString();
                String getEditTextJumlahPasokan = editTextJumlahPasokan.getText().toString();
                String getEditTextAsalPasokan = editTextAsalPasokan.getText().toString();
                String getEditTextHargaPasokan = editTextHargaPasokan.getText().toString();
                String getEditTextStok = editTextStok.getText().toString();
                String getEditTextHarga = editTextHarga.getText().toString();
                String getSpinnerKet = spinnerKet.getSelectedItem().toString();

                if (getEditTextJumlahPasokan.isEmpty()) {
                    editTextJumlahPasokan.setError("Masukkan Jumlah Pasokan");
                } else if (getEditTextAsalPasokan.isEmpty()) {
                    editTextAsalPasokan.setError("Masukkan Asal Pasokan");
                } else if (getEditTextHargaPasokan.isEmpty()) {
                    editTextHargaPasokan.setError("Masukkan Harga Pasokan");
                } else if (getEditTextStok.isEmpty()) {
                    editTextStok.setError("Masukkan Stok");
                } else if (getEditTextHarga.isEmpty()) {
                    editTextHarga.setError("Masukkan Harga");
                } else {
                    // Simpan data ke Firebase
                    ModelEntry modelEntry = new ModelEntry(getSpinnerKomoditi, getEditTextJumlahPasokan, getEditTextAsalPasokan, getEditTextHargaPasokan, getEditTextStok, getEditTextHarga, getSpinnerKet, currentDate);

                    // Tambahkan tanggal ke model data
                    modelEntry.setTanggalData(currentDate);

                    database.child("Pangan").push().setValue(modelEntry).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EntryActivity.this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EntryActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EntryActivity.this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        komoditiList = new ArrayList<>();
        ArrayAdapter<CharSequence> komoditiAdapter = ArrayAdapter.createFromResource(
                this, R.array.komoditi_array, android.R.layout.simple_spinner_item);
        komoditiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKomoditi.setAdapter(komoditiAdapter);

        for (int i = 0; i < komoditiAdapter.getCount(); i++) {
            komoditiList.add(komoditiAdapter.getItem(i).toString());
        }

        // Listener untuk Spinner (Pilihan Komoditi)
        spinnerKomoditi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedKomoditi = komoditiList.get(position);
                Toast.makeText(EntryActivity.this, "Anda memilih komoditi: " + selectedKomoditi, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                    Intent homeIntent = new Intent(EntryActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(EntryActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(EntryActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(EntryActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(EntryActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(EntryActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(EntryActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(EntryActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(EntryActivity.this, DetailActivity.class);
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
