package com.pangan.kotatomohon;

public class LocationData {
    private String locationName;
    private double latitude;
    private double longitude;
    private int stokPangan;
    private String tanggal;
    public LocationData(String locationName, double latitude, double longitude, int stokPangan, String tanggal) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stokPangan = stokPangan;
        this.tanggal = tanggal;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getStokPangan() {
        return stokPangan;
    }
    public String getTanggal() {
        return tanggal;
    }

    // Tambahkan setter untuk tanggal jika diperlukan
    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
