package com.example.happyhour;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText searchBar;
    private ImageView cocktailImage;
    private TextView cocktailName, cocktailIngredients, cocktailInstructions, defaultText, ingredientsHeader, instructionsHeader;
    private Button searchButton;
    private Button randomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views
        searchBar = findViewById(R.id.searchBar);
        cocktailImage = findViewById(R.id.cocktailImage);
        cocktailName = findViewById(R.id.cocktailName);
        cocktailIngredients = findViewById(R.id.cocktailIngredients);
        cocktailInstructions = findViewById(R.id.cocktailInstructions);
        defaultText = findViewById(R.id.defaultText);
        ingredientsHeader = findViewById(R.id.ingredientsHeader);
        instructionsHeader = findViewById(R.id.instructionsHeader);
        searchButton = findViewById(R.id.searchButton);
        randomButton = findViewById(R.id.randomButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCocktail(searchBar.getText().toString().trim());
            }
        });

        // Set an onEditorActionListener for the searchBar
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCocktail(searchBar.getText().toString().trim());
                return true;
            }
            return false;
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRandomCocktail();
            }
        });



    }

    private void searchCocktail(String cocktailName) {
        new GetCocktailInfo().execute(cocktailName);
    }

    private void fetchRandomCocktail() {
        new GetCocktailInfo(true).execute();
    }

    private class GetCocktailInfo extends AsyncTask<String, Void, JSONObject> {
        private boolean isRandom = false;

        public GetCocktailInfo() {
        }

        public GetCocktailInfo(boolean isRandom) {
            this.isRandom = isRandom;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            String url;

            if(isRandom) {
                url = "https://www.thecocktaildb.com/api/json/v1/1/random.php";
            } else {
                String cocktailName = params[0];
                url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + cocktailName;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                Log.d("API_RESPONSE", jsonData);
                return new JSONObject(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject == null) {
                    defaultText.setText("An error occurred while fetching data!");
                    return;
                }

                JSONArray drinks = jsonObject.optJSONArray("drinks");
                if (drinks != null && drinks.length() > 0) {
                    JSONObject drink = drinks.getJSONObject(0);

                    String strDrink = drink.optString("strDrink");
                    String strInstructions = drink.optString("strInstructions");
                    String strDrinkThumb = drink.optString("strDrinkThumb");

                    List<String> ingredientsList = new ArrayList<>();

                    for (int i = 1; i <= 15; i++) {
                        String ingredient = drink.optString("strIngredient" + i, null);
                        String measure = drink.optString("strMeasure" + i, null);

                        Log.d("DEBUG", "Ingredient: " + ingredient + ", Measure: " + measure);

                        if (ingredient == "null" || ingredient.trim().isEmpty()) {
                            break;
                        }

                        if (measure == "null" || measure == null || measure.trim().isEmpty()) {
                            ingredientsList.add(ingredient.trim());
                        } else {
                            ingredientsList.add(measure.trim() + " " + ingredient.trim());
                        }
                    }

                    StringBuilder ingredientsStrBuilder = new StringBuilder();
                    for (String ing : ingredientsList) {
                        ingredientsStrBuilder.append("• ").append(ing).append("\n");
                    }

                    // Set the data to views
                    Picasso.get().load(strDrinkThumb).into(cocktailImage);
                    cocktailName.setText(strDrink);
                    cocktailIngredients.setText(ingredientsStrBuilder.toString());
                    cocktailInstructions.setText(strInstructions);

                    // Set visibility
                    defaultText.setVisibility(View.GONE);
                    cocktailImage.setVisibility(View.VISIBLE);
                    cocktailName.setVisibility(View.VISIBLE);
                    cocktailIngredients.setVisibility(View.VISIBLE);
                    cocktailInstructions.setVisibility(View.VISIBLE);
                    ingredientsHeader.setVisibility(View.VISIBLE);
                    instructionsHeader.setVisibility(View.VISIBLE);

                } else {
                    defaultText.setText("No cocktail found!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                defaultText.setText("An error occurred!");
                ingredientsHeader.setVisibility(View.GONE);
                instructionsHeader.setVisibility(View.GONE);
            }
        }
    }
}
