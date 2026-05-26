package com.yoyo.vocab;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean ttsFailed = false;

    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTTS();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setTextZoom(100);

        webView.addJavascriptInterface(new AndroidTTSBridge(), "AndroidTTS");
        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }

                filePathCallback = callback;

                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }

                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void initTTS() {
        ttsFailed = false;

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {

                try {
                    tts.setLanguage(Locale.UK);
                } catch (Exception ignored) {}

                try {
                    tts.setSpeechRate(0.85f);
                    tts.setPitch(1.0f);
                } catch (Exception ignored) {}

                ttsReady = true;

            } else {
                ttsReady = false;
                ttsFailed = true;
            }
        });
    }

    private void speakInternal(String text, String kind) {

        if (text == null) return;

        String raw = text.trim();

        if (raw.isEmpty()) return;

        if (tts == null) {
            initTTS();
            return;
        }

        if (!ttsReady) {
            return;
        }

        try {

            tts.stop();

            if ("sentence".equals(kind)) {
                tts.setSpeechRate(0.92f);
            } else {
                tts.setSpeechRate(0.85f);
            }

            String speakText = raw;

            if ("word".equals(kind) && !speakText.matches(".*[.!?。？！]$")) {
                speakText = speakText + ".";
            }

            tts.speak(
                    speakText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "tts_" + System.currentTimeMillis()
            );

        } catch (Exception e) {
            ttsFailed = true;
        }
    }

    public class AndroidTTSBridge {

        @JavascriptInterface
        public void speak(String text, String kind) {
            runOnUiThread(() -> {
                speakInternal(text, kind == null ? "word" : kind);
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> {
                try {
                    if (tts != null) tts.stop();
                } catch (Exception ignored) {}
            });
        }

        @JavascriptInterface
        public void test() {
            runOnUiThread(() -> {
                speakInternal("hello", "word");
            });
        }

        @JavascriptInterface
        public boolean isReady() {
            return ttsReady;
        }

        @JavascriptInterface
        public boolean hasFailed() {
            return ttsFailed;
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {

        try {
            if (tts != null) tts.stop();
        } catch (Exception ignored) {}

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        try {

            if (webView != null) {
                webView.destroy();
            }

            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }

        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        if (requestCode == FILE_CHOOSER_REQUEST) {

            if (filePathCallback == null) return;

            Uri[] results = null;

            if (
                    resultCode == Activity.RESULT_OK &&
                    data != null &&
                    data.getData() != null
            ) {
                results = new Uri[]{data.getData()};
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;

        } else {

            super.onActivityResult(
                    requestCode,
                    resultCode,
                    data
            );
        }
    }
}
