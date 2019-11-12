package com.example.todo.app.welcome;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HelloController {

    private static final Logger logger = LoggerFactory
            .getLogger(HelloController.class);
        
    private GreenMail greenMail;

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String home(Locale locale, Model model) {
        logger.info("Welcome home! The client locale is {}.", locale);

        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG, locale);

        String formattedDate = dateFormat.format(date);

        model.addAttribute("serverTime", formattedDate);

        return "welcome/home";
    }
    
    /**
     * Simply selects the home view to render by returning its name.
     * @throws MessagingException 
     * @throws AddressException 
     * @throws IOException 
     */
    @RequestMapping(value = "/mail", method = {RequestMethod.GET, RequestMethod.POST})
    public String Mail(Locale locale, Model model) throws AddressException, MessagingException, IOException {
        logger.info("Welcome mail! The client locale is {}.", locale);
        
        // greenmail start
        this.greenMail = new GreenMail(ServerSetupTest.SMTP);
        this.greenMail.start();
        
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", "3025");
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setRecipient(RecipientType.TO, new InternetAddress("to-sample@test.com"));
        message.setFrom(new InternetAddress("from-sample@test.com"));
        message.setSubject("マルチバイトの件名です！！");
        message.setText("マルチバイトの本文です。末尾注意", "iso-2022-jp");
        
        Transport.send(message);

        MimeMessage actual = this.greenMail.getReceivedMessages()[0];
        
        try (FileOutputStream out = new FileOutputStream(new File("myMessage.eml"))) {
        	actual.writeTo(out);
        }
        
        Assert.assertThat(actual.getRecipients(RecipientType.TO)[0].toString(),
            CoreMatchers.is("to-sample@test.com"));
        Assert.assertThat(actual.getFrom()[0].toString(), CoreMatchers.is("from-sample@test.com"));
        Assert.assertThat(actual.getSubject(), CoreMatchers.is("マルチバイトの件名です！！"));
        Assert.assertThat(actual.getContent().toString(), CoreMatchers.notNullValue());
        
        // greenmail stop
        this.greenMail.stop();
        
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG, locale);

        String formattedDate = dateFormat.format(date);

        model.addAttribute("serverTime", formattedDate);

        return "welcome/home";
    }

}
