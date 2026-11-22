package com.novanexus.futbolnova;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public final class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.useWakelock = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        initialize(new FutbolNovaGame(), configuration);
    }
}
