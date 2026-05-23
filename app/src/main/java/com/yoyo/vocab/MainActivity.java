package com.yoyo.vocab;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech tts;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.82f);
                tts.setPitch(1.0f);
            }
        });

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
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
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

    public class AndroidTTSBridge {
        @JavascriptInterface
        public void speak(String text, String kind) {
            if (text == null || text.trim().isEmpty()) return;
            runOnUiThread(() -> {
                if (tts != null) {
                    tts.stop();
                    tts.setSpeechRate("sentence".equals(kind) ? 0.90f : 0.82f);
                    String s = text.trim();
                    if ("word".equals(kind) && !s.matches(".*[.!?。？！]$")) s = s + ".";
                    tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "vocab_tts");
                }
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> { if (tts != null) tts.stop(); });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (tts != null) tts.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
