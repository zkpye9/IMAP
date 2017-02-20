import java.util.*;
import javax.mail.*;

public class ReadingEmail {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "imaptests1@gmail.com", "helloworld");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            //Message msg = inbox.getMessage(inbox.getMessageCount());
            Message[] msg = inbox.getMessages();
            for (int i = 0; i<msg.length; i++) {
            	Address[] in = msg[i].getFrom();
                for (Address address : in) {
                    System.out.println("FROM:" + address.toString());
                }
                //Multipart mp = (Multipart) msg[i].getContent();
                //BodyPart bp = mp.getBodyPart(0);
                //System.out.println("SENT DATE:" + msg[i].getSentDate());
                System.out.println("SUBJECT:" + msg[i].getSubject());
                //System.out.println("CONTENT:" + bp.getContent());
            }
            
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        
        //SendingEmail test = new SendingEmail("imaptests1@gmail.com", "zkpye9@gmail.com", "imaptests1@gmail.com", "helloworld", "smtp.gmail.com");
        //test.send();
        ReplyingEmail test = new ReplyingEmail("smtp.gmail.com", "imaptests1@gmail.com","helloworld");
        test.reply();
        
        
    }
}