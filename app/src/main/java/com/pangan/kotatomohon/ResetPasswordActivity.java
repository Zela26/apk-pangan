package com.pangan.kotatomohon;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi tombol reset kata sandi dan menambahkan onClickListener
        Button btnLupaPassword = findViewById(R.id.btnLupaPassword);
        btnLupaPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mengambil email dari EditText
                String email = ((EditText) findViewById(R.id.editTextResetEmail)).getText().toString();

                // Memanggil metode resetPassword untuk memulai proses pemulihan kata sandi
                resetPassword(email);
            }
        });
    }

    // Metode untuk memulai proses pemulihan kata sandi
    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Pemulihan kata sandi berhasil, beri tahu pengguna
                            Toast.makeText(ResetPasswordActivity.this, "Email pemulihan kata sandi telah dikirim.",
                                    Toast.LENGTH_SHORT).show();
                            // Kembali ke aktivitas login atau halaman lain yang sesuai
                            finish();
                        } else {
                            // Pemulihan kata sandi gagal, tampilkan pesan kesalahan kepada pengguna
                            Toast.makeText(ResetPasswordActivity.this, "Pemulihan kata sandi gagal. Pastikan email benar.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
