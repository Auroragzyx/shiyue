package com.shiyue.app.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoItem {
    public String dataUrl;
    public String caption;

    public PhotoItem(String dataUrl, String caption) {
        this.dataUrl = dataUrl;
        this.caption = caption != null ? caption : "";
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("dataUrl", dataUrl);
            obj.put("caption", caption);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static PhotoItem fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        String data = obj.optString("dataUrl", "");
        String cap = obj.optString("caption", "");
        return new PhotoItem(data, cap);
    }
}
