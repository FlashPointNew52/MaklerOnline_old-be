package utility;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

/**
 * Created by owl on 3/25/16.
 */
public class JsonTrans implements ResponseTransformer {

    Gson gson = new GsonBuilder().create();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

}
