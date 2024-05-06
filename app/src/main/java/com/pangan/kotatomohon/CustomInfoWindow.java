package com.pangan.kotatomohon;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomInfoWindow extends InfoWindow {

    private String customText;

    public CustomInfoWindow(int layoutResId, MapView mapView, Context context, String customText) {
        super(layoutResId, mapView);
        this.customText = customText;
    }

    @Override
    public void onOpen(Object item) {
        Log.d("CustomInfoWindow", "InfoWindow dibuka untuk item: " + item.toString());

        // Atur teks kustom berdasarkan item (Marker)
        if (item instanceof Marker) {
            Marker marker = (Marker) item;
            String markerInfoText = getMarkerInfoText(marker);
            setInfoWindowText(markerInfoText);
        }
    }

    @Override
    public void onClose() {
        // Tindakan tambahan saat InfoWindow ditutup
    }

    // Metode untuk mengatur teks kustom secara dinamis
    public void setInfoWindowText(String infoWindowText) {
        TextView textView = mView.findViewById(R.id.custom_info_window_text);
        if (textView != null) {
            textView.setText(infoWindowText);
        }
    }

    // Sesuaikan metode ini untuk memformat informasi sesuai kebutuhan Anda
    private String getMarkerInfoText(Marker marker) {
        // Ekstrak informasi dari Marker dan format sesuai kebutuhan
        // Contoh: return "Lintang: " + marker.getPosition().getLatitude() + ", Bujur: " + marker.getPosition().getLongitude();
        return customText;  // Anda dapat menyesuaikan ini berdasarkan data Anda
    }
}
