package com.epsilonv.ratingpromptdemo;

import android.app.Application;

import com.epsilonv.ratingprompt.RatingPrompt;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        final RatingPrompt.Builder builder = new RatingPrompt.Builder()
                .setPackageName(this.getPackageName())
                .setQuestionTitle("How's it going?")
                .setQuestionMessage("Are you enjoying " + getString(R.string.app_name) + "?")
                .setRatingTitle("Rate us!")
                .setRatingMessage("Since you're enjoying the app, why not rate us? It only takes"
                        + " a second & helps way more than you'd think!")
                .setNumOpensThreshold(3);
        RatingPrompt.setBuilder(builder);
        final RatingPrompt prompt = RatingPrompt.getInstance();
        prompt.onOpen(this);
    }
}
