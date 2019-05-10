package life.qbic.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

  public static String getCurrentTimestampString() {
    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    return timeStamp;
  }
}
