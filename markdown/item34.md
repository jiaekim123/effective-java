# [Effective Java] item 34. int 상수 대신 열거 타입을 사용하라

## 핵심 정리

- `열거 타입은 확실히 정수 상수보다 뛰어나다.` 더 읽기 쉽고 안전하고 강력하다. 대다수 열거 타입이 명시적 생성자나 메서드 없이 쓰이지만, 각 상수를 특정 데이터와 연결짓거나 상수마다 다르게 동작하게 할 때는 필요하다.
- 드물게는 `하나의 메서드가 상수별로 다르게 동작`하게 할 때도 있다. 이런 열거 타입에는 switch 문 대신 `상수별 메서드를 구현하자. `
- 열거 타입 상수 일부가 같은 동작을 공유한다면 `전략 열거 타입 패턴`을 사용하자.

### 열거 타입
- 열거 타입: 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입.

### 정수 열거 패턴
- 자바에서 enum을 지원하기 전에 사용하던 정수 열거 패턴
```java
public static final int APPLE_FUJI          = 0;
public static final int APPLE_PIPPIN        = 1;
public static final int APPLE_GRANNY_SMITH  = 2;

public static final int ORANGE_NAVEL        = 0;
public static final int ORANGE_TEMPLE       = 1;
public static final int ORANGE_BLOOD        = 2;
```

#### 정수 열거 패턴 기법에는 단점
- 타입 안전을 보장할 방법이 없으며 표현력도 좋지 않다.
- 상수의 값이 바뀌면 클라이언트도 반드시 다시 컴파일해야 한다.
- 문자열로 출력하기 다소 까다롭다. 그 값을 출력하거나 디버거로 살펴보면 단지 숫자로만 보여서 딱히 도움이 되지 않는다. 그룹에 속한 상수가 몇 개인지도 알 수 없다.

### 열거 타입
#### 가장 단순한 열거 타입
```java
public enum Apple {FUJI, PIPPIN, GRANNY_SMITH}
public enum Orange {NAVEL, TEMPLE, BLOOD}
```
#### 열거 타입의 장점
- 열거 타입 자체는 클래스이며, 상수 하나당 자신의 인스턴스를 하나씩 만들어 public static final 필드로 공개한다.
- 열거 타입은 밖에서 접근할 수 있는 생성자를 제공하지 않으므로 사실상 final이다. 다시 말해 열거 타입은 인스턴스 통제 된다.
- 싱글턴은 원소가 하나 뿐인 열거 타입이며 거꾸로 열거타입은 싱글턴을 일반화한 형태라고 볼 수 있다.
- 열거 타입은 컴파일타임 타입 안전성을 제공한다.
- 열거 타입에는 각자의 이름 공간이 있어서 이름이 같은 상수도 평화롭게 공존한다. 
- 열거 타입에 새로운 상수를 추가하거나 순서를 바꿔도 다시 컴파일하지 않아도 된다. 공개되는 것이 오직 필드의 이름 뿐이라, 정수 열거 패턴과 달리 상수 값이 클라이언트로 컴파일되어 각인되지 않기 대문이다.
- 열거 타입의 toString 메서드는 출력하기에 적합한 문자열을 내어준다.

#### 데이터와 메서드를 갖는 열거 타입
```java
public enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS  (4.869e+24, 6.052e6),
    EARTH  (5.975e+24, 6.378e6),
    MARS   (6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN (5.685e+26, 6.027e7),
    URANUS (8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);

    private final double mass;           // In kilograms
    private final double radius;         // In meters
    private final double surfaceGravity; // In m / s^2

    // Universal gravitational constant in m^3 / kg s^2
    private static final double G = 6.67300E-11;

    // Constructor
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }

    public double mass()           { return mass; }
    public double radius()         { return radius; }
    public double surfaceGravity() { return surfaceGravity; }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity;  // F = ma
    }
}
```

- 열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다.
- 열거타입은 근본적으로 불변이라 모든 필드는 final이어야 한다. 필드를 public으로 선언해도 되지만, private으로 두고 별도의 public 접근자 메서드를 두는게 낫다.

Planet 열거 타입은 단순하지만 놀랍도록 강력하다. 어떤 객체의 지구에서의 무개를 입력받아 여덟 행성에서의 무게를 출력하는 일을 다음처럼 짧은 코드로 작성할 수 있다.
```java
public class WeightTable {
   public static void main(String[] args) {
      double earthWeight = Double.parseDouble(args[0]);
      double mass = earthWeight / Planet.EARTH.surfaceGravity();
      for (Planet p : Planet.values())
         System.out.printf("Weight on %s is %f%n",
                 p, p.surfaceWeight(mass));
   }
}
```

열거 타입은 자신안에 정의된 상수들의 값을 배열에 담아 반환하는 정적 메서드인 values를 제공한다. 값들을 선언된 순서로 저장된다. 각 열거 타입 값의 toString 메서드는 상수 이름을 문자열로 반환하므로 println과 printf로 출력하기에 안성맞춤이다.

널리 쓰이는 열거 타입은 톱레벨 클래스로 만들고, 특정 톱레벨 클래스에서만 쓰인다면 해당 클래스의 멤버 클래스로 만든다.

만약 상수마다 동작을 다르게 해야 하는 상황이라면 어떻게 해야 할까?
먼저 switch문을 이용해 상수의 값에 따라 분기하는 방법을 시도해보자.
#### 값에 따라 분기하는 열거 타입 - 이대로 만족하는가?
```java
public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    // 상수가 뜻하는 연산을 수행한다.
    public double apply(double x, double y) {
        switch(this) {
            case PLUS:  return x + y;
            case MINUS:  return x - y;
            case TIMES:  return x * y;
            case DIVIDE:  return x / y;
        }
        throw new AssertionError("알 수 없는 연산: " : + this);
    }
}
```

동작은 하지만 그렇게 예쁘지는 않다. 다행히 열거 타입은 상수 별로 다르게 동작하는 코드를 구현하는 더 나은 수단을 제공한다.

#### 상수별 메서드 구현을 활용한 열거 타입
```java
public enum Operation {
    PLUS {public double apply(double x, double y){return x + y;}},
    MINUS {public double apply(double x, double y){return x - y;}},
    TIMES {public double apply(double x, double y){return x * y;}},
    DIVIDE {public double apply(double x, double y){return x / y;}};

    public abstract double apply(double x, double y);
}
```

혹은 연산 기호를 이용하여 아래와 같이 정의할 수도 있다.
#### 상수별 클래스 몸체(class body)와 데이터를 사용한 열거 타입
```java
public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }

    public abstract double apply(double x, double y);

    // Implementing a fromString method on an enum type (Page 164)
    private static final Map<String, Operation> stringToEnum =
            Stream.of(values()).collect(
                    toMap(Object::toString, e -> e));

    // Returns Operation for string, if any
    public static Optional<Operation> fromString(String symbol) {
        return Optional.ofNullable(stringToEnum.get(symbol));
    }

    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        for (Operation op : Operation.values())
            System.out.printf("%f %s %f = %f%n",
                    x, op, y, op.apply(x, y));
    }
}
```
#### 전략 열거 타입 패턴
```java
import static item34.PayrollDay.PayType.*;

enum PayrollDay {
    MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WEDNESDAY(WEEKDAY),
    THURSDAY(WEEKDAY), FRIDAY(WEEKDAY),
    SATURDAY(WEEKEND), SUNDAY(WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) { this.payType = payType; }

    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }

    // The strategy enum type
    enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked <= MINS_PER_SHIFT ? 0 :
                        (minsWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };

        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;

        int pay(int minsWorked, int payRate) {
            int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }

    public static void main(String[] args) {
        for (PayrollDay day : values())
            System.out.printf("%-10s%d%n", day, day.pay(8 * 60, 1));
    }
}
```

- 필요한 원소를 컴파일타임에 알 수 있는 상수 집합이라면 항상 열거 타입을 사용하자.
- 열거 타입에 정의된 상수 개수가 영원히 고정 불변일 필요는 없다.

---

### 참고 자료
- Effective Java 3/E