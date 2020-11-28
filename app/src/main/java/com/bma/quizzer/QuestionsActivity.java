package com.bma.quizzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {
    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUEST";

    private Button sharebutton, nextbutton;
    private TextView correctOption;
    private FloatingActionButton bookmarkbutton;
    private TextView questions, noindicator;
    private LinearLayout optionContainer;
    private int count = 0;
    private List<QuestionModel> questionModelList;
    private int postion = 0;
    private int score = 0;

    private String category;
    private int setNo;

    private Dialog loadingDialog;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();

    private List<QuestionModel> bookmarkslist;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    private int matchedQuestionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        optionContainer = findViewById(R.id.linearLayout2);
        sharebutton = findViewById(R.id.sharebutton);
        nextbutton = findViewById(R.id.nextbutton);
        bookmarkbutton = findViewById(R.id.bookmarkbutton);
        questions = findViewById(R.id.questions);
        noindicator = findViewById(R.id.noOfQuestion);

        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();
        bookmarkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modelMatcher()){
                    bookmarkslist.remove(matchedQuestionPosition);
                    bookmarkbutton.setImageDrawable(getDrawable(R.drawable.ic_action_bookmarks));
                }else {
                    bookmarkslist.add(questionModelList.get(postion));
                    bookmarkbutton.setImageDrawable(getDrawable(R.drawable.ic_action_name));
                }
            }
        });

        category = getIntent().getStringExtra("category");
        setNo = getIntent().getIntExtra("setNo", 1);

        questionModelList = new ArrayList<>();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);


        loadingDialog.show();
        reference.child("SETS")
                .child(category)
                .child("questions")
                .orderByChild("setNo")
                .equalTo(setNo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                            questionModelList.add(snapshot1.getValue(QuestionModel.class));
                        }
                        if (questionModelList.size() > 0) {
                            for (int i = 0; i < 4; i++) {
                                optionContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        checkAnswer((TextView) v);
                                    }
                                });
                            }

                            playAnim(questions, 0, questionModelList.get(postion).getQuestion());
                            nextbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    nextbutton.setEnabled(false);
                                    nextbutton.setAlpha(0.10f);
                                    enableOption(true);
                                    postion++;
                                    if (postion == questionModelList.size()) {
                                        Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                                        scoreIntent.putExtra("score", score);
                                        scoreIntent.putExtra("total", questionModelList.size());
                                        startActivity(scoreIntent);
                                        finish();
                                        return;
                                    }
                                    count = 0;
                                    playAnim(questions, 0, questionModelList.get(postion).getQuestion());
                                }
                            });

                            sharebutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String body = questionModelList.get(postion).getQuestion()+ "\n" +
                                            questionModelList.get(postion).getOptionA()+ "\n"+
                                            questionModelList.get(postion).getOptionB()+ "\n" +
                                            questionModelList.get(postion).getOptionC()+ "\n" +
                                            questionModelList.get(postion).getOptionD();
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer Challenge");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                                    startActivity(Intent.createChooser(shareIntent,"Share Via"));
                                }
                            });

                        } else {
                            finish();
                            Toast.makeText(QuestionsActivity.this, "No Questions", Toast.LENGTH_SHORT).show();

                        }
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Toast.makeText(QuestionsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        finish();
                    }
                });


    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnim(View view, int value, final String data) {
        view.animate().alpha(value).scaleX(value)
                .scaleY(value)
                .setDuration(500)
                .setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (value == 0 && count < 4) {
                            String option = "";
                            if (count == 0) {
                                option = questionModelList.get(postion).getOptionA();
                            } else if (count == 1) {
                                option = questionModelList.get(postion).getOptionB();
                            } else if (count == 2) {
                                option = questionModelList.get(postion).getOptionC();
                            } else if (count == 3) {
                                option = questionModelList.get(postion).getOptionD();
                            }
                            playAnim(optionContainer.getChildAt(count), 0, option);
                            count++;
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (value == 0) {
                            try {
                                noindicator.setText(postion + 1 + "/" + questionModelList.size());
                                ((TextView) view).setText(data);
                                if (modelMatcher()){
                                    bookmarkbutton.setImageDrawable(getDrawable(R.drawable.ic_action_bookmarks));
                                }else {
                                    bookmarkbutton.setImageDrawable(getDrawable(R.drawable.ic_action_name));
                                }
                            } catch (ClassCastException exception) {
                                ((Button) view).setText(data);
                            }
                            view.setTag(data);
                            playAnim(view, 1, data);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private void checkAnswer(TextView selectedOption) {
        enableOption(false);
        nextbutton.setEnabled(true);
        nextbutton.setAlpha(1);
        if (selectedOption.getText().toString().equals(questionModelList.get(postion).getCorrectAns())) {
            //correct
            score++;
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16d904")));
        } else {
            //incorrect
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e80212")));
            correctOption = (TextView) optionContainer.findViewWithTag(questionModelList.get(postion).getCorrectAns());
            correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16d904")));

        }
    }

    private void enableOption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            optionContainer.getChildAt(i).setEnabled(enable);
            if (enable) {
                optionContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6200EE")));
            }
        }
    }

    private void getBookmarks() {

        String json = preferences.getString(KEY_NAME, "");
        Type type = new TypeToken<List<QuestionModel>>() {
        }.getType();

        bookmarkslist = gson.fromJson(json, type);

        if (bookmarkslist == null) {
            bookmarkslist = new ArrayList<>();
        }
    }

    private boolean modelMatcher() {
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookmarkslist) {

            if (model.getQuestion().equals(questionModelList.get(postion).getQuestion())
                    && model.getCorrectAns().equals(questionModelList.get(postion).getCorrectAns())
                    && model.getSetNo() == questionModelList.get(postion).getSetNo()) {
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks() {
        String json = gson.toJson(bookmarkslist);

        editor.putString(KEY_NAME, json);
        editor.commit();
    }
}