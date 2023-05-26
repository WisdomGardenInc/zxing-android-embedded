package com.wisdomgarden.barcodescanner.camera;

public class CameraZoomConfig {
    private int maxZoom = 0;

    private int zoomStep = 3;
    private boolean zoomSupported = true;

    public int getMaxZoom() {
        return this.maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getZoomStep() {
        return this.zoomStep;
    }

    public void setZoomStep(int zoomStep) {
        this.zoomStep = zoomStep;
    }

    public boolean getZoomSupported() {
        return this.zoomSupported;
    }

    public void setZoomSupported(boolean zoomSupported) {
        this.zoomSupported = zoomSupported;
    }

}
