import sys
p = "android/app/src/main/AndroidManifest.xml"
s = open(p, encoding="utf-8").read()
svc = '''
        <service
            android:name=".PapyrusKeyService"
            android:exported="false"
            android:label="@string/app_name"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
'''
if "PapyrusKeyService" in s:
    sys.exit(0)
assert "</application>" in s, "manifest has no </application>"
open(p, "w", encoding="utf-8").write(s.replace("</application>", svc + "    </application>", 1))
print("manifest patched")
