package Genex.utils;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import java.io.IOException;

public class EmailSystem {
    private static final String API_KEY = "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";
    ///private static final String API_KEY = System.getenv("SENDGRID_API_KEY");
    private static String TEMPLATE_ID = "d-dce24ed71c034fe2972457aaaf380772"; // Paste your Template ID here
    private static final String FROM_EMAIL = "genexesportstn@gmail.com"; // Your verified sender

    //ALWAYS LOAD TEMPLATE ID AFTER THE PERSONALIZATION OR THE TEMPLATE WILL BE EMPTYYY

    public static void sendVerificationEmail(String recipientEmail, String firstName, String otpCode) {

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("ERROR: SendGrid API Key not found in Environment Variables.");
            return;
        }

        Email from = new Email(FROM_EMAIL);
        Email to = new Email(recipientEmail);
        String subject = "Genex: Code De Verification";

        Content content = new Content("text/html", " 6464");

        Mail mail = new Mail(from, subject, to, content);
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("verification-code", otpCode);
        personalization.addDynamicTemplateData("username", firstName);
        mail.addPersonalization(personalization);


        mail.setTemplateId(TEMPLATE_ID);
        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("--- SendGrid Log ---");
            System.out.println("Status Code: " + response.getStatusCode());
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Success: Email sent to " + recipientEmail);
            } else {
                System.out.println("Error Body: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Failed to connect to SendGrid: " + ex.getMessage());
        }
    }

    public static void SendForgotPassword(String recipientEmail, String otpCode){
        TEMPLATE_ID="d-29472afc3a244db4a17dcdcb2817fe42";
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("ERROR: SendGrid API Key not found in Environment Variables.");
            return;
        }

        Email from = new Email(FROM_EMAIL);
        Email to = new Email(recipientEmail);
        String subject = "Genex: Code De Verification";

        Content content = new Content("text/html", " 6464");

        Mail mail = new Mail(from, subject, to, content);
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("verification-code", otpCode);
        mail.addPersonalization(personalization);


        mail.setTemplateId(TEMPLATE_ID);
        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("--- SendGrid Log ---");
            System.out.println("Status Code: " + response.getStatusCode());
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Success: Email sent to " + recipientEmail);
            } else {
                System.out.println("Error Body: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Failed to connect to SendGrid: " + ex.getMessage());
        }
    }
}
