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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class PelaporanActivity extends AppCompatActivity {

    private TableLayout reportTable;
    private DatabaseReference databaseReference;
    private Spinner spinnerMinggu, spinnerMonth, spinnerYear;
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
        setContentView(R.layout.activity_pelaporan);

        reportTable = findViewById(R.id.reportTable);
        spinnerMinggu = findViewById(R.id.spinnerMinggu);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        saveToPdfButton = findViewById(R.id.btnSaveToPDF);

        ArrayAdapter<CharSequence> weekAdapter = ArrayAdapter.createFromResource(this, R.array.minggu, android.R.layout.simple_spinner_item);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinggu.setAdapter(weekAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.bulan, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.tahun, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set listener untuk spinner
        spinnerMinggu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                loadTableData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                populateWeekSpinner();
                loadTableData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                populateWeekSpinner();
                loadTableData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
        populateWeekSpinner();
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
                    Intent homeIntent = new Intent(PelaporanActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (id == R.id.nav_geomap && hasPermission("Enumerator", "Pimpinan")) {
                    Intent geomapIntent = new Intent(PelaporanActivity.this, GeomapActivity.class);
                    startActivity(geomapIntent);
                    return true;
                } else if (id == R.id.nav_entry && hasPermission("Enumerator")) {
                    Intent entryIntent = new Intent(PelaporanActivity.this, EntryActivity.class);
                    startActivity(entryIntent);
                    return true;
                } else if (id == R.id.nav_entryform && hasPermission("Enumerator")) {
                    Intent entryformIntent = new Intent(PelaporanActivity.this, EntryFormActivity.class);
                    startActivity(entryformIntent);
                    return true;
                } else if (id == R.id.nav_tampil && hasPermission("Enumerator")) {
                    Intent tampilIntent = new Intent(PelaporanActivity.this, TampilEntryActivity.class);
                    startActivity(tampilIntent);
                    return true;
                } else if (id == R.id.nav_grafik) {
                    Intent grafikIntent = new Intent(PelaporanActivity.this, GrafikActivity.class);
                    startActivity(grafikIntent);
                    return true;
                } else if (id == R.id.nav_kelola) {
                    return true;
                } else if (id == R.id.nav_mingguan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent mingguIntent = new Intent(PelaporanActivity.this, PelaporanActivity.class);
                    startActivity(mingguIntent);
                    return true;
                } else if (id == R.id.nav_bulanan && hasPermission("Enumerator", "Pimpinan")) {
                    Intent bulanIntent = new Intent(PelaporanActivity.this, BulananActivity.class);
                    startActivity(bulanIntent);
                    return true;
                } else if (id == R.id.menu_tambah_akun && hasPermission("Admin")) {
                    Intent tambahIntent = new Intent(PelaporanActivity.this, TambahActivity.class);
                    startActivity(tambahIntent);
                    return true;
                } else if (id == R.id.menu_detail_akun && hasPermission("Admin")) {
                    Intent detailIntent = new Intent(PelaporanActivity.this, DetailActivity.class);
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
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String selectedYear = spinnerYear.getSelectedItem().toString();
        int selectedWeek = spinnerMinggu.getSelectedItemPosition() + 1;

        reportTable.removeAllViews();

        addTableHeader();
        Log.d("PelaporanActivity", "Loading table data for Week: " + selectedWeek + ", Month: " + selectedMonth + ", Year: " + selectedYear);
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

                        if (tanggalData != null && isDateInSelectedWeek(tanggalData, selectedMonth, selectedYear, selectedWeek)) {
                            String key = komoditi + "_" + tanggalData;
                            double averagePrice = getAveragePriceForDateAndCommodity(dataSnapshot, komoditi, tanggalData, selectedWeek);
                            averagePricesMap.put(key, averagePrice);

                            int dayIndex = getDayIndex(tanggalData);
                            if (dayIndex != -1 && !addedKomoditi.contains(komoditi)) {
                                Log.d("DataBeforeAverage", "Komoditi: " + komoditi + ", Tanggal: " + tanggalData + ", Harga: " + prices[dayIndex]);

                                prices[dayIndex] = averagePrice;
                                addDataToTable(komoditi, tanggalData, prices, selectedYear, selectedMonth, selectedWeek, dataSnapshot);
                                saveAveragePriceToFirebase(komoditi, String.valueOf(selectedWeek), tanggalData, averagePrice);
                                savePriceDataToFirebase(komoditi, String.valueOf(selectedWeek), selectedMonth, selectedYear, averagePrice);

                                addedKomoditi.add(komoditi); // Tandai komoditi sebagai sudah ditambahkan
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

    private void addPrice(String commodity, String week, String month, String year, double price) {
        if (!pricesMap.containsKey(commodity)) {
            pricesMap.put(commodity, new HashMap<>());
        }

        String key = week + month + year;
        if (!pricesMap.get(commodity).containsKey(key)) {
            pricesMap.get(commodity).put(key, new ArrayList<>());
        }

        pricesMap.get(commodity).get(key).add(price);
    }
    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        headerRow.setPadding(0, 16, 0, 16);

        // Add No column header
        addCellToRow(headerRow, "No", true);
        // Add Komoditi column header
        addCellToRow(headerRow, "Komoditi", true);

        // Dapatkan tanggal awal dan akhir minggu
        int selectedWeek = spinnerMinggu.getSelectedItemPosition() + 1;
        int selectedMonth = spinnerMonth.getSelectedItemPosition();
        int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
        // Dapatkan tanggal awal dan akhir minggu
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.WEEK_OF_MONTH, selectedWeek);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set ke hari Minggu
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_WEEK, 6); // Tambahkan 6 hari untuk mendapatkan hari Sabtu
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
        addCellToRow(headerRow, "Harga Minggu Lalu", true);
        // Add Harga Minggu Lalu vs Minggu Ini column header
        addCellToRow(headerRow, "Harga Minggu Lalu vs Minggu Ini", true);
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

    private void addDataToTable(String komoditi, String tanggalData, double[] prices, String selectedYear, String selectedMonth, int selectedWeek, DataSnapshot dataSnapshot) {
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

        // Tambahkan data harga ke baris yang sesuai
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.YEAR, Integer.parseInt(selectedYear));
        calStart.set(Calendar.MONTH, getMonthNumber(selectedMonth));
        calStart.set(Calendar.WEEK_OF_MONTH, selectedWeek);
        calStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to Sunday of the selected week
        Date startDate = calStart.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.YEAR, Integer.parseInt(selectedYear));
        calEnd.set(Calendar.MONTH, getMonthNumber(selectedMonth));
        calEnd.set(Calendar.WEEK_OF_MONTH, selectedWeek);
        calEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // Set to Saturday of the selected week
        Date endDate = calEnd.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        while (!startDate.after(endDate)) {
            int column = (int) ((startDate.getTime() - calStart.getTime().getTime()) / (1000 * 60 * 60 * 24)); // Calculate the column index for the date
            if (!Double.isNaN(prices[column])) {
                String tanggal = sdf.format(startDate);
                double averagePriceForDateAndCommodity = getAveragePriceForDateAndCommodity(dataSnapshot, komoditi, tanggal, selectedWeek);
                addCellToRow(rowToUpdate, String.valueOf(averagePriceForDateAndCommodity));
            } else {
                addCellToRow(rowToUpdate, "-");
            }
            calStart.add(Calendar.DAY_OF_MONTH, 1);
            startDate = calStart.getTime();
        }

        Spinner spinnerMinggu = findViewById(R.id.spinnerMinggu);
        String week = spinnerMinggu.getSelectedItem().toString();

        getAveragePrice(komoditi, week, tanggalData, averagePrice -> {
            Log.d("DEBUG", "Harga rata-rata dari " + komoditi + " pada minggu " + week + " adalah " + averagePrice);
            savePriceDataToFirebase(komoditi, week, selectedMonth, selectedYear, averagePrice);
            addCellToRow(rowToUpdate, String.valueOf(averagePrice));

            // Calculate highest price
            getHighestPrice(komoditi, week, tanggalData, highestPrice -> {
                Log.d("DEBUG", "Harga Tertinggi: " + komoditi + " pada minggu " + week + " adalah " + highestPrice);
                addCellToRow(rowToUpdate, String.valueOf(highestPrice));

                getLowestPrice(komoditi, week, tanggalData, lowestPrice -> {
                    Log.d("DEBUG", "Harga Terendah: " + komoditi + " pada minggu " + week + " adalah " + lowestPrice);
                    addCellToRow(rowToUpdate, String.valueOf(lowestPrice));

                    // Calculate last week's average price
                    double lastWeekAveragePrice = calculateLastWeekAveragePrice(komoditi, week, selectedMonth, selectedYear);
                    Log.d("DEBUG", "Rata-rata Harga Minggu Lalu: " + lastWeekAveragePrice);
                    addCellToRow(rowToUpdate, String.valueOf(lastWeekAveragePrice));

                    double priceChange = calculatePriceChange(lastWeekAveragePrice, averagePrice);
                    Log.d("DEBUG", "Perubahan Harga: " + priceChange);
                    addCellToRow(rowToUpdate, String.valueOf(priceChange));

                    String trend = calculateTrend(lastWeekAveragePrice, averagePrice);
                    Log.d("DEBUG", "Tren Harga: " + trend);
                    addCellToRow(rowToUpdate, trend);

                    if (!komoditiAlreadyExists) {
                        // Jika komoditi belum ada dalam tabel, tambahkan baris baru ke tabel
                        reportTable.addView(rowToUpdate); // Tambahkan baris baru di akhir tabel
                    }
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


    private double getAveragePriceForDateAndCommodity(DataSnapshot dataSnapshot, String komoditi, String tanggal, int selectedWeek) {
        double totalHarga = 0;
        int jumlahData = 0;

        // Loop through data to calculate total price and count
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String komoditiData = childSnapshot.child("spinnerKomoditi").getValue(String.class);
            String tanggalData = childSnapshot.child("tanggalData").getValue(String.class);
            String hargaStr = childSnapshot.child("editTextHarga").getValue(String.class);

            if (komoditiData != null && komoditiData.equals(komoditi) && tanggalData != null && tanggalData.equals(tanggal)) {
                double harga = getPrice(childSnapshot.child("editTextHarga"));
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

    private void saveAveragePriceToFirebase(String komoditi, String week, String tanggalData, double averagePrice) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices")
                .child(komoditi)
                .child(week)
                .child(tanggalData);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Jika data sudah ada, update nilai harga
                    databaseReference.child("harga").setValue(averagePrice)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Nilai rata-rata harga berhasil diperbarui di Firebase");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Firebase", "Gagal memperbarui nilai rata-rata harga di Firebase", e);
                                }
                            });
                } else {
                    // Jika data belum ada, simpan data baru
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Gagal memeriksa data di Firebase", databaseError.toException());
            }
        });
    }


    private void getAveragePrice(String komoditi, String week, String tanggalData, AveragePriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices")
                .child(komoditi)
                .child(week);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double total = 0;
                int count = 0;

                // Menghitung total harga yang ada di Firebase
                for (DataSnapshot weekSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : weekSnapshot.getChildren()) {
                        double price = dateSnapshot.getValue(Double.class);
                        total += price;
                        count++;
                        Log.d("Firebase", "Harga dari Firebase: " + price);
                    }
                }

                // Menambahkan harga baru ke total jika tanggalData belum ada di Firebase
                if (!dataSnapshot.hasChild(tanggalData)) {
                    double newPrice = 0;// Get new price from somewhere
                            total += newPrice;
                    count++;
                    Log.d("Firebase", "Harga baru: " + newPrice);
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

    private void savePriceDataToFirebase(String komoditi, String week, String month, String year, double averagePrice) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Week_average_prices")
                .child(komoditi)
                .child(year)
                .child(month)
                .child(week);

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

    interface AveragePriceCallback {
        void onAveragePriceReceived(double averagePrice);
    }

    interface HighestPriceCallback {
        void onHighestPriceReceived(double highestPrice);
    }

    private void getHighestPrice(String komoditi, String week, String tanggalData, HighestPriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices")
                .child(komoditi)
                .child(week);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double highestPrice = 0;

                // Mencari harga tertinggi dari harga yang ada di Firebase
                for (DataSnapshot weekSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : weekSnapshot.getChildren()) {
                        double price = dateSnapshot.getValue(Double.class);
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
            }
        });
    }

    private void getLowestPrice(String komoditi, String week, String tanggalData, LowestPriceCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("average_prices")
                .child(komoditi)
                .child(week);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double lowestPrice = Double.MAX_VALUE; // Initialize with a very high value

                // Mencari harga terendah dari harga yang ada di Firebase
                for (DataSnapshot weekSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : weekSnapshot.getChildren()) {
                        double price = dateSnapshot.getValue(Double.class);
                        if (price < lowestPrice) {
                            lowestPrice = price;
                        }
                        Log.d("Firebase", "Harga dari Firebase: " + price);
                    }
                }

                // Menambahkan harga baru ke total jika tanggalData belum ada di Firebase
                if (!dataSnapshot.hasChild(tanggalData)) {
                    double newPrice = 0; // Get new price from somewhere
                    if (newPrice < lowestPrice) {
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
    private double calculateLastWeekAveragePrice(String komoditi, String week, String month, String year) {
        // Mendapatkan tanggal untuk minggu sebelumnya
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
// Set nama bulan langsung
        calendar.set(Calendar.MONTH, getMonth(month));
        calendar.set(Calendar.WEEK_OF_MONTH, Integer.parseInt(week));
        calendar.add(Calendar.WEEK_OF_MONTH, -1); // Kurangi 1 minggu dari pilihan minggu
        String lastWeekDate = sdf.format(calendar.getTime());


        // Mendapatkan referensi database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Week_average_prices")
                .child(komoditi)
                .child(year)
                .child(month)
                .child(week);

        // Mendapatkan data harga untuk minggu sebelumnya
        final double[] lastWeekPrice = {0};
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lastWeekPrice[0] = dataSnapshot.getValue(Double.class);
                    // Gunakan lastWeekPrice untuk perhitungan atau tampilkan di UI sesuai kebutuhan
                    Log.d("Firebase", "Harga minggu sebelumnya: " + lastWeekPrice[0]);
                } else {
                    Log.d("Firebase", "Tidak ada data harga untuk minggu sebelumnya.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
                Log.e("Firebase", "Error retrieving last week's price: " + databaseError.getMessage());
            }
        });

        // Mengembalikan harga minggu sebelumnya
        return lastWeekPrice[0];
    }


    private double calculatePriceChange(double lastWeekAveragePrice, double averagePrice) {
        if (lastWeekAveragePrice != 0) {
            double priceChange = ((averagePrice - lastWeekAveragePrice) / averagePrice) * 100;
            return Math.round(priceChange * 100.0) / 100.0;
        } else {
            return 0;
        }
    }

    private String calculateTrend(double lastWeekAveragePrice, double averagePrice) {
        double diff = Math.abs(lastWeekAveragePrice - averagePrice);
        if (diff < TOLERANCE) {
            return "Stabil";
        } else if (lastWeekAveragePrice > averagePrice) {
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


    private boolean isDateInSelectedWeek(String tanggal, String selectedMonth, String selectedYear, int selectedWeek) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date date = dateFormat.parse(tanggal);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // Mendapatkan bulan yang sesuai dengan format yang dipilih dari spinner
            String dateMonth = new SimpleDateFormat("MMMM", new Locale("id", "ID")).format(date);

            // Mendapatkan indeks bulan dari spinner
            int monthIndex = spinnerMonth.getSelectedItemPosition();

            // Bandingkan bulan dari tanggal dengan bulan yang dipilih dari spinner
            boolean isMonthEqual = dateMonth.equalsIgnoreCase(selectedMonth) && cal.get(Calendar.MONTH) == monthIndex;
            // Bandingkan tahun dari tanggal dengan tahun yang dipilih dari spinner
            boolean isYearEqual = cal.get(Calendar.YEAR) == Integer.parseInt(selectedYear);
            // Bandingkan minggu dari tanggal dengan minggu yang dipilih dari spinner
            boolean isWeekEqual = getWeekFromDate(tanggal) == selectedWeek;

            return isMonthEqual && isYearEqual && isWeekEqual;
        } catch (ParseException e) {
            Log.e("PelaporanActivity", "Error parsing date: " + e.getMessage());
            return false;
        }
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

    private void populateWeekSpinner() {
        List<String> weekList = new ArrayList<>();

        // Mendapatkan jumlah minggu dalam bulan yang dipilih
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, spinnerMonth.getSelectedItemPosition());
        calendar.set(Calendar.YEAR, Integer.parseInt(spinnerYear.getSelectedItem().toString()));
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // Set hari pertama dalam seminggu sebagai Senin
        int maxWeeksInMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);

        // Menambahkan minggu ke daftar minggu
        for (int i = 1; i <= maxWeeksInMonth; i++) {
            weekList.add(String.valueOf(i));
        }

        // Set adapter untuk spinner minggu
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weekList);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinggu.setAdapter(weekAdapter);
    }

    // Metode untuk mengonversi tanggal ke minggu
    private int getWeekFromDate(String tanggal) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date date = dateFormat.parse(tanggal);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.setFirstDayOfWeek(Calendar.MONDAY); // Set hari pertama dalam seminggu sebagai Senin
            return calendar.get(Calendar.WEEK_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void saveTableToPdf() {
        String selectedWeek = spinnerMinggu.getSelectedItem().toString();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String selectedYear = spinnerYear.getSelectedItem().toString();

        String fileName = "laporanmingguan_" + selectedWeek + "_" + selectedMonth + "_" + selectedYear + ".pdf";
        String filePath = getFilesDir() + File.separator + fileName;
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.setPageSize(PageSize.A4.rotate()); // Set tampilan ke landscape
            document.open();

            // Tambahkan Judul ke PDF
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
            String titleText = "Laporan Minggu " + selectedWeek + " " + selectedMonth + " " + selectedYear;
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Tambahkan Informasi Minggu, Bulan, dan Tahun ke PDF
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            String info = "Minggu: " + spinnerMinggu.getSelectedItem() + ", Bulan: " + spinnerMonth.getSelectedItem() + ", Tahun: " + spinnerYear.getSelectedItem();
            Paragraph infoParagraph = new Paragraph(info, infoFont);
            infoParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(infoParagraph);

            // Tambahkan Tabel ke PDF
            PdfPTable pdfPTable = new PdfPTable(10); // Jumlah kolom sesuaikan dengan jumlah kolom di tabel Anda
            pdfPTable.setWidthPercentage(100); // Menetapkan lebar tabel sebagai 100% dari lebar halaman

            // Add No column header
            PdfPCell noHeaderCell = new PdfPCell(new Phrase("No"));
            noHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            noHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(noHeaderCell);

            // Add Komoditi column header
            PdfPCell komoditiHeaderCell = new PdfPCell(new Phrase("Komoditi"));
            komoditiHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            komoditiHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            komoditiHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(komoditiHeaderCell);

            // Add tanggal columns headers
            for (int i = 0; i < 7; i++) {
                PdfPCell dateHeaderCell = new PdfPCell(new Phrase("Tanggal")); // Ganti "Tanggal" dengan tanggal yang sesuai
                dateHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dateHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                dateHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
                pdfPTable.addCell(dateHeaderCell);
            }

            // Add Harga Rata-rata column header
            PdfPCell avgPriceHeaderCell = new PdfPCell(new Phrase("Harga Rata-rata"));
            avgPriceHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            avgPriceHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            avgPriceHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(avgPriceHeaderCell);

            // Add Harga Tertinggi column header
            PdfPCell maxPriceHeaderCell = new PdfPCell(new Phrase("Harga Tertinggi"));
            maxPriceHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            maxPriceHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            maxPriceHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(maxPriceHeaderCell);

            // Add Harga Terendah column header
            PdfPCell minPriceHeaderCell = new PdfPCell(new Phrase("Harga Terendah"));
            minPriceHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            minPriceHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            minPriceHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(minPriceHeaderCell);

            // Add Harga Minggu Lalu column header
            PdfPCell lastWeekPriceHeaderCell = new PdfPCell(new Phrase("Harga Minggu Lalu"));
            lastWeekPriceHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            lastWeekPriceHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            lastWeekPriceHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(lastWeekPriceHeaderCell);

            // Add Harga Minggu Lalu vs Minggu Ini column header
            PdfPCell priceChangeHeaderCell = new PdfPCell(new Phrase("Harga Minggu Lalu vs Minggu Ini"));
            priceChangeHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            priceChangeHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            priceChangeHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(priceChangeHeaderCell);

            // Add Ket column header
            PdfPCell noteHeaderCell = new PdfPCell(new Phrase("Ket"));
            noteHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            noteHeaderCell.setBackgroundColor(BaseColor.GRAY); // Warna latar belakang header
            pdfPTable.addCell(noteHeaderCell);

            // Menambahkan data dari tabel Anda
            for (int i = 0; i < reportTable.getChildCount(); i++) {
                TableRow row = (TableRow) reportTable.getChildAt(i);

                // Misalnya, Anda memiliki dua TextView dalam setiap baris
                TextView textView1 = (TextView) row.getChildAt(0);
                PdfPCell cell1 = new PdfPCell(new Phrase(textView1.getText().toString()));
                cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPTable.addCell(cell1);

                TextView textView2 = (TextView) row.getChildAt(1);
                PdfPCell cell2 = new PdfPCell(new Phrase(textView2.getText().toString()));
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPTable.addCell(cell2);
            }

            document.add(pdfPTable);

            document.close();

            // Bagikan PDF ke aplikasi lain
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(this, "com.pangan.kotatomohon.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "Bagikan Laporan Mingguan"));
        } catch (Exception e) {
            e.printStackTrace();
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


