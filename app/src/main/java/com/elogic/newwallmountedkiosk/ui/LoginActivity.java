package com.elogic.newwallmountedkiosk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elogic.newwallmountedkiosk.R;
import com.elogic.newwallmountedkiosk.responses.ApiResponse;
import com.elogic.newwallmountedkiosk.responses.LoginData;
import com.elogic.newwallmountedkiosk.responses.LoginRequest;
import com.elogic.newwallmountedkiosk.responses.LoginResponse;
import com.elogic.newwallmountedkiosk.responses.Terminal;
import com.elogic.newwallmountedkiosk.services.ApiService;
import com.elogic.newwallmountedkiosk.services.RetrofitClient;
import com.elogic.newwallmountedkiosk.services.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private MaterialAutoCompleteTextView autoCompleteLocation;
    private TextInputLayout locationInputLayout, usernameInputLayout, passwordInputLayout;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;
    private Terminal selectedTerminal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        apiService = RetrofitClient.getInstance().getApi();
        sessionManager = new SessionManager(this);

        autoCompleteLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTerminal = (Terminal) parent.getItemAtPosition(position);
                locationInputLayout.setError(null);
            }
        });

        fetchTerminals();

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void initViews() {
        locationInputLayout = findViewById(R.id.locationInputLayout);
        autoCompleteLocation = findViewById(R.id.autoCompleteLocation);
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        etUsername = findViewById(R.id.etUsername);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void fetchTerminals() {
        setLoadingState(true);

        apiService.getTerminals().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if ("1".equals(apiResponse.getCode())) {
                        parseAndDisplayTerminals(apiResponse.getData());
                    } else {
                        showError("Failed to load terminals: " + apiResponse.getMessage());
                    }
                } else {
                    handleApiError(response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoadingState(false);
                handleNetworkFailure(t);
            }
        });
    }

    private void parseAndDisplayTerminals(String dataString) {
        if (dataString == null || dataString.trim().isEmpty()) {
            showError("No terminal data received.");
            return;
        }

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Terminal>>() {
            }.getType();
            List<Terminal> terminalList = gson.fromJson(dataString, listType);

            if (terminalList == null || terminalList.isEmpty()) {
                showError("Terminal list is empty.");
                return;
            }

            ArrayAdapter<Terminal> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, terminalList);
            autoCompleteLocation.setAdapter(adapter);

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON Parsing error when loading terminals");
            showError("Failed to process terminal data.");
        }
    }

    private void performLogin() {
        if (!validateInputs()) {
            return;
        }

        setLoadingState(true);

        String terminalId = selectedTerminal.getId();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        LoginRequest request = new LoginRequest(username, password, terminalId);

        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Validate business logic success code
                    if ("1".equals(loginResponse.getCode())) {
                        LoginData data = loginResponse.getData();

                        // Ensure data payload and token are present securely
                        if (data != null && !TextUtils.isEmpty(data.getToken())) {

                            // 1. Securely save all required session data
                            sessionManager.saveTerminalId(selectedTerminal.getId());
                            sessionManager.saveAuthToken(data.getToken());
                            sessionManager.saveTimerStatus(data.getTimerStatus());

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            // 2. Navigate to Next Activity (Update MainActivity as needed)
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Missing token payload
                            showError("Authentication failed: Missing token in response.");
                        }
                    } else {
                        // Business logic error (e.g., code "0", Invalid credentials)
                        // Do not log the token or raw payload here for security
                        String errorMsg = TextUtils.isEmpty(loginResponse.getMessage()) ? "Invalid credentials." : loginResponse.getMessage();
                        showError(errorMsg);
                    }
                } else {
                    // HTTP Level Errors (401, 500, etc.)
                    handleApiError(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoadingState(false);
                handleNetworkFailure(t);
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Ensure terminal is selected and matches the dropdown text exactly
        if (selectedTerminal == null || TextUtils.isEmpty(autoCompleteLocation.getText().toString())) {
            locationInputLayout.setError("Please select a valid terminal");
            isValid = false;
        } else if (!autoCompleteLocation.getText().toString().equals(selectedTerminal.getDescription())) {
            locationInputLayout.setError("Invalid terminal selection");
            isValid = false;
        } else {
            locationInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(etUsername.getText().toString())) {
            usernameInputLayout.setError("Username is required");
            isValid = false;
        } else {
            usernameInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return isValid;
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false); // Prevents multi-click
            autoCompleteLocation.setEnabled(false);
            etUsername.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            autoCompleteLocation.setEnabled(true);
            etUsername.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }

    private void handleApiError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 400:
                errorMessage = "Invalid request format.";
                break;
            case 401:
                errorMessage = "Unauthorized. Please check your credentials.";
                break;
            case 403:
                errorMessage = "Access forbidden.";
                break;
            case 404:
                errorMessage = "Authentication endpoint not found.";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            case 503:
                errorMessage = "Service unavailable.";
                break;
            case 504:
                errorMessage = "Gateway timeout.";
                break;
            default:
                errorMessage = "An unexpected error occurred.";
                break;
        }
        showError(errorMessage);
        Log.w(TAG, "API Error returned code: " + statusCode);
    }

    private void handleNetworkFailure(Throwable t) {
        String errorMessage = "Network failure. Please check your internet connection.";
        if (t instanceof IOException) {
            errorMessage = "Connection timeout or no internet. Please try again.";
        }
        showError(errorMessage);
        // Log the exception class/message for debugging without exposing sensitive inputs
        Log.e(TAG, "Network Failure: " + t.getMessage());
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}