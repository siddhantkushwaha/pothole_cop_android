package com.potholecop.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.countryCodePicker)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.ccpLinearLayout)
    LinearLayout ccpLinearLayout;
    @BindView(R.id.phoneEditText)
    EditText phoneEditText;
    @BindView(R.id.otpEditText)
    EditText otpEditText;
    @BindView(R.id.resendOTPButton)
    Button resendOTPButton;
    @BindView(R.id.submitButton)
    Button submitButton;
    @BindView(R.id.loginInputs)
    LinearLayout loginInputs;
    @BindView(R.id.verifyOTPButton)
    Button verifyOTPButton;
    @BindView(R.id.phoneNumber)
    TextInputLayout phoneNumber;
    @BindView(R.id.verifyLinearLayout)
    LinearLayout verifyLinearLayout;
    @BindView(R.id.toggleButton2)
    Button wrongNumber;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.otp)
    TextInputLayout otp;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private String TAG = LoginActivity.class.toString();

    private static final String STATE_ENTER_PHONE = "STATE_ENTER_PHONE";
    private static final String STATE_SENDING_OTP = "STATE_SENDING_OTP";
    private static final String STATE_ENTER_OTP = "STATE_ENTER_OTP";
    private static final String STATE_PHONE_VERIFICATION_FAILED = "STATE_PHONE_VERIFICATION_FAILED";
    private static final String STATE_PHONE_VERIFICATION_SUCCESSFUL = "STATE_PHONE_VERIFICATION_SUCCESSFUL";
    private EditText uiState;

    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken phoneOTPResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerificationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        countryCodePicker.registerCarrierNumberEditText(phoneEditText);

        uiState = new EditText(LoginActivity.this);
        uiState.setText(STATE_ENTER_PHONE);
        wrongNumber.setOnClickListener(view -> {
            loginInputs.setVisibility(View.VISIBLE);
            verifyLinearLayout.setVisibility(View.GONE);
        });
        uiState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG + " check", s.toString());
                switch (s.toString()) {
                    case STATE_ENTER_PHONE:
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(findViewById(android.R.id.content), "OTP Sent.", Snackbar.LENGTH_LONG).show();
                        verifyLinearLayout.setVisibility(View.GONE);
                        loginInputs.setVisibility(View.VISIBLE);
                        break;
                    case STATE_SENDING_OTP:
                        progressBar.setVisibility(View.VISIBLE);
                        Snackbar.make(findViewById(android.R.id.content), "Sending OTP.", Snackbar.LENGTH_LONG).show();
                        break;
                    case STATE_ENTER_OTP:
                        progressBar.setVisibility(View.GONE);
                        loginInputs.setVisibility(View.GONE);
                        verifyLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case STATE_PHONE_VERIFICATION_FAILED:
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(findViewById(android.R.id.content), "Verification Failed.", Snackbar.LENGTH_LONG).show();
                        break;
                    case STATE_PHONE_VERIFICATION_SUCCESSFUL:
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phoneVerificationCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                uiState.setText(STATE_PHONE_VERIFICATION_SUCCESSFUL);

                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                uiState.setText(STATE_PHONE_VERIFICATION_FAILED);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                uiState.setText(STATE_ENTER_OTP);

                phoneVerificationId = s;
                phoneOTPResendingToken = forceResendingToken;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            Intent intent = new Intent(LoginActivity.this, LoggedInActivity.class);
//            startActivity(intent);
//            finish();
        }
    }

    @OnClick(R.id.verifyOTPButton)
    public void onVerifyOTPButtonClicked() {

        verifyOTP();
    }

    @OnClick(R.id.resendOTPButton)
    public void onResendOTPButtonClicked() {

        Log.i(TAG, countryCodePicker.getFullNumberWithPlus());
        if (!countryCodePicker.isValidFullNumber()) {
            Snackbar.make(findViewById(android.R.id.content), "Invalid Phone Number.", Snackbar.LENGTH_LONG).show();
            return;
        }
        uiState.setText(STATE_SENDING_OTP);
        resendOTP();
    }

    @OnClick(R.id.submitButton)
    public void onSubmitButtonClicked() {

        Log.i(TAG, countryCodePicker.getFullNumberWithPlus());
        if (!countryCodePicker.isValidFullNumber()) {
            Snackbar.make(findViewById(android.R.id.content), "Invalid Phone Number..", Snackbar.LENGTH_LONG).show();
            return;
        }
        uiState.setText(STATE_SENDING_OTP);
        sendOTP();
    }

    private void sendOTP() {

        String fullPhoneNumber = countryCodePicker.getFullNumberWithPlus();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                fullPhoneNumber,
                60,
                TimeUnit.SECONDS,
                LoginActivity.this,
                phoneVerificationCallback
        );
    }

    private void resendOTP() {

        if (phoneOTPResendingToken == null)
            return;

        String fullPhoneNumber = countryCodePicker.getFullNumberWithPlus();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                fullPhoneNumber,
                60,
                TimeUnit.SECONDS,
                LoginActivity.this,
                phoneVerificationCallback,
                phoneOTPResendingToken
        );
    }

    private void verifyOTP() {

        if (phoneVerificationId == null)
            return;

        String otp = otpEditText.getText().toString();
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(phoneVerificationId, otp);
        signIn(phoneAuthCredential);
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {

//                    Intent intent = new Intent(LoginActivity.this, LoggedInActivity.class);
//                    startActivity(intent);
//                    finish();

                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Login Failed.", Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
}
