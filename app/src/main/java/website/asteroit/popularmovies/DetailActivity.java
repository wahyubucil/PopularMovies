package website.asteroit.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            int id = intentThatStartedThisActivity.getExtras().getInt("id");
            String title = intentThatStartedThisActivity.getExtras().getString("title");
            String poster = intentThatStartedThisActivity.getExtras().getString("poster");
            double rating = intentThatStartedThisActivity.getExtras().getDouble("rating");

            DetailFragment detailFragment = DetailFragment.newInstance(id, title, poster, rating);
            getFragmentManager().beginTransaction().replace(R.id.fragment_detail_container, detailFragment)
                    .addToBackStack(null).commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
