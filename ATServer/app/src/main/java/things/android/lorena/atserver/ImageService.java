package things.android.lorena.atserver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.nio.HttpServerHelper;
import org.restlet.routing.Router;

/**
 reference: https://dzone.com/articles/restful-api-interface-using-android-things
 * */
public class ImageService extends IntentService {
    private static final String ACTION_START = "things.android.lorena.atserver.action.START";
    private static final String ACTION_STOP = "things.android.lorena.atserver.action.STOP";
    private Component mComponent;

    public ImageService() {
        super("ImageService");
        Engine.getInstance().getRegisteredServers().clear();
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        mComponent = new Component();
        mComponent.getServers().add(Protocol.HTTP, 6000);
        Router router = new Router(mComponent.getContext().createChildContext());
        router.attach("/camera", CameraResource.class);
        mComponent.getDefaultHost().attach("/api", router);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, ImageService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, ImageService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                try {
                    mComponent.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ACTION_STOP.equals(action)) {
                try {
                    mComponent.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
