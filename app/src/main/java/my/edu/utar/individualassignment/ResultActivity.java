package my.edu.utar.individualassignment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Retrieve the breakdown results from the intent
        double[] breakdownResults = getIntent().getDoubleArrayExtra("breakdownResults");

        // Display the breakdown results in your layout (you need to have TextViews or other UI components in your layout)
        LinearLayout resultLayout = findViewById(R.id.resultLayout); // Assuming you have a LinearLayout in your layout XML
        for (int i = 0; i < breakdownResults.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(String.format("Person %d: $%.2f", i + 1, breakdownResults[i]));
            resultLayout.addView(textView);
        }
    }
}