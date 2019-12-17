package com.android.example.gawala.Transporter.Interfaces;

public interface RequestsAdapterCallbacks {
    void onRequestCancel(int position);
    void onRequestAccepted(int position);

    void onRequestClientClicked(int position);
}
