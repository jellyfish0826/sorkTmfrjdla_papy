package com.haepari.papyrustyping;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(PapyrusPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
