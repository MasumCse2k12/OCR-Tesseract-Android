package com.istl.samples.faceverification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.imperialsoupgmail.faceverification.R;
import com.istl.samples.faceverification.gui.NFaceVerificationClientView;
import com.istl.samples.faceverification.gui.SettingsActivity;
import com.istl.samples.faceverification.gui.SettingsFragment;
import com.istl.samples.faceverification.gui.VerifyDialogFragment;
import com.istl.samples.faceverification.utils.BaseActivity;
import com.istl.samples.faceverification.utils.FVDatabaseHelper;
import com.neurotec.face.verification.client.NCapturePreviewEvent;
import com.neurotec.face.verification.client.NCapturePreviewListener;
import com.neurotec.face.verification.client.NFaceVerificationClient;
import com.neurotec.face.verification.client.NOperationResult;
import com.neurotec.face.verification.client.NStatus;
import com.neurotec.face.verification.server.rest.ApiClient;
import com.neurotec.face.verification.server.rest.ApiException;
import com.neurotec.face.verification.server.rest.api.OperationApi;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceVerificationApplication extends BaseActivity implements  VerifyDialogFragment.VerifyDialogListener {

	// ===========================================================
	// Private fields
	// ===========================================================

	private static final String TAG = "FaceVerificationApp";
	private static final String EXTRA_REQUEST_CODE = "request_code";
	private static final int VERIFICATION_REQUEST_CODE = 1;
	private static final int REQUEST_CAMERA_PERMISSION = 10;
	private boolean mAppClosing;
	private NFaceVerificationClientView mFaceView;
	private OperationApi mOperationApi;
	private byte[] mTemplateBuffer = null;
	private FVDatabaseHelper mDBHelper;
	private NFaceVerificationClient mNFV = null;
	private Map<String, Integer> mPermissions = new HashMap<String, Integer>();

//	private Button mEnrollButton = null;
//	private Button mForceButton = null;
//	private Button mCancelButton = null;
	private Button mVerifyButton = null;
//	private Button mCheckLivenessButton = null;
	public static  Context   context;

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nlvdemo);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		NFaceVerificationClient.setEnableLogging(true);
		// on application start you must set NCore context
		NFaceVerificationClient.setContext(this);
		mDBHelper = new FVDatabaseHelper(this);
		context = this;

		// button implementations
//		mEnrollButton = (Button) findViewById(R.id.button_enroll);
//		mEnrollButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if(mOperationApi != null) {
//					new EnrollmentDialogFragment().show(getFragmentManager(), "enrollment");
//				} else {
//					showError("Operation Api was not initialised");
//				}
//			}
//		});
//
//		mForceButton = (Button) findViewById(R.id.button_force);
//		mForceButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (mNFV != null) {
//					mNFV.force();
//				} else {
//					showError("Face verification client was not initialised");
//				}
//			}
//		});
//
//		mCancelButton = (Button) findViewById(R.id.button_cancel);
//		mCancelButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (mNFV != null) {
//					showProgress(R.string.msg_cancelling);
//					mNFV.cancel();
//					hideProgress();
//				} else {
//					showError("Face verification client was not initialised");
//				}
//			}
//		});
//
		mVerifyButton = (Button) findViewById(R.id.button_verify);
		mVerifyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mOperationApi != null) {
					new VerifyDialogFragment().show(getFragmentManager(), "verification");
				} else {
					showError("Operation Api was not initialised");
				}

//				Bundle bundle = new Bundle();
//				bundle.putInt(EXTRA_REQUEST_CODE, VERIFICATION_REQUEST_CODE);
//				ListFragment.newInstance(mDBHelper.listSubjectIDs(), true, bundle).show(getFragmentManager(), "verification");
			}
		});
//
//		mCheckLivenessButton = (Button) findViewById(R.id.button_checkLiveness);
//		mCheckLivenessButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if(mOperationApi != null) {
//					checkLiveness();
//				} else {
//					showError("Operation Api was not initialised");
//				}
//			}
//		});

		setControllsEnabled(false);

		mFaceView = (NFaceVerificationClientView) findViewById(R.id.nFaceView);

		String[] neededPermissions = getNotGrantedPermissions();
		if(neededPermissions.length == 0) {
			new InitializationTask().execute();
		} else {
			requestPermissions(neededPermissions);
		}
	}

	private void setControllsEnabled(boolean enable) {
//		mEnrollButton.setEnabled(enable);
//		mForceButton.setEnabled(enable);
//		mCancelButton.setEnabled(enable);
		mVerifyButton.setEnabled(enable);
//		mCheckLivenessButton.setEnabled(enable);
	}

	private String[] getNotGrantedPermissions() {
		List<String> neededPermissions = new ArrayList<String>();
		int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

		if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.CAMERA);
		}
		return neededPermissions.toArray(new String[neededPermissions.size()]);
	}

	private void requestPermissions(String[] permissions) {
		ActivityCompat.requestPermissions(this, permissions,REQUEST_CAMERA_PERMISSION);
	}

	public void onRequestPermissionsResult(int requestCode, final String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CAMERA_PERMISSION: {
				// Initialize the map with permissions
				mPermissions.clear();
				// Fill with actual results from user
				if (grantResults.length > 0) {
					for (int i = 0; i < permissions.length; i++) {
						mPermissions.put(permissions[i], grantResults[i]);
					}
					// Check if at least one is not granted
					if (mPermissions.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
						showError("Permission not granted", true);
					} else {
						Log.i(TAG, "Permission granted");
						new InitializationTask().execute();
					}
				}
			} break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nlvdemo, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAppClosing = false;
		try {
			if ((mOperationApi == null) || (SettingsFragment.isUpdateClientNeeded())) {
				Log.i("TEST", "Update client");
				ApiClient client = new ApiClient();
				client.setConnectTimeout(60000);
				SettingsFragment.updateClientAuthentification(client);
				mOperationApi = new OperationApi(client);
			}
			setControllsEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
			setControllsEnabled(false);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mAppClosing = true;
	}

	public void createTemplate(final String id) {
		Log.i("TEST", "createTemplate");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// cancel in there are any other operations in progress
					NFV.getInstance().cancel();
					byte[] registrationKey = NFV.getInstance().startCreateTemplate();
					byte[] serverKey = mOperationApi.validate(registrationKey);
					NOperationResult result = NFV.getInstance().finishOperation(serverKey);
					if (!mAppClosing) {
						mFaceView.setEventInfo(result);
						if (result.getStatus() == NStatus.SUCCESS) {
							mTemplateBuffer = result.getTemplate();
							boolean insertResultSucceeded = mDBHelper.insert(id, mTemplateBuffer);
							showInfo(String.format(getString(R.string.msg_operation_status), insertResultSucceeded ?
									(String.format(getString(R.string.msg_enrollment_to_db_succeeded))) :
									(String.format(getString(R.string.msg_enrollment_to_db_failed)))));
						} else {
							showInfo(String.format(getString(R.string.msg_operation_status), result.getStatus().toString().toLowerCase()));
						}
					}
				} catch (ApiException e) {
					showError(e);
				} catch (Exception e) {
					showError(e);
				}
			}
		}).start();
	};

	public void checkLiveness() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// cancel in there are any other operations in progress
					NFV.getInstance().cancel();
					byte[] registrationKey = NFV.getInstance().startCheckLiveness();
					byte[] serverKey = mOperationApi.validate(registrationKey);
					NOperationResult result = NFV.getInstance().finishOperation(serverKey);
					if (!mAppClosing) {
						showInfo(String.format(getString(R.string.msg_operation_status), result.getStatus().toString().toLowerCase()));
						if (result.getStatus() == NStatus.SUCCESS) {
							mFaceView.setEventInfo(result);
						}
					}
				} catch (ApiException e) {
					showError(e);
				} catch (Throwable e) {
					showError(e);
				}
			}
		}).start();
	};

	public void verify(final byte[] template) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// cancel in there are any other operations in progress
					NFV.getInstance().cancel();
					if (template == null) {
						if (!mAppClosing) showInfo(getString(R.string.msg_buffer_is_null));
						return;
					}
					NOperationResult result = NFV.getInstance().verify(template);
					if (!mAppClosing) {
						mFaceView.setEventInfo(result);
						if (result.getStatus() == NStatus.SUCCESS) {
							showInfo(String.format(getString(R.string.msg_operation_status), String.format(getString(R.string.msg_verification_succeeded))));
						} else {
							showInfo(String.format(getString(R.string.msg_operation_status), String.format(getString(R.string.msg_verification_failed) + result.getStatus().toString().toLowerCase())));
						}
					}
				} catch (Throwable e) {
					showError(e);
				}
			}
		}).start();
	};

	@Override
	protected void onStop() {
		mAppClosing = true;
		try {
			NFV.getInstance().cancel();
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onOptionsItemSelected" + item.getTitle());
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_clear_db) {
			Log.i(TAG, "action_clear_db");
			mDBHelper.clearTable();
		}
		return super.onOptionsItemSelected(item);
	}



	public void verifyFaceFromImage(final byte[] template, final String voter){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// cancel in there are any other operations in progress



					NFV.getInstance().cancel();
					byte[] registrationKey = NFV.getInstance().startImportImage(template);
					byte[] serverKey = mOperationApi.validate(registrationKey);
					NOperationResult result = NFV.getInstance().finishOperation(serverKey);
					if (!mAppClosing) {
						mFaceView.setEventInfo(result);
						if (result.getStatus() == NStatus.SUCCESS) {
							Log.e("ImportImage"," create template from import image");
							mTemplateBuffer = result.getTemplate();
							verify(mTemplateBuffer,voter);
						} else {
							showInfo(String.format(getString(R.string.msg_operation_status), result.getStatus().toString().toLowerCase()));
						}
					}
				} catch (ApiException e) {
					showError(e);
				} catch (Exception e) {
					showError(e);
				}
			}
		}).start();
	}
	public void verify(final byte[] template, final String subID) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// cancel in there are any other operations in progress
					NFV.getInstance().cancel();
					if (template == null) {
						if (!mAppClosing) showInfo(getString(R.string.msg_buffer_is_null));
						return;
					}
					NOperationResult result = NFV.getInstance().verify(template);
					if (!mAppClosing) {
						mFaceView.setEventInfo(result);
						if (result.getStatus() == NStatus.SUCCESS) {
//                            showInfo(String.format(getString(R.string.msg_operation_status), String.format(getString(R.string.msg_verification_succeeded))));
							showInfo(String.format(getString(R.string.msg_operation_status), String.format(getString(R.string.msg_verification_succeeded)) + "\n" + subID));
						} else {
							showInfo(String.format(getString(R.string.msg_operation_status), String.format(getString(R.string.msg_verification_failed) + result.getStatus().toString().toLowerCase())));
						}
					}
				} catch (Throwable e) {
					showError(e);
				}
			}
		}).start();
	}

	@Override
	public void onVerifyProvided(byte[] photo, String nidUser, boolean isVerify) {

		if(isVerify){
			Toast toast = Toast.makeText(this,"Ready to verify", Toast.LENGTH_SHORT);
			toast.show();
			verifyFaceFromImage(photo,nidUser);
		}
	}

	@Override
	public void onVerifyFailed(String response) {
		Toast toast = Toast.makeText(this,response, Toast.LENGTH_LONG);
		toast.show();
	}

	final class InitializationTask extends AsyncTask<Object, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress(R.string.msg_initialising);
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				// get NFV for the first time
				mNFV = NFV.getInstance();

				// load settings
				SettingsFragment.loadSettings();

				mNFV.setCapturePreviewListener(new NCapturePreviewListener() {

					@Override
					public void capturePreview(NCapturePreviewEvent nCapturePreviewEvent) {

						mFaceView.setEvent(nCapturePreviewEvent);
					}
				});
			} catch (Exception e) {
				Log.e(FaceVerificationApplication.this.TAG, e.getMessage(), e);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			hideProgress();
		}
	}
}
