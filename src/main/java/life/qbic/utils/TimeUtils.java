package life.qbic.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeUtils {
  
  static Logger logger = LogManager.getLogger(TimeUtils.class);


  public static String getCurrentTimestampString() {
    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    return timeStamp;
  }
  
  public static void logElapsedTime(long startTime) {
    long stopTime = System.currentTimeMillis();
    long elapsedTime = stopTime - startTime;
    logger.info(elapsedTime);
  }
}
