# [Effective Java] Item 1. 생성자 대신 정적 팩터리 메서드를 고려하라

---

### 예시

```java
public static Boolean valueOf(boolean b){
    return b ? Boolean.True : Boolean.False;
}
```

클래스는 클라이언트에 public 생성자 대신 (혹은 생성자와 함께) 정적 팩터리 메서드를 제공할 수 있다.
이 방식에는 장점과 단점이 모두 존재한다.

---

### 정적 팩터리 메서드의 5가지 장점

1. **이름을 가질 수 있다.**

    - (생성자 호출 방식) `BigInteger(int, int Random)`
    - (정적 팩토리 메서드 호출 방식) `BigInteger.probablePrime`
    - 위 예시에서 값이 소수인 BigInteger를 반환한다는 의미를 더 잘 설명할 수 있는 것은 정적 팩터리 메서드 호출 방식이다.
    - 하나의 시그니처로는 생성자를 하나만 만들 수 있다. 이에 반해 `정적 팩터리 메서드는 한 클래스에 시그니처가 같은 생성자가 여러 개 필요할 경우, 생성자를 정적 팩터리 메서드로 바꾸고 각각의 차이가 잘 드러내는 이름을 지어줄 수 있다.`
2. **호출될 때마다 인스턴스를 새로 생성하지 않아도 된다.**
    - 불변 클래스(IMMUTABLE CLASS)를 미리 만들어 두거나 새로 생성한 인스턴스를 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.
    - 이러한 특징 덕에, 생산 비용이 큰 객체가 자주 요청되는 상황이라면 성능을 상당히 끌어올려줄 수 있다. `플라이웨이트 패턴(Flyweight pattern)`도 이와 비슷한 기법이라 할 수 있다.
    - 인스턴스 통제(instance-controlled) 클래스: 반복되는 요청에 같은 객체를 반환하는 식으로 정적 팩터리 방식의 클래스는 언제 어느 인스턴스를 살아 있게 할지를 철저히 통제할 수 있는 클래스이다. 이를 통해 클래스를 `싱글턴(singleton)`으로 만들 수도, `인스턴스화 불가(noninstantiable)`로 만들 수도 있다. 또는 불변 값 클래스에서 인스턴스가 단 하나임을 보장할 수 있다. 
3. **반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.**
    - 반환할 객체의 클래스를 자유롭게 선택할 수 있는 유연성을 가진다. 이는 인터페이스를 정적 팩터리 메서드의 반환 타입으로 사용하는 `인터페이스 기반 프레임워크`를 만드는 핵심 기술이기도 하다.
4. **입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.**
    - 가령 `EnumSet` 클래스는 public 생성자 없이 오직 정적 팩터리만 제공하는데, OpenJDK에서는 원소의 수에 따라 두 가지 하위 클래스 중 하나의 인스턴스를 반환한다. (원소가 64개 이하면 long변수 하나로 관리하는 RegularEnumSet 인스턴스를, 65개 이상이면 long 배열로 관리하는 JumboEnumSet 인스턴스를 반환한다.)
5. **정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.**
    - 이러한 유연함은 JDBC와 같은 서비스 제공자 프레임워크를 만드는 근간이 된다. 
    - 서비스 제공자 프레임워크 패턴의 핵심 컴포넌트
        - (1) 서비스 인터페이스 (service interface)
            - 구현체의 동작을 정의
            - (예) JDBC의 Connection
        - (2) 제공자 등록 API (provider registration API)
            - 제공자가 구현체를 등록할 때 사용
            - (예) JDBC의 DriverManager.registerDriver
        - (3) 서비스 접근 API (service access API)
            - 클라이언트가 서비스의 인스턴스를 얻을 때 사용
            - (예) JDBC의 DriverManager.getConnection
        - (4) (추가) 서비스 제공자 인터페이스 (service provider interface)
            - 서비스 인터페이스의 인스턴스를 생성하느 팩터리 객체를 설명
            - 서비스 제공자 인터페이스가 없을 경우, 각 구현체를 인스턴스로 만들 때 리플렉션을 사용해야 함.
            - (예) JDBC의 Driver
    
### 정적 팩터리 메서드의 2가지 단점
1. **상속을 하려면 public이나 protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.**
    - 앞에서 말한 컬렉션 프레임워크의 유틸리티 구현 클래스들은 상속할 수 없다.
2. **정적 팩터리 메서드는 프로그래머가 찾기 어렵다.**
    - 생성자처럼 API 설명에 명확히 드러나지 않기 때문에, 사용자가 정적 팩터리 메서드 방식 클래스를 인스턴스화할 방법을 알아내야 한다.

---

### 정적 팩터리 메서드에서 흔하게 사용하는 명명 방식과 대표 예시
- `from`: 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드
    - `Date d = Date.from(instant);`
```java
public static Date from(Instant instant) {
        try {
            return new Date(instant.toEpochMilli());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
}
```

- `of`: 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드
    - `Set<Rank> faceCard = EnumSet.of(JACK, QUEEN, KING);`
```java
public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> result = noneOf(e.getDeclaringClass());
        result.add(e);
        return result;
}
```
```java
public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        return result;
}
```


- `valueOf`: from과 of의 더 자세한 버전
    - `BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE)`
```java
public static BigInteger valueOf(long val) {
        // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
        if (val == 0)
            return ZERO;
        if (val > 0 && val <= MAX_CONSTANT)
            return posConst[(int) val];
        else if (val < 0 && val >= -MAX_CONSTANT)
            return negConst[(int) -val];

        return new BigInteger(val);
}
```

- `instance` 혹은 `getInstance`: (매개변수를 받는다면) 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.
    - `StackWalker luke = StackWalker.getInstance(options);`
```java
private final static StackWalker DEFAULT_WALKER =
        new StackWalker(DEFAULT_EMPTY_OPTION);
```
```java
public static StackWalker getInstance() {
        // no permission check needed
        return DEFAULT_WALKER;
}
```

- `create`혹은 `newInstance`: instance 혹은 getInstance와 같지만, 매번 새로운 인스턴스를 생성해 반환함을 보장한다.
    - `Object newArray = Array.newInstance(createObject, arrayLen);`
```java
public static Object newInstance(Class<?> componentType, int length)
        throws NegativeArraySizeException {
        return newArray(componentType, length);
}
```

- `getType`: getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스의 팩터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 반환할 객체의 타입이다.
    - `FileStore fs = Files.getFileStore(path)`
```java
public static FileStore getFileStore(Path path) throws IOException {
        return provider(path).getFileStore(path);
}
```

- `newType`: newInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 반환할 객체의 타입이다.
    - `BufferedReader br = Files.newBufferedReader(path);`
```java
public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, StandardCharsets.UTF_8);
}
```

- `type`: getType과 newType의 간결한 버전
    - `List<Complaint> litany = Collections.list(legacyLitany);`
```java
public static <T> ArrayList<T> list(Enumeration<T> e) {
        ArrayList<T> l = new ArrayList<>();
        while (e.hasMoreElements())
            l.add(e.nextElement());
        return l;
}
```

---

### 핵심 정리
정적 팩터리 메서드와 public 생성자는 각자의 쓰임새가 있으니 상대적인 장단점을 이해하고 사용하는 것이 좋다. 그렇다고 하더라도 정적 팩터리를 사용하는 게 유리한 경우가 더 ㅁ낳으므로 무작정 public 생성자를 제공하던 습관이 있다면 고치자.

---

### 관련 자료

1. 플라이웨이트 패턴(Flyweight pattern) - Gramma 95
    - 객체의 내부에서 참조하는 객체를 직접 만드는 것이 아니라, 없다면 만들고, 만들어져 있다면 객체를 공유하는 식으로 객체를 구성하는 방법
2. 싱글턴(singleton) - item 3
3. 인스턴스화불가(noninstantiable) - item 4
4. 인터페이스 기반 프레임워크 - item 20
5. EnumSet 클래스 - item 36
6. 리플렉션 - item 65

### 참고 자료
- Effective Java 3/E
