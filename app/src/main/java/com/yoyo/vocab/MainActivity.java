package com.yoyo.vocab;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingText = null;
    private String pendingKind = "word";
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setupVoice();
                ttsReady = true;
                if (pendingText != null) {
                    String t = pendingText;
                    String k = pendingKind;
                    pendingText = null;
                    handler.postDelayed(() -> speakNow(t, k), 300);
                }
            } else {
                Toast.makeText(this, "语音引擎初始化失败，请检查系统文字转语音设置。", Toast.LENGTH_LONG).show();
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
                try { startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST); }
                catch (Exception e) { filePathCallback = null; return false; }
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void setupVoice() {
        if (tts == null) return;
        int r = tts.setLanguage(Locale.US);
        try {
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                for (Voice v : voices) {
                    Locale l = v.getLocale();
                    if (l != null && "en".equalsIgnoreCase(l.getLanguage()) && !v.isNetworkConnectionRequired()) {
                        tts.setVoice(v);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
        tts.setSpeechRate(0.82f);
        tts.setPitch(1.0f);
    }

    private void speakNow(String text, String kind) {
        if (tts == null || text == null || text.trim().isEmpty()) return;
        setupVoice();
        tts.stop();
        tts.setSpeechRate("sentence".equals(kind) ? 0.90f : 0.82f);
        String s = text.trim();
        if ("word".equals(kind) && !s.matches(".*[.!?。？！]$")) s += ".";
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "vocab_" + System.currentTimeMillis());
    }

    public class AndroidTTSBridge {
        @JavascriptInterface
        public void speak(String text, String kind) {
            runOnUiThread(() -> {
                if (!ttsReady) {
                    pendingText = text;
                    pendingKind = kind == null ? "word" : kind;
                    Toast.makeText(MainActivity.this, "语音引擎启动中，请稍等后再试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                speakNow(text, kind == null ? "word" : kind);
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
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) results = new Uri[]{data.getData()};
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        } else super.onActivityResult(requestCode, resultCode, data);
    }
}
