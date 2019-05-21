package things.android.lorena.atserver;

import android.util.Log;

import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.Base64;
/**
reference: https://dzone.com/articles/restful-api-interface-using-android-things
* */
public class CameraResource extends ServerResource {
    @Get("json")
    public Representation getImageResource() {
        JSONObject result = new JSONObject();

        try {
            byte[] image = ImageHolder.getInstance().getImage();

            if(image != null) {
                String encoded = Base64.getEncoder().encodeToString(image);
                result.put("image", encoded);
            }
            else {
                result.put("error", "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StringRepresentation(result.toString(), MediaType.APPLICATION_ALL_JSON);
    }
}
