package com.pangan.kotatomohon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi tombol login
        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(v -> {
            // Mengambil email dan password dari EditText
            String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();
            String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

            // Memanggil metode signIn untuk melakukan otentikasi
            signIn(email, password);
        });

        TextView lupaPasswordTextView = findViewById(R.id.textViewLupaPassword);
        lupaPasswordTextView.setOnClickListener(v -> {
            // Membuat Intent untuk membuka halaman pemulihan kata sandi
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class); // Ganti dengan kelas yang sesuai
            startActivity(intent);
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Otentikasi berhasil, lanjutkan dengan tindakan berikutnya

                            // Arahkan pengguna ke HomeActivity
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        }
                    } else {
                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Log.e("MyApp", "Sign-in gagal: " + errorMessage);

                        Toast.makeText(LoginActivity.this, "Authentication failed: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
