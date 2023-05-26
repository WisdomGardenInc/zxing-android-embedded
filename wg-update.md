# v1.0.0
feat:
1. Scan code to support zoom

    ```
    mBarcodeView.setOnScaleGestureListener(View view);
    ```

# v1.1.0
feat:
1. Scan code add manual focus

    ```
    mBarcodeView.manualFocus()
    ```
# v1.2.0
feat: 
1. Add settings for camera zoom config(This has no effect if the camera is already open.)
* Set all config
    ```
    mBarcodeView.setCameraSettings(CameraSettings cameraSettings)
    ```
* Set camera zoom config separately
  ```
  1. set zoom step
  mBarcodeView.setCameraZoomStep(int step)

  2. set zoom supported
  mBarcodeView.setCameraZoomSupported(boolean supported)

  3. set max zoom
  mBarcodeView.setCameraMaxZoom(int maxZoom)
  ```