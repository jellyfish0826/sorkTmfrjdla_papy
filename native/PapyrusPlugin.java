package com.haepari.papyrustyping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "PapyrusNative")
public class PapyrusPlugin extends Plugin {

    private SharedPreferences prefs() {
        return getContext().getSharedPreferences("papyrus", Context.MODE_PRIVATE);
    }

    @PluginMethod
    public void isEnabled(PluginCall call) {
        JSObject r = new JSObject();
        r.put("enabled", PapyrusKeyService.isEnabled(getContext()));
        call.resolve(r);
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        JSObject r = new JSObject();
        r.put("enabled", PapyrusKeyService.isEnabled(getContext()));
        r.put("connected", PapyrusKeyService.connected);
        r.put("count", PapyrusKeyService.keyCount);
        call.resolve(r);
    }

    @PluginMethod
    public void openAppInfo(PluginCall call) {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + getContext().getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }

    @PluginMethod
    public void openSettings(PluginCall call) {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }

    @PluginMethod
    public void setVolume(PluginCall call) {
        Float v = call.getFloat("value", 0.8f);
        prefs().edit().putFloat("vol", v == null ? 0.8f : v).apply();
        call.resolve();
    }

    @PluginMethod
    public void setSpace(PluginCall call) {
        Boolean b = call.getBoolean("value", false);
        prefs().edit().putBoolean("space", b != null && b).apply();
        call.resolve();
    }
}
