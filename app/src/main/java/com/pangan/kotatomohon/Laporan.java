package com.pangan.kotatomohon;

public class Laporan {
    private String tanggal;
    private String komoditi;
    private String jumlah;
    private String asal;
    private String hargaPasokan;
    private String stok;
    private String harga;
    private String keterangan;

    public Laporan() {
        // Diperlukan konstruktor kosong untuk Firebase Realtime Database.
    }
    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getKomoditi() {
        return komoditi;
    }

    public void setKomoditi(String komoditi) {
        this.komoditi = komoditi;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getAsal() {
        return asal;
    }

    public void setAsal(String asal) {
        this.asal = asal;
    }

    public String getHargaPasokan() {
        return hargaPasokan;
    }

    public void setHargaPasokan(String hargaPasokan) {
        this.hargaPasokan = hargaPasokan;
    }

    public String getStok() {
        return stok;
    }

    public void setStok(String stok) {
        this.stok = stok;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    // Buat constructor, getter, dan setter sesuai kebutuhan
}
