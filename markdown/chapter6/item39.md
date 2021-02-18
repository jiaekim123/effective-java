# [Effective Java] item 39. 명명 패턴보다 애너테이션을 사용하라

## 명명 패턴
- 도구나 프레임워크가 특별히 다루어야 할 프로그램 요소에 구분되는 명명 패턴을 적용하는 것.
- 예를 들어 Junit 3까지는 테스트 메서드 이름을 test로 시작해야만 했다.

### 명명 패턴의 단점
#### 1. 오타가 나면 안된다.
- Junit 3까지의 테스트 메서드 이름이 고정되어 있어서, 오타로 tsetSafetyOverride로 지으면 이 메서드를 무시하고 지나쳐서 테스트가 통과되었다고 오해할 수 있다.
#### 2. 올바른 프로그램 요소에만 사용되리라 보증할 방법이 없다.
- 클래스 이름을 TestSafetyMechanism으로 지어 Junit에 던졌을 때, 개발자는 이 테스트가 수행되길 기대하겠지만 Junit은 경고 메세지조차 출력하지 않고 테스트는 수행되지 않는다.
#### 3. 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다. 
- 특정 예외를 던져야만 성공하는 테스트가 있다고 할 때, 기대하는 예외 타입을 테스트에 매개변수로 전달해야 하는 상황이라고 가정해보자. 이 떄 예외의 이름을 테스트 메서드 이름에 덧붙이는 방법도 있겠지만, 보기도 나쁘고 깨지기도 쉽다. 컴파일러는 메서드 이름에 덧붙인 문자열이 예외를 가리키는지 알 도리가 없다. 테스트를 실행하기 전에는 그런 이름의 클래스가 존재하는지 또는 예외가 맞는지조차 알 수 없다.

### 애너테이션
애너테이션은 위 명명 패턴의 단점을 모두 해결해주는 멋진 개념으로, Junit도 버전 4부터 전면 도입했다. 이번 아이템에서는 애너테이션의 동작 방식을 보여주고자 직접 제작한 테스트 프레임워크를 사용할 것이다.

Test라는 이름의 애너테이션을 정의한다고 해보자. 자동으로 수행되는 간단한 테스트용 애너테이션으로, 예외가 발생하면 해당 테스트를 실패로 처리한다.

##### 마커(marker) 애너테이션 타입 선언
```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

보다시피 @Test 애너테이션 타입 선언 자체에도 두 가지 다른 애너테이션이 달려있다. 바로 @Retention과 @Target이다. 이처럼 `애너테이션 선언에 다는 애너테이션을 메타 애너테이션(meta-annotation)`이라 한다.

- `@Retention(RetentionPolicy.RUNTIME)` 메타 에너테이션
    - `@Test가 런타임에도 유지되어야 한다`는 표시
    - 이 메타에너테이션을 생략하면 테스트 도구는 @Test를 인식할 수 없다.
- `@Target(ElementType.METHOD)` 메타 애너테이션
    - `@Test가 반드시 메서드 선언에만 사용되어야 한다`고 알려주는 표시
    - 클래스 선언, 필드 선언 등 다른 프로그램 요소에는 달 수 없다.

다음 코드는 @Test 애너테이션을 실제로 적용한 모습이다. 이와 같은 애너테이션을 `"아무 매개변수 없이 단순히 대상에 마킹(marking)한다"는 뜻에서 마커(marker) 애너테이션`이라고 한다.

이 애너테이션을 사용하면 프로그래머가 Test 이름에 오타를 내거나 메서드 선언 외에 프로그램 요소에 달면 컴파일 오류를 내준다.

##### 마커 애너테이션을 사용한 프로그램 예
```java
public class Sample {
    @Test public static void m1() {} // 성공해야 한다.
    public static void m2() {}
    @Test public static void m3() { // 실패해야 한다.
        throw new RuntimeException("실패");
    }
    public static void m4() {}
    @Test public void m5() {} // 잘못 사용한 예: 정적 메서드가 아니다.
    public static void m6() {}
    @Test public static void m7() { // 실패해야 한다.
        throw new RuntimeException("실패");
    }
    public static void m8 (){}
}
```

Sample 클래스에는 정적 메서드가 7개고, 그중 4개에 @Test를 달았다. m3와 m7 메서드는 예외를 던지고 m1과 m5는 그렇지 않다. 그리고 @Test를 붙이지 않은 나머지 4개의 메서드는 테스트 도구가 무시할 것이다.

@Test 애너테이션이 Sample 클래스의 의미에 직접적인 영향을 주지는 안흔다. 그저 이 애너테이션이 관심 있는 프로그램에게 추가 정보를 제공할 뿐이다. 더 넓게 이야기하면, 대상 코드의 의미는 그대로 둔 채 그 애너테이션에 관심 있는 도구에서 특별한 처리를 할 기회를 준다. 다음의 RunTests가 바로 그런 도구의 예이다.

##### 마커 애너테이션을 처리하는 프로그램
```java
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
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " 실패: " + exc);
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
```

![item39_1](https://user-images.githubusercontent.com/37948906/108180273-e694e100-7149-11eb-85e0-e11c1d447600.PNG)


---

### 참고 자료
- Effective Java 3/E