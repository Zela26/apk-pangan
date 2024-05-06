package com.pangan.kotatomohon;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class BulananActivity extends AppCompatActivity {

    private TableLayout reportTable;
    private DatabaseReference databaseReference;
    private Spinner spinnerBulan, spinnerTahun;
    private int nextRowIndex = 1;
    private Button saveToPdfButton;
    private HashMap<String, Integer> komoditiIndices = new HashMap<>();
    private static final double TOLERANCE = 1e-6;
    private double[] prices = new double[7];
    private HashMap<String, Double> averagePricesMap = new HashMap<>();
    HashMap<String, HashMap<String, ArrayList<Double>>> pricesMap = new HashMap<>();
    private String userRole;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_bulanan);

        reportTable = findViewById(R.id.reportTable2);
        spinnerBulan = findViewById(R.id.spinnerBulan);
        spinnerTahun = findViewById(R.id.spinnerTahun);
        saveToPdfButton = findViewById(R.id.btnSaveToPDF2);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.bulan, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBulan.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.tahun, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTahun.setAdapter(yearAdapter);

        spinnerBulan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                loadTableData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        spinnerTahun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                loadTableData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Pangan");
        loadTableData();
        saveToPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTableToPdf();
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
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent homeIntent = new Intent(BulananActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(BulananActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entry && hasPermission("Enumerator")) {
                    Intent entryIntent = new Intent(BulananActivity.this, EntryActivity.class);
                    startActivity(entryIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(BulananActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(BulananActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(BulananActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(BulananActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(BulananActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(BulananActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(BulananActivity.this, DetailActivity.class);
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

    private void loadTableData() {
        String selectedMonth = spinnerBulan.getSelectedItem().toString();
        String selectedYear = spinnerTahun.getSelectedItem().toString();

        reportTable.removeAllViews();

        addTableHeader();
        Log.d("PelaporanActivity", "Loading table data for Week: " + ", Month: " + selectedMonth + ", Year: " + selectedYear);
        nextRowIndex = 1;
        HashSet<String> addedKomoditi = new HashSet<>(); // Menyimpan daftar komoditi yang telah ditambahkan ke tabel

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String komoditi = childSnapshot.child("spinnerKomoditi").getValue(String.class);
                        String tanggalData = childSnapshot.child("tanggalData").getValue(String.class);
                        // Panggil getAveragePriceForDateAndCommodity untuk mendapatkan nilai rata-rata harga

                        if (tanggalData != null && isDateInSelectedMonthAndYear(tanggalData, selectedMonth, selectedYear)) {
                            String key = komoditi + "_" + tanggalData;
                            double averagePrice = getAveragePriceForDateAndCommodity(dataSnapshot, komoditi, tanggalData);
                            averagePricesMap.put(key, averagePrice);

                            int dayIndex = getDayIndex(tanggalData);
                            if (dayIndex != -1 && !addedKomoditi.contains(komoditi)) {
                                Log.d("DataBeforeAverage", "Komoditi: " + komoditi + ", Tanggal: " + tanggalData + ", Harga: " + prices[dayIndex]);

                                prices[dayIndex] = averagePrice;
                                addDataToTable(komoditi, tanggalData, prices, selectedYear, selectedMonth, dataSnapshot);
                                saveAveragePriceToFirebase(komoditi, selectedMonth, tanggalData, averagePrice);
                                savePriceDataToFirebase(komoditi, selectedMonth, selectedYear, averagePrice);
                                addedKomoditi.add(komoditi);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }

    private boolean isDateInSelectedMonthAndYear(String tanggalData, String selectedMonth, String selectedYear) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")); // Format dengan bahasa Indonesia
            Date date = sdf.parse(tanggalData);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int monthData = cal.get(Calendar.MONTH) + 1; // Bulan dimulai dari 0, jadi perlu ditambah 1
            int yearData = cal.get(Calendar.YEAR);

            int month = getMonthNumberNumber(selectedMonth); // Mengubah nama bulan menjadi angka
            int year = Integer.parseInt(selectedYear);

            return monthData == month && yearData == year;
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getMonthNumberNumber(String monthName) {
        switch (monthName.toLowerCase()) {
            case "januari":
                return 1;
            case "februari":
                return 2;
            case "maret":
                return 3;
            case "april":
                return 4;
            case "mei":
                return 5;
            case "juni":
                return 6;
            case "juli":
                return 7;
            case "agustus":
                return 8;
            case "september":
                return 9;
            case "oktober":
                return 10;
            case "november":
                return 11;
            case "desember":
                return 12;
            default:
                throw new IllegalArgumentException("Bulan tidak valid: " + monthName);
        }
    }



    private int getDayIndex(String tanggalData) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            Date date = sdf.parse(tanggalData);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Mendapatkan indeks hari dalam seminggu (0 untuk Minggu, 1 untuk Senin, dst)
            return calendar.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1; // Jika terjadi kesalahan
    }

    private double getPrice(DataSnapshot dataSnapshot) {
        String hargaStr = dataSnapshot.getValue(String.class);
        if (hargaStr != null && !hargaStr.isEmpty()) {
            try {
                return Double.parseDouble(hargaStr);
            } catch (NumberFormatException e) {
                // Handle error parsing harga
                Log.e("PelaporanActivity", "Error parsing harga: " + e.getMessage());
            }
        }
        return 0.0;
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        headerRow.setPadding(0, 16, 0, 16);

        // Add No column header
        addCellToRow(headerRow, "No", true);
        // Add Komoditi column header
        addCellToRow(headerRow, "Komoditi", true);
// Dapatkan bulan dan tahun yang dipilih
        int selectedMonthIndex = spinnerBulan.getSelectedItemPosition(); // Indeks dimulai dari 0
        int selectedYear = Integer.parseInt(spinnerTahun.getSelectedItem().toString());

// Set tanggal awal ke 1
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonthIndex); // Menggunakan indeks bulan yang benar
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

// Set tanggal akhir ke hari terakhir dari bulan yang dipilih
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();

// Format tanggal menjadi "dd"
        SimpleDateFormat sdf = new SimpleDateFormat("dd");

        // Add tanggal columns headers
        while (!startDate.after(endDate)) {
            addCellToRow(headerRow, sdf.format(startDate), true);
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startDate = calendar.getTime();
        }

        // Add Harga Rata-rata column header
        addCellToRow(headerRow, "Harga Rata-rata", true);
        // Add Harga Tertinggi column header
        addCellToRow(headerRow, "Harga Tertinggi", true);
        // Add Harga Terendah column header
        addCellToRow(headerRow, "Harga Terendah", true);
        // Add Harga Minggu Lalu column header
        addCellToRow(headerRow, "Harga Bulan Lalu", true);
        // Add Harga Minggu Lalu vs Minggu Ini column header
        addCellToRow(headerRow, "Harga Bulan Lalu vs Bulan Ini", true);
        // Add Ket column header
        addCellToRow(headerRow, "Ket", true);

        // Add the header row to the table
        reportTable.addView(headerRow);
    }

    private void addCellToRow(TableRow row, String text, boolean isHeader) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(8, 8, 8, 8);
        cell.setTextColor(ContextCompat.getColor(this, isHeader ? R.color.black : R.color.gray));
        cell.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        row.addView(cell);
    }

    private void addDataToTable(String komoditi, String tanggalData, double[] prices, String selectedYear, String selectedMonth, DataSnapshot dataSnapshot) {
        int rowIndex = getIndexByKomoditi(komoditi, tanggalData);
        boolean komoditiAlreadyExists = rowIndex != -1;
        TableRow rowToUpdate;

        if (!komoditiAlreadyExists) {
            // Komoditi belum ada dalam tabel, buat baris baru
            rowToUpdate = new TableRow(this);
            addCellToRow(rowToUpdate, String.valueOf(nextRowIndex)); // Menggunakan nomor urut berikutnya
            nextRowIndex++; // Increment nomor urut berikutnya
            addCellToRow(rowToUpdate, komoditi); // Tambahkan kolom komoditi
        } else {
            // Komoditi sudah ada dalam tabel, dapatkan baris yang akan diperbarui
            rowToUpdate = (TableRow) reportTable.getChildAt(rowIndex);
        }

// Set tanggal awal ke 1
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.YEAR, Integer.parseInt(selectedYear));
        calStart.set(Calendar.MONTH, getMonthNumber(selectedMonth));
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calStart.getTime();

// Set tanggal akhir ke hari terakhir dari bulan yang dipilih
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.YEAR, Integer.parseInt(selectedYear));
        calEnd.set(Calendar.MONTH, getMonthNumber(selectedMonth));
        calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calEnd.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        while (!startDate.after(endDate)) {
            int column = (int) ((startDate.getTime() - calStart.getTime().getTime()) / (1000 * 60 * 60 * 24)); // Calculate the column index for the date
            if (!Double.isNaN(prices[column])) {
                String tanggal = sdf.format(startDate);
                double averagePriceForDateAndCommodity = getAveragePriceForDateAndCommodity(dataSnapshot, komoditi, tanggal);
                addCellToRow(rowToUpdate, String.valueOf(averagePriceForDateAndCommodity));
                saveAveragePriceToFirebase(komoditi, selectedMonth, tanggal, averagePriceForDateAndCommodity);
            } else {
                addCellToRow(rowToUpdate, "-");
            }
            calStart.add(Calendar.DAY_OF_MONTH, 1);
            startDate = calStart.getTime();
        }

        Spinner spinnerBulan = findViewById(R.id.spinnerBulan);
        String month = spinnerBulan.getSelectedItem().toString();

        getAveragePrice(komoditi, month, tanggalData, averagePrice -> {
            Log.d("DEBUG", "Harga rata-rata dari " + komoditi + " pada bulan " + month + " adalah " + averagePrice);
            savePriceDataToFirebase(komoditi, selectedMonth, selectedYear, averagePrice);
            addCellToRow(rowToUpdate, String.valueOf(averagePrice));

            // Calculate highest price
            getHighestPrice(komoditi, month, tanggalData, highestPrice -> {
                Log.d("DEBUG", "Harga Tertinggi: " + komoditi + " pada bulan " + month + " adalah " + highestPrice);
                addCellToRow(rowToUpdate, String.valueOf(highestPrice));

                getLowestPrice(komoditi, month, tanggalData, lowestPrice -> {
                    Log.d("DEBUG", "Harga Terendah: " + komoditi + " pada bulan " + month + " adalah " + lowestPrice);
                    addCellToRow(rowToUpdate, String.valueOf(lowestPrice));

                    calculateLastMonthAveragePrice(komoditi, selectedMonth, selectedYear, new OnPriceCalculatedListener() {
                        @Override
                        public void onPriceCalculated(double lastMonthPrice) {
                            Log.d("DEBUG", "Rata-rata Harga Bulan Lalu: " + lastMonthPrice);
                            addCellToRow(rowToUpdate, String.valueOf(lastMonthPrice));

                    double priceChange = calculatePriceChange(lastMonthPrice, averagePrice);
                    Log.d("DEBUG", "Perubahan Harga: " + priceChange);
                    addCellToRow(rowToUpdate, String.valueOf(priceChange));

                    String trend = calculateTrend(lastMonthPrice, averagePrice);
                    Log.d("DEBUG", "Tren Harga: " + trend);
                    addCellToRow(rowToUpdate, trend);

                    if (!komoditiAlreadyExists) {
                        // Jika komoditi belum ada dalam tabel, tambahkan baris baru ke tabel
                        reportTable.addView(rowToUpdate); // Tambahkan baris baru di akhir tabel
                    }
                }
            });
        });
    });
        });
    }
    private int getIndexByKomoditi(String komoditi, String tanggalData) {
        for (int i = 0; i < reportTable.getChildCount(); i++) {
            TableRow row = (TableRow) reportTable.getChildAt(i);
            if (row != null && row.getChildCount() > 1) {
                TextView textViewKomoditi = (TextView) row.getChildAt(1);
                TextView textViewTanggal = (TextView) row.getChildAt(2);
                if (textViewKomoditi != null && textViewTanggal != null) {
                    String rowKomoditi = textViewKomoditi.getText().toString();
                    String rowTanggal = textViewTanggal.getText().toString();
                    if (komoditi.equals(rowKomoditi) && tanggalData.equals(rowTanggal)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    private double getAveragePriceForDateAndCommodity(DataSnapshot dataSnapshot, String komoditi, String tanggal) {
        double totalHarga = 0;
        int jumlahData = 0;

        // Loop through data to calculate total price and count
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String komoditiData = childSnapshot.child("spinnerKomoditi").getValue(String.class);
            String tanggalData = childSnapshot.child("tanggalData").getValue(String.class);
            String hargaStr = childSnapshot.child("editTextHarga").getValue(String.class);

            if (komoditiData != null && komoditiData.equals(komoditi) && tanggalData != null && tanggalData.equals(tanggal)) {
                double harga = Double.parseDouble(hargaStr); // Parse string to double
                totalHarga += harga;
                jumlahData++;
            }
        }

        // Calculate average price
        if (jumlahData > 0) {
            return totalHarga / jumlahData;
        } else {
            // Alternatively, you can return a special value like Double.NaN to indicate no data
            return 0;
        }
    }

    private void saveAveragePriceToFirebase(String komoditi, String month, String tanggalData, double averagePrice) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices_month")
                .child(komoditi)
                .child(month)
                .child(tanggalData);

        databaseReference.child("harga").setValue(averagePrice)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Nilai rata-rata harga berhasil disimpan ke Firebase");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Gagal menyimpan nilai rata-rata harga ke Firebase", e);
                    }
                });
    }


    private void getAveragePrice(String komoditi, String month, String tanggalData, AveragePriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices_month")
                .child(komoditi)
                .child(month);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double total = 0;
                int count = 0;

                // Menghitung total harga yang ada di Firebase untuk setiap tanggal dalam bulan
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    Long hargaLong = (Long) dateSnapshot.child("harga").getValue();
                    if (hargaLong != null) {
                        double price = hargaLong.doubleValue();
                        if (price != 0) { // Memastikan harga tidak sama dengan 0
                            total += price;
                            count++;
                            Log.d("Firebase", "Harga dari Firebase: " + price);
                        }
                    }
                }

                // Menambahkan harga baru ke total jika tanggalData belum ada di Firebase
                if (!dataSnapshot.hasChild(tanggalData)) {
                    // Contoh harga baru (ganti dengan cara mendapatkan harga baru dari sumber yang sesuai)
                    double newPrice = 0;
                    if (newPrice != 0) { // Memastikan harga tidak sama dengan 0
                        total += newPrice;
                        count++;
                        Log.d("Firebase", "Harga baru: " + newPrice);
                        databaseReference.child(tanggalData).setValue(newPrice); // Menyimpan harga baru ke Firebase
                    }
                }

                if (count > 0) {
                    double averagePrice = total / count;
                    Log.d("Firebase", "Rata-rata harga: " + averagePrice);
                    callback.onAveragePriceReceived(averagePrice);
                } else {
                    Log.d("Firebase", "Tidak ada data harga.");
                    callback.onAveragePriceReceived(0); // Return 0 as average price
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });
    }
    interface AveragePriceCallback {
        void onAveragePriceReceived(double averagePrice);
    }

    private void savePriceDataToFirebase(String komoditi, String month, String year, double averagePrice) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Month_average_prices")
                .child(year)
                .child(month)
                .child(komoditi); // Gunakan komoditi sebagai child terakhir

        // Ubah logika disini agar menyimpan nilai rata-rata harga yang diterima dari getAveragePrice
        databaseReference.setValue(averagePrice)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Nilai rata-rata harga berhasil disimpan ke Firebase");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Gagal menyimpan nilai rata-rata harga ke Firebase", e);
                    }
                });
    }


    private void getHighestPrice(String komoditi, String month, String tanggalData, BulananActivity.HighestPriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices_month")
                .child(komoditi)
                .child(month);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double highestPrice = 0;

                // Mencari harga tertinggi dari harga yang ada di Firebase
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    Long hargaLong = (Long) dateSnapshot.child("harga").getValue();
                    if (hargaLong != null) {
                        double price = hargaLong.doubleValue();
                        if (price > highestPrice) {
                            highestPrice = price;
                        }
                        Log.d("Firebase", "Harga dari Firebase: " + price);
                    }
                }

                // Menambahkan harga baru ke total jika tanggalData belum ada di Firebase
                if (!dataSnapshot.hasChild(tanggalData)) {
                    double newPrice = 0; // Get new price from somewhere
                    if (newPrice > highestPrice) {
                        highestPrice = newPrice;
                    }
                    Log.d("Firebase", "Harga baru: " + newPrice);
                }

                if (highestPrice > 0) {
                    Log.d("Firebase", "Harga tertinggi: " + highestPrice);
                    callback.onHighestPriceReceived(highestPrice);
                } else {
                    Log.d("Firebase", "Tidak ada data harga.");
                    callback.onHighestPriceReceived(0); // Return 0 as highest price
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
                Log.e("Firebase", "Error: " + databaseError.getMessage());
                callback.onHighestPriceReceived(0); // Return 0 as highest price
            }
        });
    }

    interface HighestPriceCallback {
        void onHighestPriceReceived(double highestPrice);
    }

    private void getLowestPrice(String komoditi, String month, String tanggalData, BulananActivity.LowestPriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices_month")
                .child(komoditi)
                .child(month);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double lowestPrice = Double.MAX_VALUE; // Initialize with a very high value

                // Mencari harga terendah dari harga yang ada di Firebase
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    Long hargaLong = (Long) dateSnapshot.child("harga").getValue();
                    if (hargaLong != null) {
                        double price = hargaLong.doubleValue();
                        if (price < lowestPrice && price != 0) { // Memastikan harga tidak sama dengan 0
                            lowestPrice = price;
                        }
                        Log.d("Firebase", "Harga dari Firebase: " + price);
                    }
                }

                // Menambahkan harga baru ke total jika tanggalData belum ada di Firebase
                if (!dataSnapshot.hasChild(tanggalData)) {
                    double newPrice = 0; // Get new price from somewhere
                    if (newPrice < lowestPrice && newPrice != 0) { // Memastikan harga tidak sama dengan 0
                        lowestPrice = newPrice;
                    }
                    Log.d("Firebase", "Harga baru: " + newPrice);
                }

                if (lowestPrice < Double.MAX_VALUE) {
                    Log.d("Firebase", "Harga terendah: " + lowestPrice);
                    callback.onLowestPriceReceived(lowestPrice);
                } else {
                    Log.d("Firebase", "Tidak ada data harga.");
                    callback.onLowestPriceReceived(0); // Return 0 as lowest price
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
                Log.e("Firebase", "Error: " + databaseError.getMessage());
                callback.onLowestPriceReceived(0); // Return 0 as lowest price
            }
        });
    }


    interface LowestPriceCallback {
        void onLowestPriceReceived(double lowestPrice);
    }

    private int getMonth(String monthName) {
        switch (monthName) {
            case "Januari": return Calendar.JANUARY;
            case "Februari": return Calendar.FEBRUARY;
            case "Maret": return Calendar.MARCH;
            case "April": return Calendar.APRIL;
            case "Mei": return Calendar.MAY;
            case "Juni": return Calendar.JUNE;
            case "Juli": return Calendar.JULY;
            case "Agustus": return Calendar.AUGUST;
            case "September": return Calendar.SEPTEMBER;
            case "Oktober": return Calendar.OCTOBER;
            case "November": return Calendar.NOVEMBER;
            case "Desember": return Calendar.DECEMBER;
            default: return -1; // Invalid month name
        }
    }


    private void calculateLastMonthAveragePrice(String komoditi, String month, String year, OnPriceCalculatedListener listener) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));

        String[] monthNames = new DateFormatSymbols(new Locale("id", "ID")).getMonths();
        int monthIndex = Arrays.asList(monthNames).indexOf(month);
        if (monthIndex == -1) {
            Log.e("BulananActivity", "Nama bulan tidak valid: " + month);
            return;
        }

        calendar.set(Calendar.MONTH, monthIndex);

        // Jika bulan yang dipilih bukan Januari, ambil bulan sebelumnya dari tahun yang sama
        if (calendar.get(Calendar.MONTH) != Calendar.JANUARY) {
            calendar.add(Calendar.MONTH, -1);
        } else {
            // Jika bulan yang dipilih adalah Januari, ambil bulan terakhir dari tahun sebelumnya
            calendar.add(Calendar.MONTH, -1);
            calendar.add(Calendar.YEAR, -1);
        }

        String lastMonthDate = sdf.format(calendar.getTime());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Month_average_prices")
                .child(String.valueOf(calendar.get(Calendar.YEAR)))
                .child(new SimpleDateFormat("MMMM", new Locale("id", "ID")).format(calendar.getTime()))
                .child(komoditi);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double lastMonthPrice = dataSnapshot.getValue(Double.class);
                    listener.onPriceCalculated(lastMonthPrice);
                } else {
                    Log.d("Firebase", "Tidak ada data harga untuk bulan sebelumnya.");
                    listener.onPriceCalculated(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving last month's price: " + databaseError.getMessage());
                listener.onPriceCalculated(0);
            }
        });
    }



    // Interface untuk listener yang memberikan nilai rata-rata harga bulan lalu
    interface OnPriceCalculatedListener {
        void onPriceCalculated(double lastMonthPrice);
    }

    private double calculatePriceChange(double lastMonthAveragePrice, double averagePrice) {
        if (lastMonthAveragePrice != 0) {
            double priceChange = ((averagePrice - lastMonthAveragePrice) / averagePrice) * 100;
            return Math.round(priceChange * 100.0) / 100.0; // Membulatkan ke 2 angka di belakang koma
        } else {
            return 0;
        }
    }

    private String calculateTrend(double lastMonthAveragePrice, double averagePrice) {
        double diff = Math.abs(lastMonthAveragePrice - averagePrice);
        if (diff < TOLERANCE) {
            return "Stabil";
        } else if (lastMonthAveragePrice > averagePrice) {
            return "Turun";
        } else {
            return "Naik";
        }
    }

    private void addCellToRow(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(8, 8, 8, 8);
        cell.setTextColor(ContextCompat.getColor(this, R.color.black));
        row.addView(cell);
    }

    private int getMonthNumber(String month) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", new Locale("id", "ID"));
        Date date = null;
        try {
            date = dateFormat.parse(month);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.MONTH);
    }


    private void saveTableToPdf() {
        String filePath = getFilesDir() + File.separator + "laporanbulanan.pdf";
        Document document = new Document(PageSize.A4.rotate()); // Mengatur halaman menjadi landscape

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            String selectedMonth = spinnerBulan.getSelectedItem().toString();
            String selectedYear = spinnerTahun.getSelectedItem().toString();

            // Hitung jumlah kolom berdasarkan pilihan pengguna
            int numColumns = getNumColumns(selectedMonth, selectedYear);

            // Membuat judul dengan informasi bulan dan tahun
            String titleText = "Laporan Bulan " + selectedMonth + " " + selectedYear;
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Tambahkan Tabel ke PDF
            PdfPTable pdfPTable = new PdfPTable(numColumns);
            pdfPTable.setWidthPercentage(100); // Set lebar tabel menjadi 100% dari lebar halaman
            pdfPTable.setSpacingBefore(12); // Spasi sebelum tabel

            // Iterasi melalui setiap baris dalam tabel aplikasi
            for (int i = 0; i < reportTable.getChildCount(); i++) {
                View view = reportTable.getChildAt(i);
                if (view instanceof TableRow) {
                    TableRow row = (TableRow) view;
                    // Iterasi melalui setiap sel dalam baris aplikasi
                    for (int j = 0; j < row.getChildCount(); j++) {
                        View cellView = row.getChildAt(j);
                        if (cellView instanceof TextView) {
                            TextView textView = (TextView) cellView;
                            PdfPCell pdfPCell = new PdfPCell();
                            pdfPCell.setPadding(8);
                            pdfPCell.setPhrase(new Phrase(textView.getText().toString()));

                            // Set teks dalam sel agar horizontal
                            pdfPCell.setRotation(0);

                            pdfPTable.addCell(pdfPCell);
                        }
                    }
                }
            }

            // Menambahkan tabel ke dokumen
            document.add(pdfPTable);

            document.close();

            // Bagikan PDF ke aplikasi lain
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(this, "com.pangan.kotatomohon.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "Bagikan Laporan Bulanan"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNumColumns(String selectedMonth, String selectedYear) {
        // Implementasi logika untuk menghitung jumlah kolom berdasarkan bulan dan tahun yang dipilih
        // Contoh implementasi sederhana:
        switch (selectedMonth) {
            case "Januari":
            case "Maret":
            case "Mei":
            case "Juli":
            case "Agustus":
            case "Oktober":
            case "Desember":
                return 31 + 8; // Jumlah hari dalam bulan + 8 kolom lainnya
            case "April":
            case "Juni":
            case "September":
            case "November":
                return 30 + 8; // Jumlah hari dalam bulan + 8 kolom lainnya
            case "Februari":
                int year = Integer.parseInt(selectedYear);
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    return 29 + 8; // Tahun kabisat, Februari memiliki 29 hari + 8 kolom lainnya
                } else {
                    return 28 + 8; // Bukan tahun kabisat, Februari memiliki 28 hari + 8 kolom lainnya
                }
            default:
                return 0; // Default jika tidak ada logika yang sesuai
        }
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
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userRole = dataSnapshot.getValue(String.class);
                        Log.d("UserRole", "User role from Firebase: " + userRole);
                        setMenuVisibility(true); // Pengguna sudah login
                    } else {
                        setMenuVisibility(false); // Pengguna belum login
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
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


