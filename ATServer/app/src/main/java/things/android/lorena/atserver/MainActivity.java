package things.android.lorena.atserver;

import android.app.Activity;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import java.nio.ByteBuffer;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private CameraDeviceManager mCamera;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private ImageHolder mImageHolder;
    private Thread imageCaptureThread;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraThread = new HandlerThread("CameraBackgroundThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        mCamera = CameraDeviceManager.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
        mImageHolder = ImageHolder.getInstance();
        imageCaptureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                captureImageLoop();
            }
        });
        imageCaptureThread.start();
        ImageService.startService(this);
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    mImageHolder.setImage(imageBytes);
                    image.close();
                }
            };

    private void captureImageLoop() {
        while(true) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCamera.takePicture();
                }
            });

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCaptureThread.interrupt();
        mCamera.shutDown();
        mCameraThread.quitSafely();
        ImageService.stopService(this);
    }
}
