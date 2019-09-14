package me.tekkitcommando.promotionessentials.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHandler {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    public LocalDateTime getDateTime() {
        LocalDateTime dateTimeNow = LocalDateTime.now();
        String dateTimeString = dateTimeNow.format(formatter);

        return LocalDateTime.parse(dateTimeString, formatter);
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
