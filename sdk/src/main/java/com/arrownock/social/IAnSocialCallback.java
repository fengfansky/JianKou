package com.arrownock.social;

import org.json.JSONObject;

public interface IAnSocialCallback {
    public void onSuccess(JSONObject response);

    public void onFailure(JSONObject response);
}
