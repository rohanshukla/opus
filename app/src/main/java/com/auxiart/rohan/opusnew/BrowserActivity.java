package com.auxiart.rohan.opusnew;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class BrowserActivity extends AppCompatActivity {
    Toolbar toolbar;
    WebView webView;
    ProgressBar progressBar;
    EditText searchBar;
    ImageView searchButton;
    SwipeRefreshLayout refreshWV;
    private final int REQ_CODE_SPEECH_INPUT = 2810;
    private static final String TEL_PREFIX = "tel:";
    boolean doublePressedBackExit = false;
    RelativeLayout parentPanel;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        parentPanel = (RelativeLayout) findViewById(R.id.parentPanel);


        searchBar = (EditText) findViewById(R.id.searchBar);
        //this part is used for search query....
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (getCurrentFocus() != null) {
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                    }
                    String urlSearch = searchBar.getText().toString();
                    if (urlSearch.contains("http://") || urlSearch.contains("https://")) {
                        webView.loadUrl(urlSearch);
                        //is used to find any type of URL like .org, .in, .com....
                    } else if (urlSearch.contains(".")) {
                        webView.loadUrl("http://" + urlSearch);
                    } else {
                        //this part is for google search query...
                        urlSearch = "https://www.google.com/search?q=" + urlSearch;
                        webView.loadUrl(urlSearch);
                    }
                    return true;
                }
                return false;
            }
        });

        searchButton = (ImageView) findViewById(R.id.searchButton);

        //is search button on toolbar...
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                String urlSearch = searchBar.getText().toString();
                if (urlSearch.contains("http://") || urlSearch.contains("https://")) {
                    webView.loadUrl(urlSearch);
                    //is used to find any type of URL like .org, .in, .com....
                } else if (urlSearch.contains(".")) {
                    webView.loadUrl("http://" + urlSearch);
                } else {
                    //this part is for google search query...
                    urlSearch = "https://www.google.com/search?q=" + urlSearch;
                    webView.loadUrl(urlSearch);
                }
            }
        });

        //this is a progress bar binding and initialization...
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);

        webView = (WebView) findViewById(R.id.webView);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setFocusable(true);
            webView.setFocusableInTouchMode(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setSupportZoom(true);//allow zooming of webView...
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);//hide the zoom button in webView...
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            webView.getSettings().setSavePassword(false);//don't save password..
            webView.getSettings().setSaveFormData(false);//don't save any form data..
            webView.isPrivateBrowsingEnabled();//enable private browsing
            //webView.getSettings().setSupportMultipleWindows(true);

            webView.setWebViewClient(new OurViewClient());
            webView.clearCache(true);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int progress) {
                    progressBar.setProgress(progress);
                    if (progress < 100 && progressBar.getVisibility() == View.GONE) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    if (progress == 100) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

        //is used to download the file of mime type...
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        //Toast.makeText(BrowserActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

        //is swipe to refresh the webview...
        refreshWV = (SwipeRefreshLayout) findViewById(R.id.refreshWV);
        refreshWV.setColorSchemeResources(R.color.colorAccent);
        refreshWV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
                refreshWV.setRefreshing(false);
            }
        });

        refreshWV.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0) {
                    refreshWV.setEnabled(true);
                } else {
                    refreshWV.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (doublePressedBackExit) {
                webView.clearCache(true);
                webView.clearHistory();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                System.exit(0);
                return;
            }
            this.doublePressedBackExit = true;
            Snackbar.make(parentPanel, "Press again to exit..", Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doublePressedBackExit = false;
                }
            }, 2000);
        }
    }

    /*//This if for Swipe Refresh...
    @Override
    public void onScrollChanged() {
        if (webView.getScrollY()==0){
            refreshWV.setEnabled(true);
        }else {
            refreshWV.setEnabled(false);
        }
    }*/

    //my custom webView with customized code and result...
    private class OurViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(TEL_PREFIX)) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else {
                webView.setBackgroundColor(Color.TRANSPARENT);
                view.loadUrl(url);
                CookieManager.getInstance().setAcceptCookie(true);//should be true to open Gmail and facebook properly in like chrome...
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            searchBar.setText(view.getOriginalUrl());
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            searchBar.setText(url);
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    // Showing google speech input dialog
    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Log.e("Words", a.toString());
        }
    }

    // Receiving speech input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String voiceText = result.get(0);
                    searchBar.setText(voiceText);
                    webView.loadUrl("https://www.google.com/search?q=" + voiceText);
                }
                break;
            }
        }
    }

    public void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Exit?");
        builder.setMessage("Do you want to Exit?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int which) {
                //it is used to delete all the cookie store by this session...
                webView.clearCache(true);
                webView.clearHistory();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                System.exit(0);
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.voiceSearch:
                askSpeechInput();//called  method for tap to speak
                return true;

            case R.id.back:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                return true;

            case R.id.forward:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                return true;

            case R.id.refresh:
                webView.reload();
                return true;

            case R.id.home:
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }

                webView.loadUrl(null);
                searchBar.setText("");
                webView.clearCache(true);
                webView.clearHistory();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                return true;

            case R.id.exit:
                exit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}