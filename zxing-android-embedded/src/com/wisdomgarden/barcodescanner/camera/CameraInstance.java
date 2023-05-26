package com.wisdomgarden.barcodescanner.camera;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.client.android.R;
import com.wisdomgarden.barcodescanner.Size;
import com.wisdomgarden.barcodescanner.Util;

/**
 * Manage a camera instance using a background thread.
 * <p>
 * All methods must be called from the main thread.
 */
public class CameraInstance {
    private static final String TAG = CameraInstance.class.getSimpleName();

    private CameraThread cameraThread;
    private CameraSurface surface;

    private CameraManager cameraManager;

    private CameraZoomConfig cameraZoomConfig;

    private Handler readyHandler;
    private DisplayConfiguration displayConfiguration;
    private boolean open = false;
    private boolean cameraClosed = true;
    private Handler mainHandler;

    private CameraSettings cameraSettings = new CameraSettings();

    /**
     * Construct a new CameraInstance.
     * <p>
     * A new CameraManager is created.
     *
     * @param context the Android Context
     */
    public CameraInstance(Context context) {
        Util.validateMainThread();

        this.cameraThread = CameraThread.getInstance();
        this.cameraManager = new CameraManager(context);
        this.cameraManager.setCameraSettings(cameraSettings);
        this.mainHandler = new Handler();
        this.cameraZoomConfig = new CameraZoomConfig();

    }

    /**
     * Construct a new CameraInstance with a specific CameraManager.
     *
     * @param cameraManager the CameraManager to use
     */
    public CameraInstance(CameraManager cameraManager) {
        Util.validateMainThread();

        this.cameraManager = cameraManager;
        this.cameraZoomConfig = new CameraZoomConfig();
    }

    public void setDisplayConfiguration(DisplayConfiguration configuration) {
        this.displayConfiguration = configuration;
        cameraManager.setDisplayConfiguration(configuration);
    }

    public DisplayConfiguration getDisplayConfiguration() {
        return displayConfiguration;
    }

    public void setReadyHandler(Handler readyHandler) {
        this.readyHandler = readyHandler;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        setSurface(new CameraSurface(surfaceHolder));
    }

    public void setSurface(CameraSurface surface) {
        this.surface = surface;
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    /**
     * This only has an effect if the camera is not opened yet.
     *
     * @param cameraSettings the new camera settings
     */
    public void setCameraSettings(CameraSettings cameraSettings) {
        if (!open) {
            this.cameraSettings = cameraSettings;
            this.cameraManager.setCameraSettings(cameraSettings);
        }
    }

    /**
     * This only has an effect if the camera is not opened yet.
     * set camera zoom config
     *
     * @param cameraZoomConfig
     */
    public void setCameraZoomConfig(CameraZoomConfig cameraZoomConfig) {
        if (!open) {
            this.cameraZoomConfig = cameraZoomConfig;
        }
    }

    /**
     * Actual preview size in current rotation. null if not determined yet.
     *
     * @return preview size
     */
    private Size getPreviewSize() {
        return cameraManager.getPreviewSize();
    }

    /**
     * @return the camera rotation relative to display rotation, in degrees. Typically 0 if the
     * display is in landscape orientation.
     */
    public int getCameraRotation() {
        return cameraManager.getCameraRotation();
    }

    public void open() {
        Util.validateMainThread();

        open = true;
        cameraClosed = false;

        cameraThread.incrementAndEnqueue(opener);
    }

    public void configureCamera() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(configure);
    }

    public void startPreview() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(previewStarter);
    }

    public void setTorch(final boolean on) {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(() -> cameraManager.setTorch(on));
        }
    }

    /**
     * Changes the settings for Camera.
     *
     * @param callback {@link CameraParametersCallback}
     */
    public void changeCameraParameters(final CameraParametersCallback callback) {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(() -> cameraManager.changeCameraParameters(callback));
        }
    }

    public void close() {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(closer);
        } else {
            cameraClosed = true;
        }

        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isCameraClosed() {
        return cameraClosed;
    }

    public void requestPreview(final PreviewCallback callback) {
        mainHandler.post(() -> {
            if (!open) {
                Log.d(TAG, "Camera is closed, not requesting preview");
                return;
            }

            cameraThread.enqueue(() -> cameraManager.requestPreviewFrame(callback));
        });
    }

    private void validateOpen() {
        if (!open) {
            throw new IllegalStateException("CameraInstance is not open");
        }
    }

    private Runnable opener = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Opening camera");
                cameraManager.open();
            } catch (Exception e) {
                notifyError(e);
                Log.e(TAG, "Failed to open camera", e);
            }
        }
    };

    private Runnable configure = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Configuring camera");
                cameraManager.configure();
                if (readyHandler != null) {
                    readyHandler.obtainMessage(R.id.zxing_prewiew_size_ready, getPreviewSize()).sendToTarget();
                }
            } catch (Exception e) {
                notifyError(e);
                Log.e(TAG, "Failed to configure camera", e);
            }
        }
    };

    private Runnable previewStarter = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Starting preview");
                cameraManager.setPreviewDisplay(surface);
                cameraManager.startPreview();
            } catch (Exception e) {
                notifyError(e);
                Log.e(TAG, "Failed to start preview", e);
            }
        }
    };

    private Runnable closer = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Closing camera");
                cameraManager.stopPreview();
                cameraManager.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close camera", e);
            }

            cameraClosed = true;

            readyHandler.sendEmptyMessage(R.id.zxing_camera_closed);

            cameraThread.decrementInstances();
        }
    };

    private void notifyError(Exception error) {
        if (readyHandler != null) {
            readyHandler.obtainMessage(R.id.zxing_camera_error, error).sendToTarget();
        }
    }

    /**
     * Returns the CameraManager used to control the camera.
     * <p>
     * The CameraManager is not thread-safe, and must only be used from the CameraThread.
     *
     * @return the CameraManager used
     */
    protected CameraManager getCameraManager() {
        return cameraManager;
    }

    /**
     * @return the CameraThread used to manage the camera
     */
    protected CameraThread getCameraThread() {
        return cameraThread;
    }

    /**
     * @return the surface om which the preview is displayed
     */
    protected CameraSurface getSurface() {
        return surface;
    }

    public boolean getZoomSupported() {
        return cameraZoomConfig.getZoomSupported() && cameraManager.getIsZoomSupported();
    }

    public int getMaxZoom() {
        int finalMaxZoom = cameraZoomConfig.getMaxZoom();
        int defaultMaxZoom = cameraManager.getMaxZoom() / 2;
        if (finalMaxZoom < 1) {
            return defaultMaxZoom;
        }
        return Math.min(defaultMaxZoom, finalMaxZoom);
    }

    public int getZoomStep() {
        int step = cameraZoomConfig.getZoomStep();
        int defaultZoomStep = this.getMaxZoom() / 4;

        if (step < 1) {
            return defaultZoomStep;
        }

        return step;
    }

    /**
     * Change the zoom of Camera
     *
     * @param isZoomIn is zoom in
     */
    public void zoomCamera(boolean isZoomIn) {
        if (!getZoomSupported()) {
            return;
        }
        Util.validateMainThread();
        if (!open) {
            return;
        }

        int maxZoom = this.getMaxZoom();
        int zoomStep = getZoomStep();
        int curZoom = cameraManager.getZoom();
        Log.i(TAG, "[zoomCamera:zoomStep]" + zoomStep);
        Log.i(TAG, "[zoomCamera:maxZoom]" + maxZoom);

        if (isZoomIn) {
            if (curZoom < maxZoom) {
                curZoom += zoomStep;
            }
        } else {
            if (curZoom > 0) {
                curZoom -= zoomStep;
            }
        }

        if (curZoom > maxZoom) {
            curZoom = maxZoom;
        }
        if (curZoom < 0) {
            curZoom = 0;
        }

        if (curZoom != cameraManager.getZoom()) {
            final int zoomLevel = curZoom;
            cameraThread.enqueue(() -> cameraManager.setZoom(zoomLevel));
        }
    }

    public void manualFocus() {
        cameraManager.focus();
    }
}
