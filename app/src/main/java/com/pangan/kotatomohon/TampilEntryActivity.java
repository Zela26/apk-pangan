package com.pangan.kotatomohon;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TampilEntryActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TableLayout tableData;
    private Spinner spinnerMonth;
    private Spinner spinnerYear;
    private Menu optionsMenu;
    private DrawerLayout drawer;
    private String userRole;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_tampilentry);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        tableData = findViewById(R.id.tableData);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Pangan");
        // Isi spinner dengan data (Anda mungkin perlu menyediakan sumber data yang sesuai)
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBulan = spinnerMonth.getSelectedItem().toString();
                String selectedTahun = spinnerYear.getSelectedItem().toString();

                // Perbarui tabel berdasarkan bulan dan tahun yang dipilih
                updateTableData(selectedBulan, selectedTahun);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Implementasi kosong, karena Anda tidak ingin melakukan apa pun saat tidak ada yang dipilih.
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBulan = spinnerMonth.getSelectedItem().toString();
                String selectedTahun = spinnerYear.getSelectedItem().toString();

                // Perbarui tabel berdasarkan bulan dan tahun yang dipilih
                updateTableData(selectedBulan, selectedTahun);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Implementasi kosong, karena Anda tidak ingin melakukan apa pun saat tidak ada yang dipilih.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedBulan = spinnerMonth.getSelectedItem().toString();
        String selectedTahun = spinnerYear.getSelectedItem().toString();
        updateTableData(selectedBulan, selectedTahun);
        getUserRoleFromFirebase(); // Get the user role when the activity resumes
    }


    private void updateTableData(String selectedBulan, String selectedTahun) {
        tableData.removeAllViews();

        addTableHeader();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String tanggal = childSnapshot.child("tanggalData").getValue(String.class);

                        if (isDataInSelectedMonthYear(tanggal, selectedBulan, selectedTahun)) {
                            String komoditi = childSnapshot.child("spinnerKomoditi").getValue(String.class);
                            String jumlah = childSnapshot.child("editTextJumlahPasokan").getValue(String.class);
                            String asal = childSnapshot.child("editTextAsalPasokan").getValue(String.class);
                            String hargaPasokan = childSnapshot.child("editTextHargaPasokan").getValue(String.class);
                            String stok = childSnapshot.child("editTextStok").getValue(String.class);
                            String harga = childSnapshot.child("editTextHarga").getValue(String.class);
                            String keterangan = childSnapshot.child("editTextKeterangan").getValue(String.class);

                            addDataToTable(tanggal, komoditi, jumlah, asal, hargaPasokan, stok, harga, keterangan, childSnapshot);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
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
                    Intent homeIntent = new Intent(TampilEntryActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(TampilEntryActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(TampilEntryActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_entry && hasPermission("Enumerator")) {
                    Intent entryIntent = new Intent(TampilEntryActivity.this, EntryActivity.class);
                    startActivity(entryIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(TampilEntryActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(TampilEntryActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(TampilEntryActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(TampilEntryActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(TampilEntryActivity.this, DetailActivity.class);
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

    private boolean isDataInSelectedMonthYear(String tanggal, String selectedBulan, String selectedTahun) {
        if (tanggal == null || tanggal.isEmpty()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

        try {
            Date date = dateFormat.parse(tanggal);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                int year = calendar.get(Calendar.YEAR);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("id", "ID"));
                String formattedBulan = monthFormat.format(date);
                String formattedTahun = String.valueOf(year);

                return selectedBulan.equals(formattedBulan) && selectedTahun.equals(formattedTahun);
            } else {
                Log.e("DateCheck", "Failed to parse date");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("DateCheck", "Error parsing date: " + e.getMessage());
            return false;
        }
    }
    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        headerRow.setLayoutParams(layoutParams);

        addHeaderCell(headerRow, "Tanggal");
        addHeaderCell(headerRow, "Komoditi");
        addHeaderCell(headerRow, "Jumlah");
        addHeaderCell(headerRow, "Asal");
        addHeaderCell(headerRow, "Harga Pasokan");
        addHeaderCell(headerRow, "Stok");
        addHeaderCell(headerRow, "Harga");
        addHeaderCell(headerRow, "Ket");
        addHeaderCell(headerRow, ""); // Tombol Edit
        addHeaderCell(headerRow, ""); // Tombol Hapus

        tableData.addView(headerRow);
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8); // Atur padding kiri, atas, kanan, bawah
        textView.setGravity(Gravity.CENTER); // Alignteks secara horizontal di tengah
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(textView);
    }

    private void addDataToTable(String tanggal, String komoditi, String jumlah, String asal, String hargaPasokan, String stok, String harga, String keterangan, DataSnapshot childSnapshot) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);

        addDataCell(row, formatDate(tanggal));
        addDataCell(row, komoditi);
        addDataCell(row, jumlah);
        addDataCell(row, asal);
        addDataCell(row, hargaPasokan);
        addDataCell(row, stok);
        addDataCell(row, harga);
        addDataCell(row, keterangan);

        // Tambahkan tombol "Edit" dan "Hapus" di setiap baris
        Button editButton = createEditButton(childSnapshot);
        Button deleteButton = createDeleteButton(childSnapshot);

        row.addView(editButton);
        row.addView(deleteButton);

        // Set tag untuk mengidentifikasi unik setiap baris
        row.setTag(childSnapshot.getKey());

        if (row.getParent() != null) {
            ((ViewGroup) row.getParent()).removeView(row);
        }
        tableData.addView(row);
    }

    private void addDataCell(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(8, 8, 8, 8); // Atur padding kiri, atas, kanan, bawah
        cell.setGravity(Gravity.CENTER);
        row.addView(cell);
    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date date = inputFormat.parse(inputDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("id", "ID"));
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Return empty string if parsing fails
        }
    }

    private Button createDeleteButton(final DataSnapshot childSnapshot) {
        Button deleteButton = new Button(this);
        deleteButton.setText("Hapus");
        deleteButton.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tampilkan dialog konfirmasi penghapusan
                showDeleteConfirmationDialog(childSnapshot);
            }
        });
        return deleteButton;
    }

    private void showDeleteConfirmationDialog(final DataSnapshot childSnapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus data ini?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, perform the deletion
                deleteDataFromFirebase(childSnapshot);
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No, do nothing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteDataFromFirebase(final DataSnapshot childSnapshot) {
        // Implement the logic to delete the data from Firebase
        // For pangan:
        databaseReference.child(childSnapshot.getKey())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Penghapusan berhasil
                            // Anda mungkin ingin memperbarui UI atau menampilkan pesan kepada pengguna
                            Toast.makeText(TampilEntryActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();

                            // Perbarui UI dengan menghapus baris yang sesuai dari TableLayout
                            removeTableRow(childSnapshot.getKey());
                        } else {
                            // Penghapusan gagal
                            // Tangani kesalahan, tampilkan pesan kesalahan, atau log kesalahan
                            Toast.makeText(TampilEntryActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeTableRow(String dataKey) {
        // Iterasi melalui TableLayout untuk menemukan dan menghapus baris yang sesuai
        for (int i = 0; i < tableData.getChildCount(); i++) {
            View child = tableData.getChildAt(i);

            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;

                // Mengasumsikan Anda memiliki pengidentifikasi unik untuk setiap data, seperti dataKey
                // Sesuaikan kondisi ini berdasarkan logika sebenarnya yang Anda gunakan untuk mengidentifikasi setiap baris
                if (row.getTag() != null && row.getTag().equals(dataKey)) {
                    tableData.removeView(row);
                    break;  // Begitu ditemukan dan dihapus, keluar dari loop
                }
            }
        }
    }


    private Button createEditButton(DataSnapshot childSnapshot) {
        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buat Intent untuk membuka UbahEntry
                Intent intent = new Intent(TampilEntryActivity.this, UbahEntry.class);

                // Tambahkan kunci data yang akan diubah ke intent
                intent.putExtra("key_data_to_update", childSnapshot.getKey());

                // Tambahkan data yang perlu diubah ke intent
                intent.putExtra("tanggal", childSnapshot.child("tanggalData").getValue(String.class));
                intent.putExtra("komoditi", childSnapshot.child("spinnerKomoditi").getValue(String.class));
                intent.putExtra("jumlah", childSnapshot.child("editTextJumlahPasokan").getValue(String.class));
                intent.putExtra("asal", childSnapshot.child("editTextAsalPasokan").getValue(String.class));
                intent.putExtra("hargapasokan", childSnapshot.child("editTextHargaPasokan").getValue(String.class));
                intent.putExtra("stok", childSnapshot.child("editTextStok").getValue(String.class));
                intent.putExtra("harga", childSnapshot.child("editTextHarga").getValue(String.class));
                intent.putExtra("ket", childSnapshot.child("editTextKeterangan").getValue(String.class));

                // Mulai UbahEntry dengan intent yang berisi data
                startActivity(intent);
            }
        });
        return editButton;
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


