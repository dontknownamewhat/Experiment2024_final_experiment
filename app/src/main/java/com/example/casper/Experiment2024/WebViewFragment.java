package com.example.casper.Experiment2024;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;

public class WebViewFragment extends Fragment {

    private WebView webView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        webView = view.findViewById(R.id.webview);
        setupWebView();

        return view;
    }

    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 检查是否是HTTP或HTTPS URL
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    // 对于HTTP和HTTPS URL，允许WebView加载
                    view.loadUrl(url);
                    return true;
                } else {
                    // 对于其他类型的URL（如tel:, mailto: 等），可以选择不处理或者使用Intent打开
                    // 这里我们简单地返回true来阻止WebView处理这些URL
                    return true;
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setAllowFileAccess(true); // 允许文件访问（如果需要）
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 允许混合内容（如果需要）

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.loadUrl("https://www.baidu.com");
    }
}