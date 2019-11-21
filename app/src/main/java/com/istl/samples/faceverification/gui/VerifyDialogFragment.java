package com.istl.samples.faceverification.gui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.istl.samples.faceverification.R;
import com.istl.samples.faceverification.services.NidService;
import com.istl.samples.faceverification.services.RetrofitSingleton;
import com.istl.samples.faceverification.utils.NidRequest;
import com.istl.samples.faceverification.utils.VoterInfo;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyDialogFragment extends DialogFragment {

    // ===========================================================
    // Public types
    // ===========================================================

    public interface VerifyDialogListener {
        void onVerifyProvided(byte[] photo, String nidUser, boolean isVerify);
        void onVerifyFailed( String response);
    }

    // ===========================================================
    // Private fields
    // ===========================================================

    private VerifyDialogListener mListener;
    DatePickerDialog picker;
    EditText dateOfBirth;
    EditText voterNid;
    Button btnGet;
//    TextView tvw;
    Context context;
    ImageView imageView ;

    // ===========================================================
    // Public constructor
    // ===========================================================

    public VerifyDialogFragment() {
    }

    // ===========================================================
    // Public methods
    // ===========================================================



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (VerifyDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EnrollmentDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.nid_face_match, null);
        imageView = view.findViewById(R.id.faceView);
//        tvw=(TextView)view.findViewById(R.id.dateView);
        voterNid=(EditText) view.findViewById(R.id.voterNid);
        dateOfBirth=(EditText) view.findViewById(R.id.dateOfBirth);
//        dateOfBirth.setInputType(InputType.TYPE_NULL);
//        dateOfBirth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar cldr = Calendar.getInstance();
//                int day = cldr.get(Calendar.DAY_OF_MONTH);
//                int month = cldr.get(Calendar.MONTH);
//                int year = cldr.get(Calendar.YEAR);
//                // date picker dialog
//                picker = new DatePickerDialog(FaceVerificationApplication.context,
//                        new DatePickerDialog.OnDateSetListener() {
//                            @Override
//                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                                dateOfBirth.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
//                            }
//                        }, year, month, day);
//                picker.show();
//            }
//        });
        btnGet=(Button)view.findViewById(R.id.pickDate);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvw.setText("Selected Date: "+ dateOfBirth.getText());
                NidRequest request = new NidRequest();

                request.setNid(voterNid.getText().toString());
                request.setDob(dateOfBirth.getText().toString());
                NidService services = RetrofitSingleton.getClient().create(NidService.class);

                services.verifyVoter(request).enqueue(new Callback<VoterInfo>() {
                    @Override
                    public void onResponse(Call<VoterInfo> call, Response<VoterInfo> response) {
                        if (response.body() != null && response.body().getPhoto() != null) {
                            imageView.setImageBitmap(BitmapFactory.decodeByteArray(Base64.decode(response.body().getPhoto(),Base64.DEFAULT), 0,Base64.decode(response.body().getPhoto(),Base64.DEFAULT).length));
                            mListener.onVerifyProvided(Base64.decode(response.body().getPhoto(),Base64.DEFAULT),response.body().getNameEnglish(), false);
                        }
                    }

                    @Override
                    public void onFailure(Call<VoterInfo> call, Throwable t) {
                        mListener.onVerifyFailed("NOT FOUND !");


                    }
                });

            }
        });


        builder.setView(view);
        builder.setTitle("Enter Info for Verification");
        builder.setPositiveButton(getString(R.string.msg_verify), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                NidRequest request = new NidRequest();

                request.setNid(voterNid.getText().toString());
                request.setDob(dateOfBirth.getText().toString());
                NidService services = RetrofitSingleton.getClient().create(NidService.class);

                services.verifyVoter(request).enqueue(new Callback<VoterInfo>() {
                    @Override
                    public void onResponse(Call<VoterInfo> call, Response<VoterInfo> response) {
                        if (response.body() != null && response.body().getPhoto() != null) {

                            mListener.onVerifyProvided(Base64.decode(response.body().getPhoto(),Base64.DEFAULT),response.body().getNameEnglish(),true);
                        }
                    }

                    @Override
                    public void onFailure(Call<VoterInfo> call, Throwable t) {
                        mListener.onVerifyFailed("NOT FOUND !");
                    }
                });

            }
        });
        builder.setNegativeButton(getString(R.string.msg_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

}
