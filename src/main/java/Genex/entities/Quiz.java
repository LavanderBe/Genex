package Genex.entities;

public class Quiz {
    private int id;
    private int tutorialId;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctAnswer;

    // For display: linked tutorial title
    private String tutorialTitle;

    public Quiz() {}

    public Quiz(int tutorialId, String question, String optionA, String optionB, String optionC, String optionD, char correctAnswer) {
        this.tutorialId = tutorialId;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }

    public Quiz(int id, int tutorialId, String question, String optionA, String optionB, String optionC, String optionD, char correctAnswer) {
        this.id = id;
        this.tutorialId = tutorialId;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTutorialId() { return tutorialId; }
    public void setTutorialId(int tutorialId) { this.tutorialId = tutorialId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public char getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(char correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getTutorialTitle() { return tutorialTitle; }
    public void setTutorialTitle(String tutorialTitle) { this.tutorialTitle = tutorialTitle; }

    @Override
    public String toString() {
        return "Quiz{id=" + id + ", tutorialId=" + tutorialId + ", question='" + question + "'}";
    }
}