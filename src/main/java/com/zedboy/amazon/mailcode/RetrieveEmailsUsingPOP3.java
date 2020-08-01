package com.zedboy.amazon.mailcode;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kamlesh
 */
public class RetrieveEmailsUsingPOP3 {
    private final static String YAHOOPOP3HOST = "pop.mail.yahoo.com";
    private final static String MAILRUPOP3HOST = "pop.mail.ru";
    private final static String GMAILPOP3HOST = "pop.gmail.com";
    private final static String _163POP3HOST = "pop.163.com";
    private final static String POP3PORT = "995";
    private final static String JPSUBJECT = "新しいAmazonアカウントを確認します";
    private final static String JPASUBJECT = "お客様のAmazon確認コード";
    private final static String COMSUBJECT = "Verify your new Amazon account";
    private final static String COMASUBJECT = "Amazon Authentication";
    private final static String DEASUBJECT = "Ihr Amazon-Verifizierungscode";
    private final static String COMALERTSUBJECT = "Amazon security alert: Unusual sign-in attempt detected";
    private final static String JPALERTSUBJECT = "Amazonセキュリティ警告: サインイン試行が検出されました";

    private static Properties getProperties(String host, String port, String secureCon) {
        Properties properties = new Properties();
        //---------- Server Setting---------------
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        if (secureCon.equalsIgnoreCase("ssl")) {
            properties.put("mail.smtp.ssl.enable", "true");
        } else {
            properties.put("mail.smtp.ssl.enable", "false");
        }
        //---------- SSL setting------------------
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port", String.valueOf(port));
        return properties;
    }

    private static String findVerifyCode(Message[] messages) throws MessagingException, IOException {
        for (int i = messages.length - 1; i > -1; i--) {
            Message message = messages[i];
            String subject = message.getSubject();
            String contentType = message.getContentType();
            String messageContent = "";
            if (subject.equals(COMALERTSUBJECT) || subject.equals(JPALERTSUBJECT)) {
                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        messageContent = part.getContent().toString();
                    }
                } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }
                if (StringUtils.isNotEmpty(messageContent)) {
                    Pattern pattern = Pattern.compile("<a style=\".*font-weight: 600\">\\s*(.*)\\s*</a>");
                    Matcher matcher = pattern.matcher(messageContent);
                    while (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
            // store attachment file name, separated by comma
            else if (subject.equals(DEASUBJECT) || subject.equals(JPSUBJECT) || subject.equals(JPASUBJECT) || subject.equals(COMSUBJECT) || subject.equals(COMASUBJECT)) {
                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        messageContent = part.getContent().toString();
                    }
                } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }
                if (StringUtils.isNotEmpty(messageContent)) {
                    Pattern pattern = Pattern.compile("<p class=\"otp\">(.*?)</p>");
                    Matcher matcher = pattern.matcher(messageContent);
                    while (matcher.find()) {
                        return matcher.group(1);
                    }
                }
                break;
            }
        }
        return null;
    }

    public String getAmazonVerify(String userName, String password, String secureCon) throws MessagingException, IOException {
        String host = YAHOOPOP3HOST;
        switch (userName.split("@")[1]) {
            case "gmail.com":
                return getGmailIMAP("imap.gmail.com", userName, password);
            case "163.com":
                host = _163POP3HOST;
                break;
            case "mail.ru":
            case "list.ru":
                host = MAILRUPOP3HOST;
                break;
        }
        Properties properties = getProperties(host, POP3PORT, secureCon);
        Session session = Session.getDefaultInstance(properties);
        Store store = session.getStore("pop3");
        store.connect(userName, password);
        Folder folderInbox = store.getFolder("INBOX");
        folderInbox.open(Folder.READ_ONLY);
        Message[] messages = folderInbox.getMessages();
        String emailCode = findVerifyCode(messages);
        // disconnect
        folderInbox.close(false);
        store.close();
        return emailCode;
    }

    private static String getGmailIMAP(String host, String username, String password) throws MessagingException, IOException {
        String emailCode = null;
        URLName url = new URLName("imaps", host, 993, "INBOX", username, password);
        Properties props = null;
        try {
            props = System.getProperties();
//            props.setProperty("proxySet", "true");
//            props.setProperty("socksProxyHost", "127.0.0.1");
//            props.setProperty("socksProxyPort", "1080");
        } catch (SecurityException sex) {
            props = new Properties();
        }
        Session session = Session.getInstance(props, null);
        Store store = session.getStore(url);
        store.connect();
        Folder folderInbox = store.getFolder(url);
        folderInbox.open(Folder.READ_ONLY);
        Message[] messages = folderInbox.getMessages();
        emailCode = findVerifyCode(messages);
        // disconnect
        folderInbox.close(false);
        store.close();
        return emailCode;
    }


    /**
     * Runs this program with yahoo POP3 servers
     *
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, ParseException, MessagingException {
        String userName = "AlgerTomdMzAy@yahoo.com";
        String password = "bqbnypowqxrjwtrq";
        String secureCon = "ssl";

        RetrieveEmailsUsingPOP3 oep = new RetrieveEmailsUsingPOP3();
        String code = oep.getAmazonVerify(userName, password, secureCon);
        System.out.println(code);
//        oep.getCompleteInbox(host, port, userName, password, secureCon);
        //uncomment below method to check connection status
        //getConnectionStatus(host, port, userName, password);  use this method to check connection, use this method on the time of saving details only
    }
}
