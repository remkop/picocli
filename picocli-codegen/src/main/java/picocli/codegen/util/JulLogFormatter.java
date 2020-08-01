package picocli.codegen.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JulLogFormatter extends Formatter {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(new Date(record.getMillis()))).append(" ");
        sb.append(record.getLevel()).append(" ");
        sb.append("[");
        sb.append(record.getSourceClassName()).append(".").append(record.getSourceMethodName());
        sb.append("] ");
        sb.append(record.getMessage());
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
