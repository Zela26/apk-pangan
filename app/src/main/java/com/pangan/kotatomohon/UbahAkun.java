package com.pangan.kotatomohon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UbahAkun extends AppCompatActivity {

    private EditText editNama, editEmail, editKataSandi;
    private Button btnSimpanPerubahan;
    private Spinner spinnerRole;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ubah_akun);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editNama = findViewById(R.id.editNama);
        editEmail = findViewById(R.id.editEmail);
        editKataSandi = findViewById(R.id.editKataSandi);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        // Menerima data dari Intent
        Intent intent = getIntent();
        if (intent != null) {
            String nama = intent.getStringExtra("nama");
            String email = intent.getStringExtra("email");
            String kataSandi = intent.getStringExtra("kataSandi");
            String role = intent.getStringExtra("role");

            // Mengisi EditText dan Spinner dengan data yang diterima
            editNama.setText(nama);
            editEmail.setText(email);
            editKataSandi.setText(kataSandi);

            // Set pilihan Spinner sesuai dengan peran
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRole.setAdapter(adapter);
            int spinnerPosition = adapter.getPosition(role);
            spinnerRole.setSelection(spinnerPosition);
        }

        // Menambahkan onClickListener ke tombol Simpan Perubahan
        btnSimpanPerubahan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanPerubahanAkun();
            }
        });
    }

    private void simpanPerubahanAkun() {
        // Ambil nilai dari EditText
        final String nama = editNama.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String role = spinnerRole.getSelectedItem().toString();

        // Anda dapat menambahkan validasi di sini jika diperlukan

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Perbarui profil (nama) pengguna
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nama)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Profil (nama) berhasil diperbarui, lanjutkan untuk menyimpan data lain di Database Realtime
                            String userId = currentUser.getUid();
                            User user = new User(userId, nama, email, role);

                            // Perbarui data pengguna di Firebase Realtime Database hanya jika userId sesuai
                            mDatabase.child("User").child(userId).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Tampilkan pesan sukses dan data yang sudah diubah
                                                Toast.makeText(UbahAkun.this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();

                                                // Kirim data yang sudah diubah ke DetailActivity
                                                Intent intent = new Intent(UbahAkun.this, DetailActivity.class);
                                                intent.putExtra("nama", nama);
                                                intent.putExtra("email", email);
                                                intent.putExtra("role", role);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                String pesanKesalahan = task.getException() != null ? task.getException().getMessage() : "Tidak ada pesan kesalahan yang tersedia";
                                                Toast.makeText(UbahAkun.this, "Gagal menyimpan data: " + pesanKesalahan, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
