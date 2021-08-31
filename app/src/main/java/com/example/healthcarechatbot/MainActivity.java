package com.example.healthcarechatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private LinearLayout loginPage;
    private LinearLayout registerPage;

    private ProgressBar progressBar;

    private EditText emailLoginText;
    private EditText passwordLoginText;
    private Button loginPageButton;
    private TextView toggleOperationLoginText;

    private EditText emailText;
    private EditText passwordText;
    private EditText passwordConfirmText;
    private Button registerPageButton;
    private TextView toggleOperationRegisterText;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);

        loginPage = findViewById(R.id.loginPage);
        registerPage = findViewById(R.id.registerPage);

        emailLoginText = findViewById(R.id.emailLoginText);
        passwordLoginText = findViewById(R.id.passwordLoginText);
        loginPageButton = findViewById(R.id.loginPageButton);
        toggleOperationLoginText = findViewById(R.id.toggleOperationLoginText);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        passwordConfirmText = findViewById(R.id.passwordConfirmText);
        registerPageButton = findViewById(R.id.registerPageButton);
        toggleOperationRegisterText = findViewById(R.id.toggleOperationRegisterText);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            finish();

        }

        toggleOperationLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage.setVisibility(View.GONE);
                registerPage.setVisibility(View.VISIBLE);
            }
        });

        toggleOperationRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPage.setVisibility(View.GONE);
                loginPage.setVisibility(View.VISIBLE);
            }
        });

        loginPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_email = emailLoginText.getText().toString();
                String text_password = passwordLoginText.getText().toString();
                loginUser(text_email, text_password);
            }
        });

        registerPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_email = emailText.getText().toString();
                String text_password = passwordText.getText().toString();
                String text_confirm_password = passwordConfirmText.getText().toString();
                if(TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)) {
                    Toast.makeText(MainActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                }
                else if(text_password.length() < 8) {
                    Toast.makeText(MainActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
                }
                else if(!text_confirm_password.equals(text_password)) {
                    Toast.makeText(MainActivity.this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
                }
                else {
                    registerUser(text_email, text_password);

                }
            }
        });
    }

    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                finish();

                progressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Incorrect login credentials!", Toast.LENGTH_SHORT).show();

                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void registerUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(MainActivity.this, "Registration Unsuccessful!", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);

            }
        });
    }
}