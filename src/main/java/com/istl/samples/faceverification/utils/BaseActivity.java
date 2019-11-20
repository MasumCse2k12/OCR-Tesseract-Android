package com.istl.samples.faceverification.utils;

import com.neurotec.face.verification.server.rest.ApiException;
import com.istl.samples.faceverification.gui.ErrorDialogFragment;
import com.istl.samples.faceverification.gui.InfoDialogFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

public abstract class BaseActivity extends Activity {

	private static final String TITLE_START = "<title>";
	private static final String TITLE_END = "</title>";

	// ===========================================================
	// Private fields
	// ===========================================================

	private ProgressDialog mProgressDialog;

	// ===========================================================
	// Private methods
	// ===========================================================

	private String getResponseTitle(String src) {
		StringBuilder sb = new StringBuilder();
		sb.append(src.substring(src.indexOf(TITLE_START) + TITLE_START.length(), src.indexOf(TITLE_END)));
		return sb.toString();
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	protected void showProgress(int messageId) {
		showProgress(getString(messageId));
	}

	protected void showProgress(final String message) {
		hideProgress();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProgressDialog = ProgressDialog.show(BaseActivity.this, "", message);
			}
		});
	}

	protected void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		});
	}

	protected void showError(String message, boolean close) {
		ErrorDialogFragment.newInstance(message, close).show(getFragmentManager(), "error");
	}

	protected void showError(int messageId) {
		showError(getString(messageId));
	}

	protected void showError(String message) {
		showError(message, false);
	}

	protected void showError(Throwable th) {
		Log.e(getClass().getSimpleName(), "Exception", th);
		showError(Utils.getMessage(th), false);
	}

	protected void showError(ApiException e) {
		if (e != null) {
			Log.e(e.getClass().getSimpleName(), e.getMessage() + e.getResponseBody(), e);
			Log.i("TEST", "e.getMessage(): " + e.getMessage());
			Log.i("TEST", "e.getResponseBody(): " + e.getResponseBody());
			Log.i("TEST", "e..getCode(): " + e.getCode());
			if (e.getResponseBody() != null && e.getResponseBody().contains(TITLE_START)) {
				showError(e.getMessage() + getResponseTitle(e.getResponseBody()), false);
			} else {
				showError("Unknown exception occurred (please check if cloud server is available): " + e.getMessage(), false);
			}
		}
	}

	protected void showInfo(int messageId) {
		showInfo(getString(messageId));
	}

	protected void showInfo(String message) {
		InfoDialogFragment.newInstance(message).show(getFragmentManager(), "info");
	}

	@Override
	protected void onStop() {
		super.onStop();
		hideProgress();
	}
}

