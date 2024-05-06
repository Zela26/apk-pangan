package com.pangan.kotatomohon;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

public class UserManagement {

    public static void setUserRole(Context context, String roleName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(roleName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Atribut peran pengguna telah ditambahkan ke profil pengguna
                                Toast.makeText(context, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Tangani kesalahan saat memperbarui profil
                                // Misalnya, tampilkan pesan kesalahan kepada pengguna
                                Toast.makeText(context, "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Tangani kasus di mana user == null, misalnya, tampilkan pesan kesalahan
            Toast.makeText(context, "Tidak ada pengguna yang terautentikasi.", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName(); // Mendapatkan peran pengguna dari atribut kustom
        } else {
            return null; // Pengguna tidak terautentikasi
        }
    }
}
