package com.pangan.kotatomohon;

public class StokInfo {
    private String komoditi;
    private int stok;
    private String tanggal;

    public StokInfo(String komoditi, int stok, String tanggal) {
        this.komoditi = komoditi;
        this.stok = stok;
        this.tanggal = tanggal;
    }

    public String getKomoditi() {
        return komoditi;
    }

    public int getStok() {
        return stok;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
