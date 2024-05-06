package com.pangan.kotatomohon.ui.home.entry;

import android.widget.Button;

public class ModelEntry {
    private String spinnerKomoditi;
    private String editTextJumlahPasokan;
    private String editTextAsalPasokan;
    private String editTextHargaPasokan;
    private String editTextStok;
    private String editTextHarga;
    private String spinnerKet;
    private Button buttonSimpan;
    private String tanggalData;

    public ModelEntry(String spinnerKomoditi, String editTextJumlahPasokan, String editTextAsalPasokan, String editTextHargaPasokan, String editTextStok, String editTextHarga, String spinnerKet, String tanggalData) {
        this.spinnerKomoditi = spinnerKomoditi;
        this.editTextJumlahPasokan = editTextJumlahPasokan;
        this.editTextAsalPasokan = editTextAsalPasokan;
        this.editTextHargaPasokan = editTextHargaPasokan;
        this.editTextStok = editTextStok;
        this.editTextHarga = editTextHarga;
        this.spinnerKet = spinnerKet;
        this.tanggalData = tanggalData;
    }

    public String getSpinnerKomoditi() {
        return spinnerKomoditi;
    }

    public void setSpinnerKomoditi(String spinnerKomoditi) {
        this.spinnerKomoditi = spinnerKomoditi;
    }

    public String getEditTextJumlahPasokan() {
        return editTextJumlahPasokan;
    }

    public void setEditTextJumlahPasokan(String editTextJumlahPasokan) {
        this.editTextJumlahPasokan = editTextJumlahPasokan;
    }

    public String getEditTextAsalPasokan() {
        return editTextAsalPasokan;
    }

    public void setEditTextAsalPasokan(String editTextAsalPasokan) {
        this.editTextAsalPasokan = editTextAsalPasokan;
    }

    public String getEditTextHargaPasokan() {
        return editTextHargaPasokan;
    }

    public void setEditTextHargaPasokan(String editTextHargaPasokan) {
        this.editTextHargaPasokan = editTextHargaPasokan;
    }

    public String getEditTextStok() {
        return editTextStok;
    }

    public void setEditTextStok(String editTextStok) {
        this.editTextStok = editTextStok;
    }

    public String getEditTextHarga() {
        return editTextHarga;
    }

    public void setEditTextHarga(String editTextHarga) {
        this.editTextHarga = editTextHarga;
    }

    public String getSpinnerKet() {
        return spinnerKet;
    }

    public void setSpinnerKet(String spinnerKet) {
        this.spinnerKet = spinnerKet;
    }

    public Button getButtonSimpan() {
        return buttonSimpan;
    }

    public void setButtonSimpan(Button buttonSimpan) {
        this.buttonSimpan = buttonSimpan;
    }

    public String getTanggalData() {
        return tanggalData;
    }

    public void setTanggalData(String tanggalData) {
        this.tanggalData = tanggalData;
    }
}
