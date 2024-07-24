package com.example.barcode2ds;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginSignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signupTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        usernameEditText = findViewById(R.id.editTextText4);
        passwordEditText = findViewById(R.id.editTextTextPassword2);
        loginButton = findViewById(R.id.button);
        signupTextView = findViewById(R.id.textView10);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignupActivity();
            }
        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedPassword = sharedPreferences.getString(username, "");

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin đăng nhập", Toast.LENGTH_SHORT).show();
        } else if (password.equals(savedPassword)) {
            // Đăng nhập thành công, chuyển sang MainActivity
            Intent intent = new Intent(LoginSignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Username hoặc Password bị nhập sai", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSignupActivity() {
        setContentView(R.layout.signup_layout);

        final EditText signupUsernameEditText = findViewById(R.id.editTextText4);
        final EditText signupPasswordEditText = findViewById(R.id.editTextTextPassword2);
        final EditText confirmPasswordEditText = findViewById(R.id.editTextTextPassword);
        Button signupButton = findViewById(R.id.button);
        TextView loginTextView = findViewById(R.id.textView9);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = signupUsernameEditText.getText().toString().trim();
                String password = signupPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(LoginSignupActivity.this, "Thiếu thông tin đăng ký", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(LoginSignupActivity.this, "Xem xét lại mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(username, password);
                    editor.apply();

                    Toast.makeText(LoginSignupActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    // Quay lại màn hình đăng nhập
                    setContentView(R.layout.login_layout);
                    recreate();
                }
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.login_layout);
                recreate();
            }
        });
    }
}

