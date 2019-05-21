package things.android.lorena.atserver;

public class ImageHolder {
    private static ImageHolder INSTANCE;
    private byte[] imageBytes;

    public static synchronized ImageHolder getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ImageHolder();
        }

        return INSTANCE;
    }

    public synchronized void setImage(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public synchronized byte[] getImage() {
        return this.imageBytes;
    }
}
