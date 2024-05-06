package com.pangan.kotatomohon;

public class DataHarga {
    private String komoditi;
    private String harga;

    public DataHarga() {
        // Diperlukan konstruktor kosong untuk Firebase
    }

    public DataHarga(String komoditi, String harga) {
        this.komoditi = komoditi;
        this.harga = harga;
    }

    public String getKomoditi() {
        return komoditi;
    }

    public String getHarga() {
        return harga;
    }
}
