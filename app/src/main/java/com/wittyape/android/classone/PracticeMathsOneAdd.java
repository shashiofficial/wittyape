package com.wittyape.android.classone;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wittyape.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PracticeMathsOneAdd extends Fragment implements View.OnClickListener {

    public PracticeMathsOneAdd() {

    }

    private TextView textViewTimer;
    private TextView textViewScore;

    private int correctScore = 0;
    private int totalQuestions = 0;

    private TextView textViewQuestion;

    private ArrayList<Integer> answersList;
    private int locationOfCorrectAnswer;

    private Button buttonAnswer00;
    private Button buttonAnswer01;
    private Button buttonAnswer10;
    private Button buttonAnswer11;

    private Button buttonStartStop;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public static final String COLLECTION_NAME = "users";

    CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.practicemathsoneadd, container, false);

        initViews(view);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return view;
    }

    private void initViews(View view) {

        textViewTimer = view.findViewById(R.id.text_view_timer_add_class_one);
        textViewScore = view.findViewById(R.id.text_view_score_add_class_one);

        textViewQuestion = view.findViewById(R.id.text_view_question_add_class_one);

        buttonAnswer00 = view.findViewById(R.id.button_add_00_class_one);
        buttonAnswer01 = view.findViewById(R.id.button_add_01_class_one);
        buttonAnswer10 = view.findViewById(R.id.button_add_10_class_one);
        buttonAnswer11 = view.findViewById(R.id.button_add_11_class_one);

        buttonStartStop = view.findViewById(R.id.button_start_add_class_one);

        buttonAnswer00.setOnClickListener(this);
        buttonAnswer01.setOnClickListener(this);
        buttonAnswer10.setOnClickListener(this);
        buttonAnswer11.setOnClickListener(this);
        buttonStartStop.setOnClickListener(this);

        enableButton(false);

        answersList = new ArrayList<>();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_start_add_class_one) {

            preCountingTimer();
            setScore();
            setQuestion();
            startCountDown();

        } else {

            if (Integer.valueOf(((Button) view).getText().toString()) == answersList.get(locationOfCorrectAnswer)) {
                answerClickAnimation(view, true);

                correctScore++;
                totalQuestions++;
                setScore();

                setQuestion();
            } else {
                answerClickAnimation(view, false);

                totalQuestions++;
                setScore();

                setQuestion();
            }

        }
    }

    private void setQuestion() {

        answersList.clear();
        Random random = new Random();

        int firstNumber = random.nextInt(6);
        int secondNumber = random.nextInt(6);

        String question = "Adding : " + firstNumber + " + " + secondNumber + " =";
        textViewQuestion.setText(question);

        locationOfCorrectAnswer = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAnswer) {
                answersList.add(firstNumber + secondNumber);
            } else {
                int wrongAnswer = random.nextInt(11);

                while (wrongAnswer == (firstNumber + secondNumber))
                    wrongAnswer = random.nextInt(11);

                answersList.add(wrongAnswer);
            }

        }

        buttonAnswer00.setText(Integer.toString(answersList.get(0)));
        buttonAnswer01.setText(Integer.toString(answersList.get(1)));
        buttonAnswer10.setText(Integer.toString(answersList.get(2)));
        buttonAnswer11.setText(Integer.toString(answersList.get(3)));
    }

    private void preCountingTimer() {

        textViewTimer.setText("Timer");
        textViewQuestion.setText("Score");

        enableButton(true);

        buttonStartStop.setVisibility(View.GONE);

        correctScore = 0;
        totalQuestions = 0;

    }

    private void startCountDown() {

        countDownTimer = new CountDownTimer(10100, 1000) {

            @Override
            public void onTick(long l) {

                long timeLeft = l / 1000;

                if (timeLeft <= 5) {
                    textViewTimer.setTextColor(Color.RED);
                }

                textViewTimer.setText(String.valueOf(l / 1000) + "s");
            }

            @Override
            public void onFinish() {
                textViewTimer.setTextColor(Color.BLACK);
                postCountingTimer();
            }

        }.start();

    }

    private void postCountingTimer() {
        textViewTimer.setText("Time-up");
        textViewQuestion.setText("-");

        buttonAnswer00.setText("-");
        buttonAnswer01.setText("-");
        buttonAnswer10.setText("-");
        buttonAnswer11.setText("-");

        enableButton(false);

        buttonStartStop.setVisibility(View.VISIBLE);
        buttonStartStop.setText("Retry");

        showResult(correctScore, totalQuestions);
        updateScoreInFirestore(correctScore);

        correctScore = 0;
        totalQuestions = 0;
    }

    private void enableButton(boolean isEnable) {
        buttonAnswer00.setEnabled(isEnable);
        buttonAnswer01.setEnabled(isEnable);
        buttonAnswer10.setEnabled(isEnable);
        buttonAnswer11.setEnabled(isEnable);
    }

    private void setScore() {
        textViewScore.setText(String.valueOf(correctScore) + "/" + String.valueOf(totalQuestions));
    }

    private void showResult(int finalScore, int finalQuestions) {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View layoutView = layoutInflater.inflate(R.layout.dialog_result, null);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).setView(layoutView);
        alertDialog.setCancelable(true);
        final AlertDialog testDialog = alertDialog.create();
        testDialog.show();

        TextView textViewFinalScore = layoutView.findViewById(R.id.text_view_correct_ans_dialog);
        TextView textViewTotalQuestions = layoutView.findViewById(R.id.text_view_total_question_dialog);
        TextView textViewAccuracy = layoutView.findViewById(R.id.text_view_accuracy_dialog);

        textViewFinalScore.setText("Correct Answers : " + finalScore);
        textViewTotalQuestions.setText("Questions attempted : " + finalQuestions);

        if (finalScore == 0 || finalQuestions == 0) {
            textViewAccuracy.setText("Accuracy: 0%");
        } else {
            textViewAccuracy.setText("Accuracy: " + Integer.toString((finalScore * 100) / finalQuestions) + "%");
        }
    }

    private void updateScoreInFirestore(final int finalScore) {

        final CollectionReference collectionReference = firebaseFirestore.collection(COLLECTION_NAME);

        collectionReference
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        //Check if the document exists
                        if (documentSnapshot.exists()) {

                            String userScore = documentSnapshot.getString("score");

                            if (userScore.isEmpty()) {
                                userScore = "0";
                            }

                            int updatedScore = finalScore + Integer.parseInt(userScore);

                            Map<String, Object> data = new HashMap<>();
                            data.put("score", String.valueOf(updatedScore));

                            collectionReference
                                    .document(firebaseAuth.getCurrentUser().getUid())
                                    .update(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(getActivity(), "Score updated", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void answerClickAnimation(final View view, final boolean isCorrect) {
        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
        view.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isCorrect)
                    view.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                else
                    view.setBackgroundColor(getResources().getColor(R.color.colorRed));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setBackgroundResource(R.drawable.design_button_answer_count);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (countDownTimer != null)
            countDownTimer.cancel();
    }
}