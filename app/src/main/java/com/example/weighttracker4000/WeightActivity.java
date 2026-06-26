package com.example.weighttracker4000;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import androidx.appcompat.app.AppCompatActivity;


public class WeightActivity extends AppCompatActivity {

    private int userID;
    private WeightDatabase database;

    private EditText goalWeightInput;
    private EditText dateInput;
    private EditText weightInput;
    private Button setGoalButton;
    private Button addWeightButton;
    private LinearLayout weightGridContainer;
    private int selectedWeightID = -1;

// onCreate method to initialize the activity and set up the UI components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_grid_screen);

        // Initialize database and get user ID from intent
        database = new WeightDatabase(this);
        userID = getIntent().getIntExtra("USER_ID", -1);

        // Initialize UI components by finding their IDs
        goalWeightInput = findViewById(R.id.goalWeightInput);
        dateInput = findViewById(R.id.dateInput);
        weightInput = findViewById(R.id.weightInput);
        setGoalButton = findViewById(R.id.setGoalButton);
        addWeightButton = findViewById(R.id.addWeightButton);
        weightGridContainer = findViewById(R.id.weightGridContainer);

        //load existing weight entries from the database and display them in the grid
        loadGoalWeight();
        loadWeights();

        // Set click listener for the "Set Goal" button to save the user's goal weight
        setGoalButton.setOnClickListener(v -> setGoalWeight());
        addWeightButton.setOnClickListener(v -> addWeightEntry());
    }


// Load the user's goal weight from the database and display it in the input field
    private void loadGoalWeight() {
        double goalWeight = database.getGoalWeight(userID);

// If a goal weight exists, set it in the input field
        if (goalWeight > 0) {
            goalWeightInput.setText(String.valueOf(goalWeight));
        }
    }


    // Method to load the user's weight entries from the database and display them in a grid layout
    private void setGoalWeight() {
        String goalText = goalWeightInput.getText().toString().trim();

        // if the input is empty when the button is clicked, show a toast message asking the user to enter a valid weight
        if (goalText.isEmpty()) {
            Toast.makeText(this, "Please enter a goal weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read the goal weight from the input field and save it to the database
        double goalWeight = Double.parseDouble(goalText);
        database.setGoalWeight(userID, goalWeight);

        // Show a toast message confirming that the goal weight has been saved
        Toast.makeText(this, "Goal weight set.", Toast.LENGTH_SHORT).show();
        loadWeights();
    }

    // Method to add a new weight entry to the database and update the grid display
    private void addWeightEntry() {
        String date = dateInput.getText().toString().trim();
        String weightText = weightInput.getText().toString().trim();

        // if either the date or weight input is empty when the button is clicked, show a toast message
        // asking the user to enter the date and weight.
        if (date.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "Please enter both a date and weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        // convert the weight input to a double and save the new weight entry to the database
        double weight = Double.parseDouble(weightText);

        // Check if the user is adding a new weight entry and add new entry to the database
        if (selectedWeightID == -1) {
            long result = database.addWeight(userID, date, weight);

            // Confirm the entry was added and send a toast message to user
            if (result != -1) {
                Toast.makeText(this, "Weight entry added", Toast.LENGTH_SHORT).show();
                // Check if the new weight meets the goal weight.
                checkGoalAlert(weight);
            }


        } else {
            // Update the selected weight entry in the database, send toast message, and check
            // if the updated weight meets the goal weight.
            database.updateWeight(selectedWeightID, date, weight);
            Toast.makeText(this, "Weight entry updated", Toast.LENGTH_SHORT).show();
            checkGoalAlert(weight);

            // Reset the edit mode and return the button to the add mode.
            selectedWeightID = -1;
            addWeightButton.setText("Add Weight Entry");
        }

        // Clear the input fields.  Refresh displayed weights
        dateInput.setText("");
        weightInput.setText("");
        loadWeights();
    }

    // Method to check if the user's current weight meets or exceeds their goal weight and send an alert if necessary
    // Method to check if the user's current weight meets their goal weight
    private void checkGoalAlert(double currentWeight) {
        double goalWeight = database.getGoalWeight(userID);

        // Only continue if a goal weight has been saved and the user has reached the goal
        if (goalWeight > 0 && currentWeight <= goalWeight) {

            // Only send an SMS if the user enabled SMS notifications
            if (database.notificationsEnabled(userID)) {
                sendGoalSMS();
            }
        }
    }

    // Method to send an SMS message when the user reaches their goal weight
    private void sendGoalSMS() {
        String phoneNumber = "5554";
        String message = "Way to go!!! You have reached your goal weight! We knew you could do it!";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "SMS Failed to Send", Toast.LENGTH_SHORT).show();
        }
    }


    // Method to load the user's weight entries from the database and display them in a grid layout
    private void loadWeights() {

        // Clear existing views in the weight grid container to avoid duplicates
        weightGridContainer.removeAllViews();

        // Get weight entries and goal weight for current user
        Cursor cursor = database.getWeights(userID);
        double goalWeight = database.getGoalWeight(userID);

        // Create a row for in the grid for each weight entry in the database
        while (cursor.moveToNext()) {
            // Get the weight entry data from the database
            int weightID = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));

            // Create horizontal row to display weight entry data
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setMinimumHeight(60);

            // Create cells for the date, weight and goal weight
            TextView dateView = makeCell(date);
            TextView weightView = makeCell(String.valueOf(weight));
            TextView goalView = makeCell(goalWeight > 0 ? String.valueOf(goalWeight) : "None");

            //create a button to delete the weight entry
            Button deleteButton = new Button(this);
            deleteButton.setText("X");
            deleteButton.setOnClickListener(v -> {

                // Remove the weight entry from the database and refresh the grid display
                database.deleteWeight(weightID);
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
                loadWeights();
            });

            // Add all views to the row
            row.addView(dateView);
            row.addView(weightView);
            row.addView(goalView);
            row.addView(deleteButton);

            // Allow the user to click on a row to edit the weight entry
            row.setOnClickListener(v -> {

                // Load the selected weight entry data into the input fields
                selectedWeightID = weightID;
                dateInput.setText(date);
                weightInput.setText(String.valueOf(weight));

                // Change the button text to indicate that the user is in edit mode
                addWeightButton.setText("Update Entry");

                //toast message to user indicating they are in edit mode
                Toast.makeText(this, "You can now update this entry.", Toast.LENGTH_SHORT).show();
            });

            // Add the row to the weight grid
            weightGridContainer.addView(row);
        }

        cursor.close();
    }

    // Create and format TextView cells for the weight grid display
    private TextView makeCell(String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(16);

        // Set layout parameters for the cell to ensure it takes up equal space in the row
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                60,
                1
        );

        cell.setLayoutParams(params);
        return cell;
    }
}