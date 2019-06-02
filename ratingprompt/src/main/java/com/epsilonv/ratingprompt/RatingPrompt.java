package com.epsilonv.ratingprompt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;

/**
 * A utility to show a prompt to ask the user to rate the application
 * Created by jmaciak on 10/31/17.
 */

public class RatingPrompt {
    private static Builder INSTANCE_BUILDER;
    private static RatingPrompt INSTANCE;

    private final String packageName;
    private final String questionTitle;
    private final String questionMessage;
    private final String ratingTitle;
    private final String ratingMessage;
    private final int numOpensThreshold;

    private static final String PLAY_STORE_LINK_BASE = "market://details?id=";
    private static final String SHARED_PREFS_NAME = "RatingDialog";
    private static final String SHARED_PREFS_KEY_DO_SHOW = "show"; // flag to set if this dialog should ever be shown again
    private static final String SHARED_PREFS_KEY_NUM_OPENS = "num_opens";
    private static final String SHARED_PREFS_KEY_NUM_OPENS_THRES = "num_opens_thres";

    // the number of times the application has been opened
    private int numOpens;
    // whether the prompt is still active
    private boolean active;

    private RatingPrompt(final String packageName, final String questionTitle,
                         final String questionMessage, final String ratingTitle,
                         final String ratingMessage, final int numOpensThreshold) {
        this.packageName = packageName;
        this.questionTitle = questionTitle;
        this.questionMessage = questionMessage;
        this.ratingTitle = ratingTitle;
        this.ratingMessage = ratingMessage;
        this.numOpensThreshold = numOpensThreshold;
    }

    public static void setBuilder(final Builder builder) {
        INSTANCE_BUILDER = builder;
    }
    /**
     * Gets a singleton instance of the RatingPrompt. A call to setBuilder() must be made before
     * calling get instance.
     * @return the RatingPrompt singleton
     */
    public static RatingPrompt getInstance() {
        if(INSTANCE == null) {
            synchronized (RatingPrompt.class) {
                if(INSTANCE == null) {
                    INSTANCE = INSTANCE_BUILDER.build();
                }
            }
        }
        return INSTANCE;
    }

    public void onOpen(@NotNull final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME,
                Context.MODE_PRIVATE);
        if(!IsFirstRun(prefs)) {
            readSharedPreferences(prefs);
        } else {
            active = true;
        }
        numOpens++;
        writeSharedPreferences(prefs);
    }

    public void show(@NotNull final Context context) {
        if(doShowDialog() && active) {
            AlertDialog.Builder rateMeBuilder = new AlertDialog.Builder(context);
            rateMeBuilder.setTitle(ratingTitle)
                    .setMessage(ratingMessage)
                    .setPositiveButton("Yes", (dialog, which) ->
                        context.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(PLAY_STORE_LINK_BASE + packageName))))
                    .setNegativeButton("No", (dialog, which) -> {
                        // do nothing
                    });
            final AlertDialog rateMeDialog = rateMeBuilder.create();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(questionTitle)
                    .setMessage(questionMessage)
                    .setPositiveButton("Yes", (dialog, which) -> rateMeDialog.show())
                    .setNegativeButton("No", (dialog, which) -> {
                        // todo: show support email
                    }).show();
            active = false;
            SetActive(context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE), false);
        }
    }

    private boolean doShowDialog() {
        return numOpens >= numOpensThreshold;
    }

    private void readSharedPreferences(@NotNull final SharedPreferences prefs) {
        active = prefs.getBoolean(SHARED_PREFS_KEY_DO_SHOW, true);
        numOpens = prefs.getInt(SHARED_PREFS_KEY_NUM_OPENS, -1);
    }

    private void writeSharedPreferences(@NotNull final SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SHARED_PREFS_KEY_DO_SHOW, active);
        editor.putInt(SHARED_PREFS_KEY_NUM_OPENS, numOpens);
        editor.putInt(SHARED_PREFS_KEY_NUM_OPENS_THRES, numOpensThreshold);
        editor.apply();
    }

    private static boolean IsFirstRun(@NotNull final SharedPreferences prefs) {
        return (prefs.getInt(SHARED_PREFS_KEY_NUM_OPENS_THRES, -1) == -1);
    }

    private static void SetActive(@NotNull final SharedPreferences prefs, boolean active) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SHARED_PREFS_KEY_DO_SHOW, active);
        editor.apply();
    }

    public static class Builder {
        private String packageName;
        private String questionTitle;
        private String questionMessage;
        private String ratingTitle;
        private String ratingMessage;
        private int numOpensThreshold;

        public Builder setPackageName(final String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder setQuestionTitle(final String questionTitle) {
            this.questionTitle = questionTitle;
            return this;
        }

        public Builder setQuestionMessage(final String questionMessage) {
            this.questionMessage = questionMessage;
            return this;
        }

        public Builder setRatingTitle(final String ratingTitle) {
            this.ratingTitle = ratingTitle;
            return this;
        }

        public Builder setRatingMessage(final String ratingMessage) {
            this.ratingMessage = ratingMessage;
            return this;
        }

        public Builder setNumOpensThreshold(final int numOpensThreshold) {
            this.numOpensThreshold = numOpensThreshold;
            return this;
        }

        private RatingPrompt build() {
            return new RatingPrompt(packageName, questionTitle, questionMessage, ratingTitle, ratingMessage, numOpensThreshold);
        }

    }
}
