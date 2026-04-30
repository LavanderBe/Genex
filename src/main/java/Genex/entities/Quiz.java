package Genex.entities;

public class Quiz {
    private int id;
    private int tutorial_id;
    private String question;
    private String option_a;
    private String option_b;
    private String option_c;
    private String option_d;
    private String correct_option;

    // For UI display
    private String tutorial_name;

    public Quiz() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTutorial_id() { return tutorial_id; }
    public void setTutorial_id(int tutorial_id) { this.tutorial_id = tutorial_id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOption_a() { return option_a; }
    public void setOption_a(String option_a) { this.option_a = option_a; }

    public String getOption_b() { return option_b; }
    public void setOption_b(String option_b) { this.option_b = option_b; }

    public String getOption_c() { return option_c; }
    public void setOption_c(String option_c) { this.option_c = option_c; }

    public String getOption_d() { return option_d; }
    public void setOption_d(String option_d) { this.option_d = option_d; }

    public String getCorrect_option() { return correct_option; }
    public void setCorrect_option(String correct_option) { this.correct_option = correct_option; }

    public String getTutorial_name() { return tutorial_name; }
    public void setTutorial_name(String tutorial_name) { this.tutorial_name = tutorial_name; }
}
