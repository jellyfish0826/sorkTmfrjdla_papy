package com.haepari.papyrustyping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 하드웨어(블루투스) 키보드의 키 입력을 받아 효과음만 재생한다.
 * 입력 내용은 저장하지 않고 어디로도 보내지 않으며, 키 이벤트는 소비하지 않는다(그대로 앱에 전달됨).
 */
public class PapyrusKeyService extends AccessibilityService {

    private static final String[] NAMES = {
        "arrow", "back", "changemode", "enter", "esc", "function",
        "menu", "modifier", "papyrus", "printscreen", "select"
    };
    private static final Set<String> LONG_SOUNDS = new HashSet<>(Arrays.asList(
        "changemode", "esc", "function", "menu", "modifier", "printscreen"
    ));

    private SoundPool pool;
    private final HashMap<String, Integer> ids = new HashMap<>();
    private final HashMap<String, Integer> streams = new HashMap<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
            setServiceInfo(info);
        }
        loadSounds();
    }

    private void loadSounds() {
        if (pool != null) return;
        AudioAttributes aa = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        pool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(aa).build();
        for (String n : NAMES) {
            int res = getResources().getIdentifier(n, "raw", getPackageName());
            if (res != 0) ids.put(n, pool.load(this, res, 1));
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        if (e.getAction() != KeyEvent.ACTION_DOWN || e.getRepeatCount() > 0) return false;
        if (pool == null) loadSounds();
        String name = soundFor(e);
        if (name != null) play(name);
        return false; // 키 입력은 그대로 통과시킴
    }

    private String soundFor(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.KEYCODE_ESCAPE: return "esc";
            case KeyEvent.KEYCODE_DEL: return "back";
            case KeyEvent.KEYCODE_TAB: return "select";
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return "enter";
            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT: return "modifier";
            case KeyEvent.KEYCODE_CAPS_LOCK:
            case KeyEvent.KEYCODE_NUM_LOCK:
            case KeyEvent.KEYCODE_SCROLL_LOCK: return "changemode";
            case KeyEvent.KEYCODE_SYSRQ: return "printscreen";
            case KeyEvent.KEYCODE_BREAK: return "function";
            case KeyEvent.KEYCODE_MOVE_HOME:
            case KeyEvent.KEYCODE_MOVE_END:
            case KeyEvent.KEYCODE_PAGE_UP:
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_INSERT:
            case KeyEvent.KEYCODE_FORWARD_DEL: return "select";
            case KeyEvent.KEYCODE_META_LEFT: return "function";
            case KeyEvent.KEYCODE_META_RIGHT:
            case KeyEvent.KEYCODE_MENU: return "menu";
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "arrow";
            case KeyEvent.KEYCODE_SPACE:
                return prefs().getBoolean("space", false) ? "papyrus" : null;
            default:
                break;
        }
        if (k >= KeyEvent.KEYCODE_F1 && k <= KeyEvent.KEYCODE_F12) return "function";
        if (e.isPrintingKey() || e.getUnicodeChar() != 0) return "papyrus";
        return null;
    }

    private SharedPreferences prefs() {
        return getSharedPreferences("papyrus", Context.MODE_PRIVATE);
    }

    private void play(String name) {
        Integer id = ids.get(name);
        if (pool == null || id == null) return;
        if (LONG_SOUNDS.contains(name)) {
            Integer old = streams.get(name);
            if (old != null) pool.stop(old);
        }
        float v = prefs().getFloat("vol", 0.8f);
        int sid = pool.play(id, v, v, 1, 0, 1f);
        if (LONG_SOUNDS.contains(name)) streams.put(name, sid);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }

    @Override
    public boolean onUnbind(Intent intent) {
        release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }

    private void release() {
        if (pool != null) {
            pool.release();
            pool = null;
        }
        ids.clear();
        streams.clear();
    }

    public static boolean isEnabled(Context c) {
        String s = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (s == null || s.isEmpty()) return false;
        ComponentName cn = new ComponentName(c, PapyrusKeyService.class);
        String full = cn.flattenToString();
        String shortName = cn.flattenToShortString();
        for (String part : s.split(":")) {
            if (part.equalsIgnoreCase(full) || part.equalsIgnoreCase(shortName)) return true;
        }
        return false;
    }
}
