package com.pangan.kotatomohon;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DetailActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private TableLayout tableLayout;
    private Menu optionsMenu;
    private String userRole;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_detail);

        // Inisialisasi Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userRef = database.getReference("User"); // Sesuaikan dengan nama node di Firebase

        // Inisialisasi TableLayout
        tableLayout = findViewById(R.id.tableLayout);

        // Ambil semua data pengguna dari Firebase
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        addTableRow(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
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
                    Intent homeIntent = new Intent(DetailActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(DetailActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(DetailActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(DetailActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(DetailActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(DetailActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(DetailActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(DetailActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Enumerator")) {
                    Intent detailIntent = new Intent(DetailActivity.this, EntryActivity.class);
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


    private void addTableRow(final User user) {
        TableRow row = new TableRow(this);

        TextView namaTextView = new TextView(this);
        namaTextView.setText(user.getNama());
        namaTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView emailTextView = new TextView(this);
        emailTextView.setText(user.getEmail());
        emailTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView kataSandiTextView = new TextView(this);
        kataSandiTextView.setText("*******");
        kataSandiTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView roleTextView = new TextView(this);
        roleTextView.setText(user.getRole());
        roleTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buat Intent untuk membuka UbahAkun
                Intent intent = new Intent(DetailActivity.this, UbahAkun.class);

                // Sertakan data pengguna saat ini
                intent.putExtra("nama", user.getNama());
                intent.putExtra("email", user.getEmail());
                intent.putExtra("kataSandi", "*******");
                intent.putExtra("role", user.getRole());

                // Mulai UbahAkun dengan Intent yang berisi data
                startActivity(intent);
            }
        });

        Button hapusButton = new Button(this);
        hapusButton.setText("Hapus");
        hapusButton.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hapusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(user); // Show confirmation dialog before deletion
            }
        });

        row.addView(namaTextView);
        row.addView(emailTextView);
        row.addView(kataSandiTextView);
        row.addView(roleTextView);
        row.addView(editButton);
        row.addView(hapusButton);

        tableLayout.addView(row);
    }


    private void showDeleteConfirmationDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus pengguna ini?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, perform the deletion
                deleteUserFromFirebase(user);
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

    private void deleteUserFromFirebase(final User user) {
        // Get the userId using the getter method
        String userId = user.getUserId();

        // Implement the logic to delete the user from Firebase
        // For pangan:
        FirebaseDatabase.getInstance().getReference("User")
                .child(userId) // Use the getter method to access userId
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Penghapusan berhasil
                            // Anda mungkin ingin memperbarui UI atau menampilkan pesan kepada pengguna
                            Toast.makeText(DetailActivity.this, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show();

                            // Perbarui UI dengan menghapus baris yang sesuai dari TableLayout
                            removeTableRow(user);

                            // Kembali ke halaman sebelumnya
                            finish();
                        } else {
                            // Penghapusan gagal
                            // Tangani kesalahan, tampilkan pesan kesalahan, atau log kesalahan
                            Toast.makeText(DetailActivity.this, "Gagal menghapus pengguna", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void removeTableRow(User user) {
        // Iterasi melalui TableLayout untuk menemukan dan menghapus baris yang sesuai
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);

            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;

                // Mengasumsikan Anda memiliki pengidentifikasi unik untuk setiap pengguna, seperti user.getUserId()
                // Sesuaikan kondisi ini berdasarkan logika sebenarnya yang Anda gunakan untuk mengidentifikasi setiap baris
                if (row.getTag() != null && row.getTag().equals(user.getUserId())) {
                    tableLayout.removeView(row);
                    break;  // Begitu ditemukan dan dihapus, keluar dari loop
                }
            }
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
