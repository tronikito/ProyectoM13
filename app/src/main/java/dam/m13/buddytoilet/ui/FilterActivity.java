package dam.m13.buddytoilet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;

public class FilterActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private UpdateSeekBarProgress seekBarProgress;

    private List<String> listFields;
    private Drawable backgroundNoSelected;
    private Drawable backgroundSelected;
    private Drawable backgroundDisable;
    private User currentUser;
    private Switch switchBtn;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        switchBtn = findViewById(R.id.switchBtn);
        LinearLayout seekbarDistance = findViewById(R.id.seekbarDistance);
        LinearLayout applyButton = findViewById(R.id.applyButton);
        SeekBar seekBar = findViewById(R.id.seekBar);
        listFields = Arrays.asList(getResources().getStringArray(R.array.filter_values));

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.filter_data), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        backgroundNoSelected = getDrawable(getResources().getIdentifier("b_button1", "drawable", getPackageName()));
        backgroundSelected = getDrawable(getResources().getIdentifier("b_button2", "drawable", getPackageName()));
        backgroundDisable = getDrawable(getResources().getIdentifier("b_button4", "drawable", getPackageName()));

        String filterState = sharedPreferences.getString(getResources().getString(R.string.filter),
                getResources().getString(R.string.false_value));

        activateButtons();
        if (filterState.matches(getResources().getString(R.string.true_value))) {
            switchBtn.setChecked(true);
            activateButtons();
        } else {
            switchBtn.setChecked(false);
            disableButtons();
        }

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchBtn.setChecked(!switchBtn.isChecked());
                switchBtn.toggle();

                if (switchBtn.isChecked()) {
                    activateButtons();
                } else
                    disableButtons();

                sharedPreferences.edit().putString(
                        getResources().getString(R.string.filter), switchBtn.isChecked() ?
                                getResources().getString(R.string.true_value) :
                                getResources().getString(R.string.false_value))
                        .apply();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        seekBar.setMax(100);
        seekBar.setMin(25);

        /** start loading options **/

        String distanceSaved = sharedPreferences.getString(getResources().getString(R.string.distance), "1");
        int distance = 55;
        if (distanceSaved.equals("1")) {
            distance = 35;
            seekBar.setProgress(35);
        } else if (distanceSaved.equals("2")) {
            distance = 55;
            seekBar.setProgress(55);
        } else if (distanceSaved.equals("3")) {
            distance = 100;
            seekBar.setProgress(100);
        }

        int widthBar = (int) Math.ceil((width / 100) * (distance + 3));
        ViewGroup.LayoutParams layoutParams = seekbarDistance.getLayoutParams();
        layoutParams.width = widthBar;
        seekbarDistance.setLayoutParams(layoutParams);

        if (retrieveIntent()) {
            applyButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    buttonPushEffect(v).addListener(new AnimatorListenerAdapter() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            Intent i = new Intent(FilterActivity.this, NavigationActivity.class);
                            i.putExtra(getResources().getString(R.string.user_intent), currentUser);
                            startActivity(i);
                        }
                    });
                    Toast.makeText(FilterActivity.this, getResources().getString(R.string.filters_applied), Toast.LENGTH_SHORT).show();
                }
            });
        }

        for (int x = 0; x < listFields.size(); x++) {

            String nameField = listFields.get(x);
            int yesResource = getResources().getIdentifier("yes" + nameField, "id", getPackageName());
            TextView yes = findViewById(yesResource);
            int noResource = getResources().getIdentifier("no" + nameField, "id", getPackageName());
            TextView no = findViewById(noResource);

            //listeners
            yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {

                    if (sharedPreferences.getString(getResources().getString(R.string.filter), getResources().getString(R.string.false_value)).equals("true")) {

                        editor.putString(nameField, "true").apply();
                        yes.setBackground(backgroundSelected);
                        no.setBackground(backgroundNoSelected);

                        buttonPushEffect(yes);
                    }

                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {

                    if (sharedPreferences.getString(getResources().getString(R.string.filter), getResources().getString(R.string.false_value)).equals("true")) {

                        editor.putString(nameField, "false").apply();
                        yes.setBackground(backgroundNoSelected);
                        no.setBackground(backgroundSelected);

                        buttonPushEffect(no);
                    }
                }
            });
        }

        /** activate seekbar animation **/

        seekBarProgress = new UpdateSeekBarProgress(seekBar, seekbarDistance, width);
        seekBarProgress.run();
    }

    private void activateButtons() {

        for (int x = 0; x < listFields.size(); x++) {

            String nameField = listFields.get(x);

            int yesResource = getResources().getIdentifier("yes" + nameField, "id", getPackageName());
            TextView yes = findViewById(yesResource);
            int noResource = getResources().getIdentifier("no" + nameField, "id", getPackageName());
            TextView no = findViewById(noResource);

            if (sharedPreferences.getString(nameField, getResources().getString(R.string.false_value)).equals("true")) {
                yes.setBackground(backgroundSelected);
                no.setBackground(backgroundNoSelected);
            } else {
                yes.setBackground(backgroundNoSelected);
                no.setBackground(backgroundSelected);
            }
        }
    }

    private void disableButtons() {

        for (int x = 0; x < listFields.size(); x++) {

            String nameField = listFields.get(x);

            if (!nameField.equals(getResources().getString(R.string.filter))) {

                int yesResource = getResources().getIdentifier("yes" + nameField, "id", getPackageName());
                TextView yes = findViewById(yesResource);
                int noResource = getResources().getIdentifier("no" + nameField, "id", getPackageName());
                TextView no = findViewById(noResource);

                if (sharedPreferences.getString(nameField, getResources().getString(R.string.false_value)).equals("true")) {
                    yes.setBackground(backgroundDisable);
                    no.setBackground(backgroundDisable);
                } else {
                    yes.setBackground(backgroundDisable);
                    no.setBackground(backgroundDisable);
                }
            }
        }
    }

    private AnimatorSet buttonPushEffect(View buttonEffect) {

        AnimatorSet animationSet = new AnimatorSet();
        float valueFrom = 1f;
        float valueTo = 0.9f;
        ObjectAnimator buttonEffect1 = ObjectAnimator.ofFloat(buttonEffect, "scaleX", valueFrom, valueTo);
        ObjectAnimator buttonEffect2 = ObjectAnimator.ofFloat(buttonEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setDuration(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();

        animationSet = new AnimatorSet();
        valueFrom = 0.9f;
        valueTo = 1f;
        buttonEffect1 = ObjectAnimator.ofFloat(buttonEffect, "scaleX", valueFrom, valueTo);
        buttonEffect2 = ObjectAnimator.ofFloat(buttonEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setStartDelay(250);
        buttonEffect1.setDuration(250);
        buttonEffect2.setStartDelay(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();

        return animationSet;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        seekBarProgress.stopSeekBar();
    }

    public static int UPDATE_SEEKBAR = 10;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public class UpdateSeekBarProgress implements Runnable {

        private final SeekBar seekBar;
        private final LinearLayout seekBarLayout;
        private final int widthScreen;
        private boolean continueSeekbar = true;
        TextView distanceText;
        private SharedPreferences.Editor editor = sharedPreferences.edit();

        public UpdateSeekBarProgress(SeekBar seekBar, LinearLayout seekBarLayout, int widthScreen) {
            this.seekBar = seekBar;
            this.seekBarLayout = seekBarLayout;
            this.widthScreen = widthScreen;
            this.distanceText = findViewById(R.id.distance);
        }

        public void stopSeekBar() {
            continueSeekbar = false;
        }

        @Override
        public void run() {

            if (!seekBar.hasFocus()) {
                int distance = seekBar.getProgress();
                String distanceString = getResources().getString(R.string.distance);
                String resultDistance = "0KM";

                if (distance <= 35) {
                    editor.putString(distanceString, "1").apply();
                    resultDistance = "1 km";
                } else if (distance > 35 && distance < 75) {
                    resultDistance = "2 km";
                    editor.putString(distanceString, "2").apply();
                } else if (distance >= 75) {
                    resultDistance = "3 km";
                    editor.putString(distanceString, "3").apply();
                }

                distanceText.setText(resultDistance);
                int width = (int) Math.ceil((widthScreen / 100) * (distance + 3));
                ViewGroup.LayoutParams layoutParams = seekBarLayout.getLayoutParams();
                layoutParams.width = width;
                seekBarLayout.setLayoutParams(layoutParams);

                if (continueSeekbar) {
                    handler.postDelayed(this, UPDATE_SEEKBAR);
                }
            }
        }
    }

    private boolean retrieveIntent() {
        Intent infoIntent = getIntent();
        if (infoIntent.hasExtra(getResources().getString(R.string.user_intent)))
            currentUser = (User) infoIntent.getSerializableExtra(getResources().getString(R.string.user_intent));
        return currentUser != null;
    }
}