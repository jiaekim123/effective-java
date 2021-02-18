package item39;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]); // item39.Sample
        for (Method m : testClass.getDeclaredMethods()) { // 리플렉션으로 사용할 메서드 m1, m2...
            if (m.isAnnotationPresent(Test.class)) { // @Test 어노테이션이 있으면
                tests++;
                try {
                    m.invoke(null); // 실제 메서드 실행
                    passed++; // 성공하면 passed++
                } catch (InvocationTargetException wrappedExc) { // 테스트에서 예외를 던지면 리플렉션 매커니즘이 InvocationTargetException으로 감싸서 다시 던진다.
                    Throwable exc1 = wrappedExc.getTargetException();
                    Throwable exc2 = wrappedExc.getCause();
                    System.out.println(m + " 실패 exc1: " + exc1);
                    System.out.println(m + " 실패 exc1: " + exc2);
                } catch (Exception exc) {
                    // 테스트에서 잡지 못한 예외가 있다는것은 @Test 애너테이션을 잘못 사용했다는 뜻.
                    System.out.println("잘못 사용한 @Test: " + m);
                }
            }
        }
        System.out.printf("성공: %d, 실패: %d%n",
                passed, tests - passed);
    }
}