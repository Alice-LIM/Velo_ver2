package seoul.iot.biketrainner;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by AliceLim on 2017. 11. 13..
 */

public class StartApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "fonts/nanumbarungothic.ttf"))
                .addBold(Typekit.createFromAsset(this, "fonts/nanumbarungothic.ttf"));
    }
}
