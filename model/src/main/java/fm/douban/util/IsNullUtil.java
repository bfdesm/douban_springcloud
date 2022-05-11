package fm.douban.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class IsNullUtil {
    public static boolean isNull(String a) {
        if (a == null)
            return true;
        return false;
    }

    public static boolean isNull(LocalDateTime a) {
        if (a == null)
            return true;
        return false;
    }

    public static boolean isNull(List a) {
        if (a == null)
            return true;
        return false;
    }

    public static boolean isNull(Integer a) {
        if (a == null)
            return true;
        return false;
    }

    public static boolean isNull(Map a) {
        if (a == null)
            return true;
        return false;
    }

    public static boolean isNull(LocalDate localDate){
        if(localDate == null)
            return true;
        return false;
    }

    public static boolean isNull(Long a){
        if(a == null)
            return true;
        return false;
    }
}
