package bt.nhdcl.usermicroservice.service;

public interface EmailService {
    /**
     * Sends an email to the specified recipient.
     * 
     * @param to      the email address of the recipient
     * @param subject the subject of the email
     * @param text    the body content of the email
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendEmail(String to, String subject, String text);
}