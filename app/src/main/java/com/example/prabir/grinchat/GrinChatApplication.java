package com.example.prabir.grinchat;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by prabir on 1/6/15.
 */
public class GrinChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "aIInPaxFpVJSAEpGVe6lxfAmG9ZrHJeto81ambS1", "VPR0n1c49XUfqIOqQUu8hy1KG7tiakBrCoWs8sYz");

    }
}
