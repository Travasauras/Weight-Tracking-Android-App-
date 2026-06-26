package com.example.weighttracker4000;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditInput;
    private EditText passwordEditInput;
    private Button loginButton;
    private Button createAccountButton;

    private WeightDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Connect UI elements to the login screen layout
        usernameEditInput = findViewById(R.id.usernameEditInput);
        passwordEditInput = findViewById(R.id.passwordEditInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Initialize the database
        database = new WeightDatabase(this);

        // Set button actions for logging in and creating an account
        loginButton.setOnClickListener(v -> loginUser());
        createAccountButton.setOnClickListener(v -> createUser());
    }

    // Validate the user's login information
    private void loginUser() {
        String username = usernameEditInput.getText().toString().trim();
        String password = passwordEditInput.getText().toString().trim();

        // Make sure both fields are filled in
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check the database for a matching username and password
        int userID = database.checkLogin(username, password);

        if (userID != -1) {

            // Send new users to the SMS permission screen first
            // Send returning users directly to the weight tracker screen
            Intent intent;

            if (database.hasNotificationSettings(userID)) {
                intent = new Intent(MainActivity.this, WeightActivity.class);
            } else {
                intent = new Intent(MainActivity.this, SMSActivity.class);
            }

            // Pass the current user ID to the next screen
            intent.putExtra("USER_ID", userID);
            startActivity(intent);

        } else {
            // Show an error if the login information is incorrect
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    // Create a new user account
    private void createUser() {
        String username = usernameEditInput.getText().toString().trim();
        String password = passwordEditInput.getText().toString().trim();

        // Make sure both fields are filled in
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add the new user to the database
        long result = database.addUser(username, password);

        if (result == -1) {
            // Username is already being used
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        } else {
            // Account was created successfully and user is sent to the SMS permission screen
            Intent intent = new Intent(MainActivity.this, SMSActivity.class);
            intent.putExtra("USER_ID", (int) result);
            startActivity(intent);
        }
    }
}