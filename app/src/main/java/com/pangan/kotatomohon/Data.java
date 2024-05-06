package com.pangan.kotatomohon;

public class Data {
    private String dataId;
    private String tanggal;
    private String komoditi;
    private String jumlah;
    private String asal;
    private String hargapasokan;
    private String stok;
    private String harga;
    private String ket;

    public Data() {
        // Diperlukan konstruktor kosong untuk Firebase Realtime Database.
    }

    public Data(String dataId, String tanggal, String komoditi, String jumlah, String asal, String hargapasokan, String stok, String harga, String ket) {
        this.dataId = dataId;
        this.tanggal = tanggal;
        this.komoditi = komoditi;
        this.jumlah = jumlah;
        this.asal = asal;
        this.hargapasokan = hargapasokan;
        this.stok = stok;
        this.harga = harga;
        this.ket = ket;
    }
    public String getTanggal() {
        return tanggal;
    }

    public String getKomoditi() {
        return komoditi;
    }

    public String getJumlah() {
        return jumlah;
    }

    public String getAsal() {
        return asal;
    }

    public String getHargapasokan() {
        return hargapasokan;
    }

    public String getStok() {
        return stok;
    }

    public String getHarga() {
        return harga;
    }

    public String getKet() {
        return ket;
    }

}

