package com.pangan.kotatomohon;

public class LokasiPangan {
    private String namaLokasi;
    // tambahkan atribut lain jika diperlukan

    public LokasiPangan() {
        // Diperlukan untuk deserialization
    }

    public LokasiPangan(String namaLokasi) {
        this.namaLokasi = namaLokasi;
    }

    public String getNamaLokasi() {
        return namaLokasi;
    }
}
