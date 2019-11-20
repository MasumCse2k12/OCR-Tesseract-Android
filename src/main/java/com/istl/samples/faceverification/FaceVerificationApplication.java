package com.istl.samples.faceverification;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.istl.samples.faceverification.gui.EnrollmentDialogFragment;
import com.istl.samples.faceverification.gui.NFaceVerificationClientView;
import com.istl.samples.faceverification.gui.SettingsActivity;
import com.istl.samples.faceverification.gui.SettingsFragment;
import com.istl.samples.faceverification.gui.SubjectListFragment;
import com.istl.samples.faceverification.utils.BaseActivity;
import com.istl.samples.faceverification.utils.FVDatabaseHelper;
import com.istl.samples.faceverification.utils.Utils;
import com.neurotec.face.verification.client.NCapturePreviewEvent;
import com.neurotec.face.verification.client.NCapturePreviewListener;
import com.neurotec.face.verification.client.NFaceVerificationClient;
import com.neurotec.face.verification.client.NOperationResult;
import com.neurotec.face.verification.client.NStatus;
import com.neurotec.face.verification.server.rest.ApiClient;
import com.neurotec.face.verification.server.rest.ApiException;
import com.neurotec.face.verification.server.rest.api.OperationApi;
import com.istl.samples.faceverification.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceVerificationApplication extends BaseActivity implements SubjectListFragment.SubjectSelectionListener, EnrollmentDialogFragment.EnrollmentDialogListener {

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

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private Context myContext;
    public Criteria criteria;
    public String bestProvider;

    private Button mEnrollButton = null;
    //	private Button mForceButton = null;
//	private Button mCancelButton = null;
    private Button mVerifyButton = null;
//	private Button mCheckLivenessButton = null;

    // ===========================================================
    // Protected methods
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlvdemo);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

// Get the Instance ID token//
                        String token = task.getResult().getToken();
                        String msg = "FCM Token : " + token;
                        Log.d(TAG, msg);

                    }
                });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        NFaceVerificationClient.setEnableLogging(true);
        // on application start you must set NCore context
        myContext = this;
        NFaceVerificationClient.setContext(this);
        mDBHelper = new FVDatabaseHelper(this);

        // button implementations
        mEnrollButton = (Button) findViewById(R.id.button_enroll);
        mEnrollButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOperationApi != null) {
                    new EnrollmentDialogFragment().show(getFragmentManager(), "enrollment");
                } else {
                    showError("Operation Api was not initialised");
                }
            }
        });

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

        mVerifyButton = (Button) findViewById(R.id.button_verify);
        mVerifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_REQUEST_CODE, VERIFICATION_REQUEST_CODE);
                SubjectListFragment.newInstance(mDBHelper.listSubjectIDs(), true, bundle).show(getFragmentManager(), "verification");
            }
        });

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
        if (neededPermissions.length == 0) {
            new InitializationTask().execute();
        } else {
            requestPermissions(neededPermissions);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                Log.e("IMEI", "imei no >>>> " + telephonyManager.getDeviceId());
                Utils.imeiNo = telephonyManager.getDeviceId();
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.e("GPS", "+++++++++++++++0GPS________________________");
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                String country = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();

                Utils.countryName = country;

                Geocoder geocoder = new Geocoder(getApplicationContext());
                for (String provider : locationManager.getAllProviders()) {
                    @SuppressWarnings("ResourceType") Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && addresses.size() > 0) {
                                country = addresses.get(0).getCountryName();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Toast.makeText(getApplicationContext(), country, Toast.LENGTH_LONG).show();
                Utils.countryName = country;

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        String coutry_name = "null";
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        if (location != null) {
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && addresses.size() > 0) {
                                    coutry_name = addresses.get(0).getCountryName();
//                                    Toast.makeText(getBaseContext(), "GPSInfo Add: " + addresses.get(0).getAddressLine(0) + " PostCode: " + addresses.get(0).getPostalCode() + " CounCode: " + addresses.get(0).getCountryCode() + " AdArea: " + addresses.get(0).getAdminArea(), Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Utils.countryName = coutry_name;
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.e("Latitude", "disable");
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.e("Latitude", "enable");
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        Log.e("Latitude", "status");
                    }
                };

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);


                isLocationEnabled();

//				getLocation();


            }
        }


    }

//	@SuppressLint("MissingPermission")
//	protected void getLocation() {
//		if (isLocationEnabled(getApplicationContext())) {
//			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//			criteria = new Criteria();
//			bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
//
//			//You can still do this if you like, you might get lucky:
//			Location location = locationManager.getLastKnownLocation(bestProvider);
//			if (location != null) {
//				Log.e("TAG", "GPS is on");
//				latitude = location.getLatitude();
//				longitude = location.getLongitude();
//				Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
////                searchNearestPlace(voice2text);
//			} else {
//				//This is what you need:
//				locationManager.requestLocationUpdates(bestProvider, 1000, 0, locationListener);
//			}
//		}
//	}

    private void isLocationEnabled() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
//        else{
//            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
//            alertDialog.setTitle("Confirm Location");
//            alertDialog.setMessage("Your Location is enabled, please enjoy");
//            alertDialog.setNegativeButton("Back to interface",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alert=alertDialog.create();
//            alert.show();
//        }
    }

    private void setControllsEnabled(boolean enable) {
        mEnrollButton.setEnabled(enable);
//		mForceButton.setEnabled(enable);
//		mCancelButton.setEnabled(enable);
        mVerifyButton.setEnabled(enable);
//		mCheckLivenessButton.setEnabled(enable);
    }

    private String[] getNotGrantedPermissions() {
        List<String> neededPermissions = new ArrayList<String>();
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int phoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int gpsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }
        if (phoneState != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (gpsPermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return neededPermissions.toArray(new String[neededPermissions.size()]);
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION);
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
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    Log.e("IMEI", "imei no >>>> " + telephonyManager.getDeviceId());
                    Utils.imeiNo = telephonyManager.getDeviceId();
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Log.e("GPS", "+++++++++++++++0GPS________________________");
                    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                    String country = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();

                    Utils.countryName = country;

                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    for (String provider : locationManager.getAllProviders()) {
                        @SuppressWarnings("ResourceType") Location location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && addresses.size() > 0) {
                                    country = addresses.get(0).getCountryName();
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Toast.makeText(getApplicationContext(), country, Toast.LENGTH_LONG).show();
                    Utils.countryName = country;

                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                            String coutry_name = "null";
                            Geocoder geocoder = new Geocoder(getApplicationContext());
                            if (location != null) {
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        coutry_name = addresses.get(0).getCountryName();
//                                        Toast.makeText(getBaseContext(), "GPSInfo Add: " + addresses.get(0).getAddressLine(0) + " PostCode: " + addresses.get(0).getPostalCode() + " CounCode: " + addresses.get(0).getCountryCode() + " AdArea: " + addresses.get(0).getAdminArea(), Toast.LENGTH_LONG).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Utils.countryName = coutry_name;
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            Log.e("Latitude", "disable");
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            Log.e("Latitude", "enable");
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            Log.e("Latitude", "status");
                        }
                    };

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);


                    isLocationEnabled();

                }
            }
            break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nlvdemo, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager != null && locationListener != null)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAppClosing = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager != null && locationListener != null)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
            }
        }
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

    public void createTemplate(final String id, final String nid, final String imei, final String country) {
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
                            boolean insertResultSucceeded = mDBHelper.insert(id, nid, imei, country, mTemplateBuffer);
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
    }

    ;

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
    }

    ;

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

    ;

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

    @Override
    public void onSubjectSelected(String subjectID, Bundle bundle) {
        if (bundle.getInt(EXTRA_REQUEST_CODE) == VERIFICATION_REQUEST_CODE) {
            byte[] template = mDBHelper.getTemplate(subjectID);
            verify(template, subjectID);
        }
    }

    @Override
    public void onEnrollmentIDProvided(String id, String nid, String imei, String country) {
        try {
            Log.e(TAG, "mDBHelper.listNIDs() > " + mDBHelper.listNIDs());
            if (id.isEmpty()) {
                showError("Subject ID is empty");
            } else if (nid.isEmpty()) {
                showError("NID is empty");
            } else if (imei.isEmpty()) {
                showError("IMEI No is empty");
            } else if (country.isEmpty()) {
                showError("Location is empty");
            } else if (mDBHelper.listSubjectIDs().contains(id) || mDBHelper.listNIDs().contains(nid)) {
                showError("DB already contains this ID");
            } else {
                createTemplate(id, nid, imei, country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
