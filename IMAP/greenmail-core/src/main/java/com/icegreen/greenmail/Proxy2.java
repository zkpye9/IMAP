package com.icegreen.greenmail;

import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

class Email {
    public String from;
    public String to;
    public String subject;
    public String msg;
    public Date receive;
    public Date send;

    public Email(String from, String to, String subject, String msg, Date receive, Date send) {
        this.from = from;
        this.to= to;
        this.subject = subject;
        this.msg = msg;
        this.receive = receive;
        this.send = send;
    }
}
public class Proxy2 {

    public static LinkedList<Email> readGmail(String currentEmail) {
        LinkedList<Email> result = new LinkedList<Email>();
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");

        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "imaptests1@gmail.com", "helloworld");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] msg = inbox.getMessages();
            Email temp;
            for (int i = 0; i < msg.length; i++) {
                temp = new Email("", "", "", "", null, null);
                Address[] in = msg[i].getFrom();
                for (Address address : in) {
                    temp = new Email(address.toString(), currentEmail, "", "", null, null);
                }

                temp.subject = msg[i].getSubject();
                if (msg[i].isMimeType("text/plain")) {
                    temp.msg = msg[i].getContent().toString();
                } else if (msg[i].isMimeType("multipart/*")) {
                    System.out.println("message body is multipart");
                    MimeMultipart mimeMultipart = (MimeMultipart) msg[i].getContent();
                    temp.msg = getTextFromMimeMultipart(mimeMultipart);
                }
                //temp.msg = msg[i].getContent().toString();
                temp.receive = msg[i].getReceivedDate();
                temp.send = msg[i].getSentDate();
                result.add(temp);
            }

        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws Exception{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                System.out.println("bodypart is plain text");
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                System.out.println("bodypart is html");
                String html = (String) bodyPart.getContent();
                //result = result + "\n" + Jsoup.parse(html).text();
                result = result+"this is a html, in the future if you want to suppor html in an email, simply use" +
                        "Jsoup to parse the html part";
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                System.out.println("body part is mimemultipart");
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }


    public static void updateEmails() throws Exception{
        ImapServer imapS = gmail.getImap();
        Managers manager = gmail.getManagers();
        ImapHostManager imapM = manager.getImapHostManager();
        UserManager userM= manager.getUserManager();

        Flags testFlags = new Flags();

        int updateCount = 0;
        LinkedList<Email> fetch = readGmail("imaptests1@gmail.com");

        for (int j = fetch.size()-1; j>=0; j--) {
            if (emailBuffer.size() == 0) {
                System.out.println("before update, there's no email in the cache");
                for (Email i: fetch) {
                    MimeMessage testMessage = GreenMailUtil.createTextEmail(i.to,
                            i.from, i.subject, i.msg, imapS.getServerSetup());
                    imapM.getInbox(userM.getUserByEmail("imaptests1@gmail.com")).appendMessage(testMessage, testFlags, null);
                }
                return;
            }
            else {
                if (fetch.get(j).subject.equals(emailBuffer.getLast().subject)
                        && fetch.get(j).from.equals(emailBuffer.getLast().from)
                        && fetch.get(j).to.equals(emailBuffer.getLast().to)
                        && fetch.get(j).send.equals(emailBuffer.getLast().send)
                        && fetch.get(j).receive.equals(emailBuffer.getLast().receive)) {
                    break;
                } else {
                    updateCount++;
                }
            }
        }

        for (int i = updateCount; i>0; i--) {
            emailBuffer.add(fetch.get(fetch.size()-i));
            MimeMessage testMessage = GreenMailUtil.createTextEmail(fetch.get(fetch.size()-i).to,
                    fetch.get(fetch.size()-i).from, fetch.get(fetch.size()-i).subject, fetch.get(fetch.size()-i).msg, imapS.getServerSetup());
            imapM.getInbox(userM.getUserByEmail("imaptests1@gmail.com")).appendMessage(testMessage, testFlags, null);
        }
    }


    public static GreenMail gmail = new GreenMail();
    public static LinkedList<Email> emailBuffer;
    public static void main(String argv[]) throws Exception {

        //GreenMail gmail = new GreenMail();
        gmail.setUser("imaptests1@gmail.com", "imaptests1@gmail.com", "helloworld");
        //updateEmails();
        ImapServer imapS = gmail.getImap();
        Managers manager = gmail.getManagers();
        ImapHostManager imapM = manager.getImapHostManager();
        UserManager userM= manager.getUserManager();

        Flags testFlags = new Flags();

        emailBuffer = readGmail("imaptests1@gmail.com");
        for (Email i: emailBuffer) {
            MimeMessage testMessage = GreenMailUtil.createTextEmail(i.to,
                    i.from, i.subject, i.msg, imapS.getServerSetup());
            imapM.getInbox(userM.getUserByEmail("imaptests1@gmail.com")).appendMessage(testMessage, testFlags, null);
        }
        gmail.start();
        System.out.println("Finish starting Server");
    }
}
