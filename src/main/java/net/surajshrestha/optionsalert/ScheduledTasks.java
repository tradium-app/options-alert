package net.surajshrestha.optionsalert;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

import com.sendgrid.*;

@Component
public class ScheduledTasks {
    private static Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:dss");

    @Scheduled(fixedRate = 5 * 60 * 60 * 1000)
    public void reportCurrentTime(){
        try {
            long nextMinOptionsDateEpoch = getMinNextOptionsDate();
            Document doc = Jsoup.connect("https://finance.yahoo.com/quote/BABA/options").get();
            Elements selectOptions = doc.select("#Col1-1-OptionContracts-Proxy > section select option");

            long optionDateEpoch = 0;
            for(Element selectOption : selectOptions) {
                optionDateEpoch = Long.parseLong(selectOption.val());
                if(optionDateEpoch > nextMinOptionsDateEpoch){
                    break;
                }
            }

            String optionUrl = String.format("https://finance.yahoo.com/quote/BABA/options?p=BABA&date=%s&straddle=true", optionDateEpoch);
            doc = Jsoup.connect(optionUrl).get();
            Elements rowElements = doc.select("#Col1-1-OptionContracts-Proxy > section table > tbody > tr.call-in-the-money");
            Element lastRow = rowElements.last();
            String percentChangeStr = lastRow.select("td.data-col2 > span").last().text();
            percentChangeStr = percentChangeStr.replaceAll("%", "");
            Float percentChange = Float.parseFloat(percentChangeStr);

            sendNotificationEmail(percentChange);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("The time is now {}", dateFormat.format(new Date()));
    }

    private void sendNotificationEmail(float percentChange) throws IOException {
        Email from = new Email("info@siristechnology.com");
        String subject = String.format("Options change alert %.2f%%", percentChange);
        Email to = new Email("syuraj@gmail.com");
        Content content = new Content("text/plain", String.format("Options change by %.2f", percentChange));

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(System.getenv("sendgridApiKey"));

        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sendGrid.api(request);
    }

    private long getMinNextOptionsDate(){
        Instant instant = Instant.now();
        Instant instant2 = instant.plus(45, ChronoUnit.DAYS);

        return instant2.toEpochMilli()/1000;
    }
}
