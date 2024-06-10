package com.example.fyp_ilikethatcoffee;

public class Faq {
    String Question;
    String Answer;
    String Category;

    public Faq() {
    }

    public Faq(String question, String answer, String category) {
        Question = question;
        Answer = answer;
        Category = category;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
