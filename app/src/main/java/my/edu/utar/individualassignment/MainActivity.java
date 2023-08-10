package my.edu.utar.individualassignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText etTotalBill, etNumPeople, etIndividualAmount, etPercentage;
    private LinearLayout customOptionsLayout;
    private RadioGroup rgMethod, rgCustomOptions;
    private RadioButton rbEqual, rbCustom, rbPercentage, rbIndividual;
    private Button btnCalculate, btnStore, btnShare, btnShowResults;
    private TextView tvResult, tvIndividualAmount, tvPercentage;

    // Other constants and variables
    private static final String PREF_NAME = "BreakdownResultsPref";
    private static final String KEY_TOTAL_AMOUNT = "total_amount";
    private static final String KEY_NUM_PEOPLE = "num_people";
    private static final String KEY_BREAKDOWN = "breakdown";
    private static final String KEY_RESULT_INDEX = "result_index";
    private double[] breakdown = new double[0];
    private int latestResultIndex = -1;
    private double totalAmount;
    private int numPeople;
    //private double[] individualValues;
    private boolean isCustomBreakdown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        etTotalBill = findViewById(R.id.etTotalBill);
        etNumPeople = findViewById(R.id.etNumPeople);
        etIndividualAmount = findViewById(R.id.etIndividualAmount);
        etPercentage = findViewById(R.id.etPercentage);
        rgMethod = findViewById(R.id.rgMethod);
        rbEqual = findViewById(R.id.rbEqual);
        rbCustom = findViewById(R.id.rbCustom);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnStore = findViewById(R.id.btnStore);
        btnShare = findViewById(R.id.btnShare);
        btnShowResults = findViewById(R.id.btnShowResults);
        tvResult = findViewById(R.id.tvResult);
        customOptionsLayout = findViewById(R.id.customOptions);
        rgCustomOptions = findViewById(R.id.rgCustomOptions);
        rbIndividual = findViewById(R.id.rbIndividual);
        rbPercentage = findViewById(R.id.rbPercentage);
        tvIndividualAmount = findViewById(R.id.tvIndividualAmount);
        tvPercentage = findViewById(R.id.tvPercentage);

        // Set "Equal Break-Down" as the default selection
        rbEqual.setChecked(true);
        etIndividualAmount.setVisibility(View.GONE);
        tvIndividualAmount.setVisibility(View.GONE);
        tvPercentage.setVisibility(View.GONE);
        customOptionsLayout.setVisibility(View.GONE);

        // Set up listeners
        setupListeners();
    }

    // Set up listeners for buttons and radio groups
    private void setupListeners() {
        btnCalculate.setOnClickListener(v -> calculateBreakdown());

        btnStore.setOnClickListener(v -> {
            if (isCustomBreakdown && totalAmount > 0 && numPeople > 0) {
                if (breakdown != null) {
                    storeResults(breakdown, totalAmount, numPeople);
                } else {
                    Toast.makeText(MainActivity.this, "Calculate custom breakdown first.", Toast.LENGTH_SHORT).show();
                }
            } else if (!isCustomBreakdown && totalAmount > 0 && numPeople > 0) {
                // For equal breakdown, the breakdown array is already filled with equal amounts
                storeResults(breakdown, totalAmount, numPeople);
            } else {
                Toast.makeText(MainActivity.this, "Calculate and select a custom breakdown first.", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowResults.setOnClickListener(v -> showStoredResults());

        btnShare.setOnClickListener(v -> shareResults());

        rgMethod.setOnCheckedChangeListener((group, checkedId) -> handleRadioGroupChange(checkedId));

        rgCustomOptions.setOnCheckedChangeListener((group, checkedId) -> handleCustomOptionsChange(checkedId));

        rbIndividual.setOnClickListener(v -> calculateCustomBreakdownByIndividualValues());

        rbPercentage.setOnClickListener(v -> calculateCustomBreakdownByPercentage());

    }

    // Handle radio group change
    private void handleRadioGroupChange(int checkedId) {
        if (checkedId == R.id.rbEqual) {
            etIndividualAmount.setVisibility(View.GONE);
            customOptionsLayout.setVisibility(View.GONE);
            etPercentage.setVisibility(View.GONE);
            tvIndividualAmount.setVisibility(View.GONE);
            tvPercentage.setVisibility(View.GONE);
        } else if (checkedId == R.id.rbCustom) {
            customOptionsLayout.setVisibility(View.VISIBLE);
            etIndividualAmount.setVisibility(View.GONE);
            etPercentage.setVisibility(View.GONE);
        }
    }

    // Handle custom options change
    private void handleCustomOptionsChange(int checkedId) {
        if (checkedId == R.id.rbIndividual) {
            etIndividualAmount.setVisibility(View.VISIBLE);
            etPercentage.setVisibility(View.GONE);
            tvIndividualAmount.setVisibility(View.VISIBLE);
            tvPercentage.setVisibility(View.GONE);
        } else if (checkedId == R.id.rbPercentage) {
            etIndividualAmount.setVisibility(View.GONE);
            etPercentage.setVisibility(View.VISIBLE);
            tvPercentage.setVisibility(View.VISIBLE);
            tvIndividualAmount.setVisibility(View.GONE);
        }
    }

    // Handle custom option selected
    private void handleCustomOptionSelected() {
        isCustomBreakdown = true;
        int selectedCustomOptionId = rgCustomOptions.getCheckedRadioButtonId();
        if (selectedCustomOptionId == R.id.rbIndividual) {
            calculateCustomBreakdownByIndividualValues();
        } else if (selectedCustomOptionId == R.id.rbPercentage) {
            calculateCustomBreakdownByPercentage();
        }
    }

    // Calculate the breakdown based on user input
    private void calculateBreakdown() {
        // Close the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Retrieve input values
        try {
            totalAmount = Double.parseDouble(etTotalBill.getText().toString());
            numPeople = Integer.parseInt(etNumPeople.getText().toString());
        } catch (NumberFormatException e) {
            showToast("Please enter valid values.");
            return;
        }

        breakdown = new double[numPeople];
        if (rbEqual.isChecked()) {
            // Calculate and display equal breakdown
            calculateEqualBreakdown(totalAmount, numPeople);
        } else if (rbCustom.isChecked()) {
            // Calculate custom breakdown
            handleCustomOptionSelected();
        } else {
            showToast("Please select a breakdown option.");
        }
    }

    // Calculate equal breakdown and display
    private void calculateEqualBreakdown(double totalAmount, int numPeople) {
        isCustomBreakdown = false;
        double equalAmount = totalAmount / numPeople;
        displayBreakdownResult(equalAmount);
    }

    private double[] calculateCustomBreakdownByIndividualValues() {
        // Get the total bill amount and individual values input as strings
        String totalBillStr = etTotalBill.getText().toString().trim();
        String individualValuesStr = etIndividualAmount.getText().toString().trim();
        breakdown = new double[numPeople];
        if (!totalBillStr.isEmpty() && !individualValuesStr.isEmpty()) {
            try {
                // Parse the total bill amount from string to double
                double totalBillAmount = Double.parseDouble(totalBillStr);


                // Split the individual values using commas
                String[] individualValuesArray = individualValuesStr.split(",");

                // Validate the number of individual values matches the number of people
                int numPeople = individualValuesArray.length;
                double sumIndividualValues = 0.0;
                for (String value : individualValuesArray) {
                    sumIndividualValues += Double.parseDouble(value);
                }

                if (numPeople > 0 && sumIndividualValues == totalBillAmount) {
                    // Calculate and show the breakdown when the sum of individual values matches the total bill
                    String resultMessage = "Total Bill Amount: RM " + etTotalBill.getText().toString() + "\n"
                            + "Total Number of People: " + numPeople + "\n\n";

                    // Calculate and show the breakdown for each person
                    for (int i = 0; i < numPeople; i++) {
                        double individualAmount = Double.parseDouble(individualValuesArray[i]);
                        breakdown[i] = Double.parseDouble(individualValuesArray[i]);
                        resultMessage += "Person " + (i + 1) + " should pay: RM " + String.format(Locale.getDefault(), "%.2f", individualAmount) + "\n";
                    }
                    tvResult.setText(resultMessage);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bill Breakdown(individual values)");
                    builder.setMessage(resultMessage);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                    return breakdown;
                } else {
                    // Show an error message if the number of individual values or the sum doesn't match the total bill
                    showToast("Invalid individual values. Please enter valid amounts.");
                }
            } catch (NumberFormatException e) {
                showToast("Invalid input. Please enter valid numbers.");
            }
        } else {
            showToast("Please enter total bill amount and individual values.");
        }
        return null;
    }

    private double[] calculateCustomBreakdownByPercentage() {
        // Get the total bill amount and individual percentages input as strings
        String totalBillStr = etTotalBill.getText().toString().trim();
        String individualPercentagesStr = etPercentage.getText().toString().trim();
        breakdown = new double[numPeople];

        if (!totalBillStr.isEmpty() && !individualPercentagesStr.isEmpty()) {
            try {
                // Parse the total bill amount from string to double
                double totalBillAmount = Double.parseDouble(totalBillStr);

                // Split the individual percentages using commas
                String[] individualPercentagesArray = individualPercentagesStr.split(",");

                // Validate the number of individual percentages matches the number of people
                int numPeople = individualPercentagesArray.length;
                double sumPercentages = 0.0;
                for (String percentage : individualPercentagesArray) {
                    sumPercentages += Double.parseDouble(percentage);
                }

                if (numPeople > 0 && sumPercentages == 100.0) {
                    String result = "Total Bill Amount: RM " + etTotalBill.getText().toString() + "\n"
                            + "Total Number of People: " + numPeople + "\n\n";

                    String resultMessage ="";
                    for (String percentage : individualPercentagesArray) {
                        resultMessage = "";
                        // Calculate and show the breakdown for each person
                        for (int i = 0; i < numPeople; i++) {

                            double individualPercentage = Double.parseDouble(individualPercentagesArray[i]);
                            double individualAmount = (individualPercentage / 100.0) * totalBillAmount;

                            breakdown[i] = (individualPercentage / 100.0) * totalBillAmount;
                            resultMessage += "Person " + (i + 1) + " (" + individualPercentage + "%) should pay: RM " +
                                    String.format(Locale.getDefault(), "%.2f", individualAmount) + "\n";
                        }

                    }
                    result+= resultMessage;
                    tvResult.setText(result);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bill Breakdown(Percentage)");
                    builder.setMessage(result);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                    return breakdown;
                } else {
                    // Show an error message if the number of individual percentages or the sum doesn't equal 100%
                    showToast("Invalid individual percentages. Please enter valid percentages (sum must be 100%).");
                }
            } catch (NumberFormatException e) {
                showToast("Invalid input. Please enter valid numbers.");
            }
        } else {
            showToast("Please enter total bill amount and individual percentages.");
        }
        return null;
    }

    // Store breakdown results
    private void storeResults(double[] breakdown, double totalAmount, int numPeople) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (breakdown.length > 0) {
            StringBuilder breakdownString = new StringBuilder();
            for (double value : breakdown) {
                breakdownString.append(value).append(",");
            }
            breakdownString.deleteCharAt(breakdownString.length() - 1);

            editor.putString(PREF_NAME, "BreakdownResultsPref");
            editor.putString(KEY_TOTAL_AMOUNT, String.valueOf(totalAmount));
            editor.putString(KEY_NUM_PEOPLE, String.valueOf(numPeople));
            editor.putString(KEY_BREAKDOWN, breakdownString.toString());
            editor.putInt(KEY_RESULT_INDEX, latestResultIndex);

            editor.apply();

            showToast("Breakdown results stored.");
        } else {
            showToast("No breakdown results to store.");
        }
    }

    // Show stored breakdown results
    private void showStoredResults() {
        SharedPreferences preferences = getSharedPreferences("BreakdownResultsPref", 0);

        String storedTotalAmount = preferences.getString(KEY_TOTAL_AMOUNT, "");
        String storedNumPeople = preferences.getString(KEY_NUM_PEOPLE, "");
        String storedBreakdown = preferences.getString(KEY_BREAKDOWN, "");

        if (!storedTotalAmount.isEmpty() && !storedNumPeople.isEmpty() && !storedBreakdown.isEmpty()) {
            // Convert the stored breakdown string into an array of doubles
            String[] breakdownValuesStr = storedBreakdown.split(",");
            double[] storedBreakdownValues = new double[breakdownValuesStr.length];
            for (int i = 0; i < breakdownValuesStr.length; i++) {
                storedBreakdownValues[i] = Double.parseDouble(breakdownValuesStr[i]);
            }

            // Create a formatted message for displaying stored results
            String resultMessage = "Stored Breakdown Result:\n"
                    + "Total Amount: RM " + storedTotalAmount + "\n"
                    + "Number of People: " + storedNumPeople + "\n\n"
                    + "Breakdown:\n";

            for (int i = 0; i < storedBreakdownValues.length; i++) {
                resultMessage += "Person " + (i + 1) + ": RM " + storedBreakdownValues[i] + "\n";
            }

            // Display the stored results using a dialog or any other UI element
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stored Breakdown Result");
            builder.setMessage(resultMessage);
            builder.setPositiveButton("OK", null);
            builder.show();
        } else {
            showToast("No stored breakdown results found.");
        }
    }

    // Share breakdown results
    private void shareResults() {
        if (breakdown.length > 0) {
            StringBuilder breakdownString = new StringBuilder();
            for (double value : breakdown) {
                breakdownString.append(value).append(",");
            }
            breakdownString.deleteCharAt(breakdownString.length() - 1);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Bill Breakdown Results");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Total Amount: " + totalAmount + "\n"
                    + "Number of People: " + numPeople + "\n"
                    + "Breakdown: " + breakdownString.toString());

            startActivity(Intent.createChooser(shareIntent, "Share breakdown results"));
        } else {
            showToast("No breakdown results to share.");
        }
    }

    // Display breakdown result in the UI
    private void displayBreakdownResult(double individualAmount) {
        String resultMessage = "Each person should pay: RM " + String.format(Locale.getDefault(), "%.2f", individualAmount);
        tvResult.setText(resultMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bill Breakdown");
        builder.setMessage(resultMessage);
        builder.setPositiveButton("OK", null);
        builder.show();
        Arrays.fill(breakdown, individualAmount);
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
