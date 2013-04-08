/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.ActivityLayout;
import ru.orangesoftware.financisto.model.Currency;
import ru.orangesoftware.financisto.utils.Utils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 1/19/12 11:24 PM
 */
public class RateNode {

    public static final int EDIT_RATE = 112;

    private final DecimalFormat nf = new DecimalFormat("0.00000");

    private final RateNodeOwner owner;
    private final ActivityLayout x;
    private final LinearLayout layout;

    public View rateInfoNode;
    public TextView rateInfo;
    public EditText rate;
    public ImageButton bCalc;

    public ImageButton bDownload;

    public RateNode(RateNodeOwner owner, ActivityLayout x, LinearLayout layout) {
        this.owner = owner;
        this.x = x;
        this.layout = layout;
        createUI();
    }

    private void createUI() {
        rateInfoNode = x.addRateNode(layout);
        rate = (EditText)rateInfoNode.findViewById(R.id.rate);
        rate.addTextChangedListener(rateWatcher);
        rate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    rate.selectAll();
                }
            }
        });
        rateInfo = (TextView)rateInfoNode.findViewById(R.id.data);
        bCalc = (ImageButton)rateInfoNode.findViewById(R.id.rateCalculator);
        bCalc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Activity activity = owner.getActivity();
                Intent intent = new Intent(activity, CalculatorInput.class);
                intent.putExtra(AmountInput.EXTRA_AMOUNT, String.valueOf(getRate()));
                activity.startActivityForResult(intent, EDIT_RATE);
            }
        });
        bDownload = (ImageButton)rateInfoNode.findViewById(R.id.rateDownload);
        bDownload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new RateDownloadTask().execute();
            }
        });
    }

    public void disableAll() {
        rate.setEnabled(false);
        bCalc.setEnabled(false);
        bDownload.setEnabled(false);
    }

    public void enableAll() {
        rate.setEnabled(true);
        bCalc.setEnabled(true);
        bDownload.setEnabled(true);
    }

    public float getRate() {
        try {
            String rateText = Utils.text(rate);
            if (rateText != null) {
                rateText = rateText.replace(',', '.');
                return Float.parseFloat(rateText);
            }
            return 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setRate(double r) {
        rate.removeTextChangedListener(rateWatcher);
        rate.setText(nf.format(Math.abs(r)));
        rate.addTextChangedListener(rateWatcher);
    }

    public void updateRateInfo() {
        double r = getRate();
        StringBuilder sb = new StringBuilder();
        Currency currencyFrom = owner.getCurrencyFrom();
        Currency currencyTo = owner.getCurrencyTo();
        if (currencyFrom != null && currencyTo != null) {
            sb.append("1").append(currencyFrom.name).append("=").append(nf.format(r)).append(currencyTo.name).append(", ");
            sb.append("1").append(currencyTo.name).append("=").append(nf.format(1.0/r)).append(currencyFrom.name);
        }
        rateInfo.setText(sb.toString());
    }

    private class RateDownloadTask extends AsyncTask<Void, Void, Float> {

        private final HttpClient httpClient = new DefaultHttpClient();
        private final Pattern pattern = Pattern.compile("<double.*?>(.+?)</double>");

        private String error;
        private ProgressDialog progressDialog;

        @Override
        protected Float doInBackground(Void... args) {
            Currency fromCurrency = getFromCurrency();
            Currency toCurrency = getToCurrency();
            if (fromCurrency != null && toCurrency != null) {
                HttpGet get = new HttpGet("http://flowzr-hrd.appspot.com/?action=currencyRateDownload&from_currency="+fromCurrency.name+"&to_currency="+toCurrency.name);
                try {
                    Log.i("RateDownload", get.getURI().toString());
                    HttpResponse r = httpClient.execute(get);
                    int code=r.getStatusLine().getStatusCode();                    
                	String s = EntityUtils.toString(r.getEntity());
                    if (code==200) {
                    	Log.i("RateDownload", s);
                    	return Float.valueOf(s);
                    } else {                   
                        error = s;
                    }                    
                } catch (Exception e) {
                    error = e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
            owner.onBeforeRateDownload();
        }

        private void showProgressDialog() {
            Activity activity = owner.getActivity();
            String message = activity.getString(R.string.downloading_rate, getFromCurrency(), getToCurrency());
            progressDialog = ProgressDialog.show(activity, null, message, true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            owner.onAfterRateDownload();
        }

        @Override
        protected void onPostExecute(Float result) {
            progressDialog.dismiss();
            owner.onAfterRateDownload();
            if (result == null) {
                if (error != null) {
                    Toast t = Toast.makeText(owner.getActivity(), error, Toast.LENGTH_LONG);
                    t.show();
                }
            } else {
                setRate(result);
                owner.onSuccessfulRateDownload();
            }
        }

        private Currency getFromCurrency() {
            return owner.getCurrencyFrom();
        }

        private Currency getToCurrency() {
            return owner.getCurrencyTo();
        }

    }

    private final TextWatcher rateWatcher = new TextWatcher(){
        @Override
        public void afterTextChanged(Editable s) {
            owner.onRateChanged();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };



}
