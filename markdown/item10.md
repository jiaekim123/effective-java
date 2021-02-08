# [Effective Java] Item 10. equals는 일반 규약을 지켜 재정의하라

---

equals 메서드는 재정의하기 쉬워보이지만 곳곳에 함정이 도사리고 있어서 자칫하면 끔찍한 결과를 초래한다. 문제를 회피하는 가장 쉬운 길은 아얘 재정의하지 않는 것이다. 그냥 두면 그 클래스의 인스턴스는 오직 자기 자신과만 같게 된다. 그러니 다음에서 열거한 상황 중 하나에 
해당한다면 재정의하지 않는 것이 최선이다.

---

### **equals를 재정의 하지 않아야 하는 경우**
1. 각 인스턴스가 본질적이고 고유하다.
    - 값을 표현하는 게 아니라 동작하는 개체를 표현하는 클래스가 여기 해당한다. Thread가 좋은 예로, Object의 equals 메서드는 이러한 클래스에 딱 맞게 구현되었다.
2. 인스턴스의 '논리적 동치성(logical equality)'을 검사할 일이 없다.
    - 예컨데 java.util.regex.Pattern은 equals를 재정의해서 두 Pattern의 인스턴스가 같은 정규표현식을 나타내는지 검사하는, 즉 논리적 동치성을 검사하는 방법도 있다. 하지만 설계자는 클라이언트가 이 방법을 원하지 않거나 애초에 필요하지 않다고 판단할 수도 있다. 설계자가 후자로 판단했다면 Object의 기본 equals만으로 해결된다.
3. 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.
    - 예컨대 대부분의 set 구현체는 AbstractSet이 구현한 equals를 상속받아 쓰고, List 구현체들은 AbstractList로부터, Map 구현체들은 AbstractMap으로부터 상속받아 그대로 쓴다.
4. 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.
    - 여러분이 위험을 철저히 회피하는 스타일이라 equals가 실수로라도 호출되는 걸 막고 싶다면 다음처럼 구현해 두자.
    ```java
    @Override public boolean equals(Object o) {
        throw new AssertionError(); // 호출 금지!
    }
    ```

---

### **equals를 재정의하는 경우**

그렇다면 equals를 재정의해야 힐 때는 언제일까? `객체 실별성(object identity; 두 객체가 물리적으로 같은가)이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때`이다.

주로 값 클래스들이 여기 해당한다. 값 클래스란 Integer와 String처럼 값을 표현하는 클래스를 말한다. 두 값 객체를 equals로 비교하는 프로그래머는 객체가 같은지가 아니라 값이 같은지를 알고 싶을 것이다. equals가 논리적으로 동치성을 확인하도록 정의해두면, 그 인스턴스는 값을 비교하길 원하는 프로그래머의 기대에 부응함을 물론 Map의 키와 Set의 원소로 사용할 수 있다. 

값 클래스라 해도, 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스라면 equals를 재정의하지 않아도 된다. (Enum이 여기에 해당한다.)
이런 클래스에서는 어차피 논리적으로 같은 인스턴스가 2개 이상 만들어지지 않으니 논리적 동치성과 객체 식별성이 사실상 똑같은 의미가 된다. 따라서 Object의 equals가 논리적 동치성까지 확인해준다고 볼 수 있다.

equals 메서드를 재정의할 때는 반드시 일반 규약을 따라야 한다. 다음은 Object 명세에 적힌 규약이다.

> equals 메서드는 동치 관계(equivalence relation)를 구현하며, 다음을 만족한다.
> - 반사성(reflexivity): null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true다.
> - 대칭성(symmertry): null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true면 y.equals(x)도 true다.
> - 추이성(transitivity): null아 아닌 모든 참조 값 x, y, z에 대해 x.equals(y)가 true이고 y.equals(z)도 true면 x.equals(z)도 true다.
> - 일관성(consistency): null이 아닌 모든 참조 값 x, y에 대해 x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 flase를 반환해야 한다.
> - null-false: null이 아닌 모든 참조 값 x에 대해 x.equals(null)은 false다.\


### 1. 반사성

반사성은 단순히 말해 객체는 자기 자신과 같아야 한다는 뜻이다. 

### 2. 대칭성

대칭성은 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.
예를 들어, 대칭성을 위배한 잘못된 코드를 살펴보자.
#### **대칭성을 위배한 잘못된 코드**
```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위반
    @Override public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(
                    ((CaseInsensitiveString) o).s);
        if (o instanceof String)  // 한 방향으로만 동작함!
            return s.equalsIgnoreCase((String) o);
        return false;
    }

    // 대칭성 위반 실험
    public static void main(String[] args) {
        CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
        String s = "polish";

        List<CaseInsensitiveString> list = new ArrayList<>();
        list.add(cis);

        System.out.println(list.contains(s)); // JDK 버전에 따라 다른 결과가 나옴!
    }

}
```

위와 같이, equals 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할지 알 수 없다.

이 문제를 해결하려면 아래와 같이 CaseInsensitiveString의 equals를 String과도 연동하겠다는 꿈을 버려야 한다. 
```java
   // 올바른 equals (String과 연동하지 않은 버전) (Page 40)
   @Override public boolean equals(Object o) {
       return o instanceof CaseInsensitiveString &&
               ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
   }
```

### 3. 추이성

추이성은 첫 번째 객체와 두 번째 객체가 같고, 두 번째 객체와 세 번째 객체가 같다면, 첫 번째 객체와 세 번째 객체도 같아야 한다는 뜻이다.

예를 들어, 상위 클래스에는 없는 새로운 필드를 하위 클래스에 추가하는 상황을 생각해보자. equals 비교에 영향을 주는 정보를 추가한 것이다. 간단히 2차원에서의 점을 표현하는 Point 클래스를 만들어보고, ColorPoint로 색상을 추가하여 확장해보자.

```java
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        Point p = (Point)o;
        return p.x == x && p.y == y;
    }

    @Override public int hashCode()  {
        return 31 * x + y;
    }
}
```

```java
public class ColorPoint extends Point {
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        return super.equals(o) && ((ColorPoint) o).color == color;
    }
```

equals 메서드는 어떻게 해야 할까? 그대로 둔다면 Point의 구현이 상속되어 색상 정보는 무시한 채 비교를 수행한다. equals 규약을 어긴 것은 아니지만, 중요한 정보를 놓치게 되니 받아들일 수 없는 상황이다. 다음 코드처럼 비교하는 대상이 또 다른 ColorPoint이고 위치와 색상이 같을 때만 true를 반환하는 equals를 생각해보자.

#### **대칭성이 위배된 잘못된 코드**
```java
   @Override public boolean equals(Object o) {
       if (!(o instanceof ColorPoint))
           return false;
       return super.equals(o) && ((ColorPoint) o).color == color;
   }
```

이 메서드는 일반 Point를 ColorPoint에 비교한 결과가 그 둘을 바꿔 비교한 결과가 다를 수 있다. Point의 equals는 색상을 무시하고, ColorPoint의 equals는 입력 매개변수의 클래스 종류가 다르다며 매번 false만 반환할 것이다. 각각의 인스턴스를 하나씩 만들어 실제로 동작하는 모습을 봐보자.

```java
Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);
System.out.println(p.equals(cp) + " " + cp.equals(p));
```

p.equals(cp)는 true를, cp.equals(p)는 false를 반환한다. ColorPoint, equals가 Point와 비교할 때는 색상을 무시하도록 하면 해결될까?

#### **추이성이 위배된 잘못된 코드**
```java
   @Override public boolean equals(Object o) {
       if (!(o instanceof Point))
           return false;

       // If o is a normal Point, do a color-blind comparison
       if (!(o instanceof ColorPoint))
           return o.equals(this);

       // o is a ColorPoint; do a full comparison
       return super.equals(o) && ((ColorPoint) o).color == color;
   }
```

이 방식은 대칭성을 지켜주지만 추이성을 깨버린다.

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
System.out.printf("%s %s %s%n", p1.equals(p2), p2.equals(p3), p1.equals(p3));
```

이제 p1.equals(p2)와 p2.equals(p3)는 true를 반환하는데, p1.equals(p3)가 false를 반환한다. p1과 p2, p2와 p3비교에서는 색상을 무시했지만, p1과 p3 비교에서는 색상까지 고려했기 때문이다.

그럼 해법은 무엇일까? 사실 이 현상은 모든 객체지향 언어의 동치 관계에서 나타나는 근본적인 문제다. 구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다. 객체 지향적 추상화의 이점을 포기하지 않는 한은 말이다.

이 말은 얼핏, equals 안의 instanceof 검사를 getClass 검사로 바꾸면 규약도 지키도 값도 추가하면서 구체 클래스를 상속할 수 있다는 뜻으로 들린다.

#### **리스코프 치환 원칙 위배**
```java
   @Override public boolean equals(Object o) {
       if (o == null || o.getClass() != getClass())
           return false;
       Point p = (Point) o;
       return p.x == x && p.y == y;
   }
```

> 리스코프 치환 원칙: 컴퓨터 프로그램에서 자료형 S가 자료형 T의 하위형이라면 필요한 프로그램의 속성(정확성, 수행하는 업무 등)의 변경 없이 자료형 T의 객체를 자료형 S의 객체로 교체(치환)할 수 있어야 한다는 원칙이다

이번 equals는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다. 괜찮아 보이지만 실제로 활용할 수는 없다. `Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로써 활용될 수 있어야 한다.` 그런데 이 방식에서는 그렇지 못하다. 예를 들어`주어진 점이 (반지름이 1인) 단위 원 안에 있는지를 판별하는 메서드`가 필요하다고 해보자. 다음은 이를 구현한 코드다.

```java
private static final Set<Point> unitCircle = Set.of(
    new Point( 1,  0), new Point( 0,  1),
    new Point(-1,  0), new Point( 0, -1));

public static boolean onUnitCircle(Point p) {
    return unitCircle.contains(p);
}
```

이제 값을 추가하지 않는 방식으로 Point를 확장하여 만들어진 인스턴스의 갯수를 생성자에서 세보자.

```java
public class CounterPoint extends Point {
    private static final AtomicInteger counter =
            new AtomicInteger();

    public CounterPoint(int x, int y) {
        super(x, y);
        counter.incrementAndGet();
    }
    public static int numberCreated() { return counter.get(); }
}
```
`리스코프 치환 원칙 (Liskov subsitution principle)`에 따르면 어떤 타입에 있어 중요한 속성이라면 그 하위 타입에서도 마찬가지로 중요하다. 따라서 `그 타입의 모든 메서드가 하위 타입에서도 똑같이 잘 작동해야 한다.`

즉, "Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로써 활용될 수 있어야 한다."

구체 클래스의 하위 클래스가 값을 추가할 방법은 없지만 괜찮은 우회 방법이 잇다. "상속 대신 컴포지션을 사용하라"는 아이템 18의 조언을 따르면 된다. Point를 상속하는 대신 Point를 ColorPoint의 private 필드로 두고, colorPoint와 같은 위치의 일반 Point를 반환하는 뷰(view) 메서드를 public으로 추가하는 식이다.

```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    /**
     * Returns the point-view of this color point.
     */
    public Point asPoint() {
        return point;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }

    @Override public int hashCode() {
        return 31 * point.hashCode() + color.hashCode();
    }
}
```

자바 라이브러리에서도 구체 클래스를 확장해 값을 추가한 클래스가 종종 있다. 한 가지 예로 java.sql.Timestamp는 java.util.qDate를 확장한 후 nanoseconds 필드를 추가했다. 그 결과로 Timestamp의 equals는 대칭성을 위반하며, Date 객체와 한 컬렉션에 넣거나 서로 섞어 사용하면 엉뚱하게 동작할 수 있다.
Timestamp를 이렇게 설계한 것은 실수니 절대 따라해서는 안 된다.

> 추상 클래스의 하위 클래스에서라면 equals 규약을 지키면서도 값을 추가할 수 있다. "태그 달린 클래스보다는 클래스 계층 구조를 활용하라" 는 아이템 23의 조언을 따르는 클래스 계층구조에서는 아무 중요한 사실이다. 예컨대 아무런 값을 갖지 않는 추상 클래스인 Shape를 위에 두고, 이를 확장하여 radius 필드를 추가한 Circle 클래스와, length와 width 필드를 추가한 Rectangle 클래스를 만들 수 있다. 상위 클래스를 직접 인스턴스로 만드는게 불가능하다면 지금까지 이야기한 문제들은 일어나지 않는다.

### 4. 일관성

일관성은 두 객체가 같다면 (어느 하나 혹은 두 객체 모두가 수정되지 않는 한) 앞으로도 영원히 같아야 한다는 뜻이다. 가변 객체는 비교 시점에 따라 서로 다를 수도 같을 수도 있는 반면, 불변 객체는 한번 다르면 끝까지 달라야 한다. 클래스를 작성할 때는 불변 클래스로 만드는게 나을지를 심사숙고하자. 불변 클래스로 만들기로 했다면 equals가 한번 같다고 한 객체와는 영원히 같다고 답하고, 다르다고 한 객체와는 영원히 다르다고 답하도록 만들어야 한다.

클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다. 이 제약을 어기면 일관성 조건을 만족시키기가 아주 어려워진다. 예컨대 java.net.URL의 equals는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교한다. 호스트 이름을 IP 주소로 바꾸려면 네트워크를 통해야 하는데 그 결과가 항상 같다고 보장할 수 없다. 이는 URL의 equals가 일반 규약을 어기게 하고, 실무에서도 종종 문제를 일으킨다. URL의 equals를 이렇게 구현한 것은 커다란 실수였으니 절대 따라해서는 안된다. 하위 호환성이 발목을 잡아서 잘못된 동작을 바로잡을 수도 없다. 이런 문제를 피하려면 equals는 항상 메모리에 존재하는 객체만을 사용한 결정적(deterministic) 계산만 수행해야 한다.

### 5. null-false

null-false는 이름과 같이 모든 객체가 null과 같지 않아야 한다는 뜻이다. 의도하지 않았음에도 o.equals(null)이 true를 반환하는 상황은 상상하기 어렵지만, 실수로 NullPointerException을 던지는 코드는 흔할 것이다. 이 일반 규약은 이런 경우도 허용하지 않는다. 수많은 클래스가 다음 코드처럼 입력이 null인지 확인해 자신을 보호한다.

#### 명시적 null 검사 - 필요 없음.
```java
@Override public boolean equals(Object o) {
    if ( o == null )
        return false;
    ...
}
```

이런 검사는 필요하지 않다. `동치성`을 검사하려면 equals는 건네받은 객체를 적절히 형변환한 후 필수 필드들의 값을 알아내야 한다. 그러려면 `형변환에 앞서 instanceof 연산자로 입력 매개변수가 올바른 타입인지 검사해야 한다.`

#### 묵시적 null 검사 - 이쪽이 낫다.
```java
@Override public boolean equals(Object o) {
    if (!(o instanceof MyType))
        return false;
    MyType mt = (MyType) o;
    ...
}
```

equals가 타입을 확인하지 않으면 잘못된 타입이 인수로 주어졌을 때 ClassCastException을 던져서 일반 규약을 위배하게 된다. `그런데 instanceof는 (두 번째 피 연산자와 무관하게) 첫 번째 피연산자가 null이면 false를 반환한다.` 따라서 `입력이 null이면 타입 확인 단계에서 false를 반환하기 때문에 null 검사를 명시적으로 하지 않아도 된다.`

---

### equals 메서드 구현 방법

지금까지의 내용을 종합해서 양질의 equals 메서드 구현 방법을 단계별로 정리해보겠다.

1. **== 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.**
    - 자기 자신이면 true를 반환한다. 이는 단순한 성능 최적화용으로, 비교 작업이 복잡한 상황일 때 값어치를 할 것이다.

2. **instanceof 연산자로 입력이 올바른 타입인지 확인한다. 그렇지 않다면 false를 반환한다.**
    - 이때의 올바른 타입은 equals가 정의된 클래스인 것이 보통이지만, 가끔은 그 클래스가 구현한 특정 인터페이스가 될 수도 있다.
    - 어떤 인터페이스는 자신을 구현한 (서로 다른) 클래스끼리도 비교할 수 있도록 equals 규약을 수정하기도 한다. 이런 인터페이스를 구현한 클래스라면 equals에서 (클래스가 아닌) 해당 인터페이스를 사용해야 한다. Set, List, Map, Map.Entry 등의 컬렉션 인터페이스들이 여기 해당한다.

3. **입력을 올바른 타입으로 형변환한다.**
    - 앞서 2번에서 instanceof 검사를 했기 때문에 이 단계는 100% 성공한다.

4. **입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.**
    - 모든 필드가 일치하면 true를, 하나라도 다르면 false를 반환한다. 2단계에서 인터페이스를 사용했다면 입력의 필드 값을 가져올 때도 그 인터페이스의 메서드를 사용해야 한다. 타입이 클래스라면 해당 필드에 직접 접근할 수도 있다. (접근 권한에 따라)

 float와 double을 제외한 기본 타입 필드는 == 연산자로 비교하고, 참조 타입 필드는 각각의 equals 메서드로, float와 double 필드는 각각 정적 메서드인 Float.compare(float, float)와 Double.compare(double, double)로 비교한다. float와 double을 특별 취급하는 이유는 Float.NaN, -0.0f, 특수한 부동소수 값 등을 다뤄야 하기 때문이다. 배열의 모든 원소가 핵심 필드라면 Arrays.equals 메서드들 중 하나를 사용하자.

 때론 null도 정상 값으로 취급하는 참조 타입 필드도 있다. 이런 필드는 정적 메서드인 Object.equals(Object, Object)로 비교해 NullPointerException 발생을 예방하자.

앞서의 CastInsensitiveString 예처럼 비교하기가 아주 복잡한 필드를 가진 클래스도 있다. 이럴 때는 그 필드의 표준형을 저장해둔 후 표준형끼리 비교하면 훨씬 경제적이다. 이 기법은 특히 불변 클래스에 제격이다. 가변 객체라면 값이 바뀔 때마다 표준형을 최신 상태로 갱신해줘야 한다.

어떤 필드를 먼저 비교하느냐가 equals의 성능을 좌우하기도 한다. 최상의 성능을 바란다면 다를 가능성이 더 크거나 비교하는 싼 필드를 먼저 비교하자. 동기화용 락 필드 같이 객체의 논리적 상태와 관련 없는 필드는 비교하면 안된다. 핵심 필드로부터 계산해낼 수 있는 파생 필드 역시 굳이 비교할 필요는 없지만, 파생 필드를 비교하는 쪽이 더 빠를 때도 있다. 파생 필드가 객체 전체의 상태를 대표하는 상황이 그렇다. 예컨대 자신의 영역을 캐시해두는 Polygon 클래스가 있다고 해보자. 그렇다면 모든 변과 정점을 일일히 비교할 필요 없이 캐시해둔 영역만 비교하면 결과를 곧바로 알 수 있다.

`equals를 다 구현했다면 세 가지만 자문해보자. 대칭적인가? 추이성이 있는가? 일관적인가?` 

자문에서 끝내지 말고 단위테스트를 작성해 돌려보자. 단 equals 메서드를 AutoValue를 이용해 작성했다면 테스트를 생략해도 안심할 수 있다. 세 요건 중 하나라도 실패한다면 원인을 찾아서 고치자. 물론 나머지 요건인 반사성과 null-false 도 만족해야 하지만, 이 둘이 문제되는 경우는 별로 없다.

다음은 이상의 비법에 따라 작성해본 PhoneNumber 클래스용 equals 메서드이다.

#### **전형적인 equals 메서드의 예**
```java
public final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "area code");
        this.prefix   = rangeCheck(prefix,   999, "prefix");
        this.lineNum  = rangeCheck(lineNum, 9999, "line num");
    }

    private static short rangeCheck(int val, int max, String arg) {
        if (val < 0 || val > max)
            throw new IllegalArgumentException(arg + ": " + val);
        return (short) val;
    }

    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PhoneNumber))
            return false;
        PhoneNumber pn = (PhoneNumber)o;
        return pn.lineNum == lineNum && pn.prefix == prefix
                && pn.areaCode == areaCode;
    }

}

```

드디어 마지막 주의사항이다.

- `equals를 재정의할 땐 hashCode도 반드시 재정의하자. (아이템 11)`
- 너무 복잡하게 해결하려 들지 말자.
    - 필드들의 동치성만 검색해도 equals 규약을 어렵지 않게 지킬 수 있다. 오히려 너무 공격적으로 파고들다가 문제를 일으키기도 한다. 일반적으로 별칭 (alias)는 비교하지 않는게 좋다. 예컨대 File 클래스라면, 심볼릭 링크를 비교해 같은 파일을 가리키는지 확ㅇ니하려 들면 안된다. 다행히 File 클래스는 이런 시도를 하지 않는다.
- Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자. 많은 프로그래머가 equals를 다음과 같이 작성해두고 문제의 원인을 찾아 해맨다.

```java
public boolean equals(MyClass o) {
    ...
}
```

이 메서드는 Object.equals를 재정의한 게 아니다. 입력 타입이 Object가 아니므로 재정의한게 아니라 다중정의(아이템 52)한 것이다. 기본 equals를 그대로 둔 채로 추가한 것일지라도, 이처럼 '타입을 구체적으로 명시한' equals는 오히려 해가 된다. 이 메서드는 하위 클래스에서 @Override 애너테이션이 긍정 오류를 내게 하고 보안 측면에서도 잘못된 정보를 준다. 이번 절 예제 코드들에서처럼 @Override 애너테이션을 일관되게 사용하면 이러한 실수를 예방할 수 있다. 예를 들어 다음 equals는 컴파일 되지 않고, 무엇이 문제인지 정확히 알려주는 오류 메세지를 보여줄 것이다.

```java
// 여전히 잘못도니 예 - 컴파일 되지 않음.
@Override public boolean equals(MyClass o) {
    ...
}
```

equals(hashCode도 마찬가지)를 작성하고 테스트하는 일은 지루하고 이를 테스트하는 코드도 항상 뻔하다. 다행히 이 작업을 대신해줄 오픈소스가 있으니, 그 친구는 바로 구글이 만든 AutoValue 프레임워크다. 클래스에 애너테이션 하나만 추가하면 AutoValue가 이 메서드들을 알아서 작성해주며, 여러분이 직접 작성하는 것과 근본적으로 똑같은 코드를 만들어 줄 것이다.

대다수의 IDE도 같은 기능을 제공하지만 생성된 코드가 AutoValue만큼 깔끔하거나 읽기 좋지는 않다. 또한 IDE는 나중에 클래스가 수정된 걸 자동으로 알아채지는 못하니 테스트 코드를 작성해둬야 한다. 이런 단점을 감안하더라도 사람이 직접 작성하는 것보다 IDE에 맡기는 편이 낫다. 적어도 사람처럼 부주의한 실수를 저지르지는 않으니 말이다.

---

### 핵심 정리

```
꼭 필요한 경우가 아니면 equals를 재정이하지 말자. 많은 경우에 Object의 equals가 여러분이 원하는 비교를 정확히 수행해준다. 재정의할 때는 그 클래스의 핵심 필드 모두를 빠짐없이, 다섯 가지 규약을 확실히 지켜가며 비교해야 한다.
```

### 참고 자료
- Effective Java 3/E