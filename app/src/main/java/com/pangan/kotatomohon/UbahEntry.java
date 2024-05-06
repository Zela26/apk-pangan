package com.pangan.kotatomohon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UbahEntry extends AppCompatActivity {

    // Deklarasikan variabel untuk komponen UI
    private TextView tanggalTextView;
    private Spinner spinnerKomoditi;
    private EditText editTextJumlahPasokan;
    private EditText editTextAsalPasokan;
    private EditText editTextHargaPasokan;
    private EditText editTextStok;
    private EditText editTextHarga;
    private Spinner spinnerKet;
    private Button buttonSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubahentry);

        // Inisialisasi komponen UI
        tanggalTextView = findViewById(R.id.tanggalTextView);
        spinnerKomoditi = findViewById(R.id.spinnerKomoditi);
        editTextJumlahPasokan = findViewById(R.id.editTextJumlahPasokan);
        editTextAsalPasokan = findViewById(R.id.editTextAsalPasokan);
        editTextHargaPasokan = findViewById(R.id.editTextHargaPasokan);
        editTextStok = findViewById(R.id.editTextStok);
        editTextHarga = findViewById(R.id.editTextHarga);
        spinnerKet = findViewById(R.id.spinnerKet);
        buttonSimpan = findViewById(R.id.buttonSimpan);

        // Mendapatkan intent yang memicu aktivitas ini
        Intent intent = getIntent();

        // Periksa apakah intent memiliki data tambahan
        if (intent != null && intent.getExtras() != null) {
            String tanggal = intent.getStringExtra("tanggal");
            String komoditi = intent.getStringExtra("komoditi");
            String jumlah = intent.getStringExtra("jumlah");
            String asal = intent.getStringExtra("asal");
            String hargaPasokan = intent.getStringExtra("hargapasokan");
            String stok = intent.getStringExtra("stok");
            String harga = intent.getStringExtra("harga");
            String keterangan = intent.getStringExtra("ket");

            // Isi komponen UI dengan data yang diterima dari intent
            tanggalTextView.setText(tanggal);
            spinnerKomoditi.setSelection(getIndex(spinnerKomoditi, komoditi));
            editTextJumlahPasokan.setText(jumlah);
            editTextAsalPasokan.setText(asal);
            editTextHargaPasokan.setText(hargaPasokan);
            editTextStok.setText(stok);
            editTextHarga.setText(harga);
            spinnerKet.setSelection(getIndex(spinnerKet, keterangan));
        }

        // Menambahkan OnClickListener ke tombol Simpan
        buttonSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dapatkan referensi ke Firebase Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Pangan");

                // Dapatkan nilai dari komponen UI
                String komoditi = spinnerKomoditi.getSelectedItem().toString();
                String jumlah = editTextJumlahPasokan.getText().toString();
                String asal = editTextAsalPasokan.getText().toString();
                String hargaPasokan = editTextHargaPasokan.getText().toString();
                String stok = editTextStok.getText().toString();
                String harga = editTextHarga.getText().toString();
                String keterangan = spinnerKet.getSelectedItem().toString();

                // Dapatkan data yang akan diupdate dari intent
                String keyToUpdate = getIntent().getStringExtra("key_data_to_update");

                // Buat objek data yang akan diupdate
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("spinnerKomoditi", komoditi);
                updatedData.put("editTextJumlahPasokan", jumlah);
                updatedData.put("editTextAsalPasokan", asal);
                updatedData.put("editTextHargaPasokan", hargaPasokan);
                updatedData.put("editTextStok", stok);
                updatedData.put("editTextHarga", harga);
                updatedData.put("editTextKeterangan", keterangan);

                // Update data di Firebase Database
                databaseReference.child(keyToUpdate).updateChildren(updatedData, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@NonNull DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // Data berhasil diupdate
                            Toast.makeText(UbahEntry.this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show();
                            finish(); // Kembali ke aktivitas sebelumnya atau tutup aktivitas ini
                        } else {
                            // Terjadi kesalahan saat update
                            Toast.makeText(UbahEntry.this, "Gagal mengupdate data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // Metode untuk mendapatkan indeks pilihan spinner berdasarkan nilai
    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i;
            }
        }
        return 0; // Default jika tidak ditemukan
    }
}
