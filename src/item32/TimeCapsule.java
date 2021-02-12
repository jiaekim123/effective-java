package item32;

import java.util.ArrayList;
import java.util.List;

public class TimeCapsule {
    
    @SafeVarargs
    static <T> List<T> saveCapsule(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists)
            result.addAll(list);
        return result;
    }

    public static void main(String[] args) {
        List<String> timeCapsule = saveCapsule(
                List.of("편지", "인형"), List.of("USB", "편지"), List.of("반지","사진"));
        System.out.println(timeCapsule);
    }
}