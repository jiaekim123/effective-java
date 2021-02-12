# [Effective Java] Item 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

이따금 인스턴스 필드들을 모아놓는 일 외에는 아무 목적도 없는 퇴보한 클래스를 작성하려 할 때가 있다.

#### **이처럼 퇴보한 클래스는 public 이어서는 안된다!**
```java
    public double x;
    public double y;
```

이런 클래스는 데이터 필드에 직접 접근할 수 있으니 캡슐화의 이점을 제공하지 못한다. API 수정하지 않고는 내부 표현을 바꿀 수 없고, 불변식을 보장할 수 없으며, 외부에서 필드에 접근할 때 부수 작업을 수행할 수도 없다. 철저한 객체 지향 프로그래머는 이런 클래스를 상당히 싫어해서 필드를 모두 private으로 바꾸고 public 접근자 (getter)를 추가한다.

#### **접근자와 변경자(mutator) 메서드를 활용해 데이터를 캡슐화한다.**
```java
class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX (double x) { this.x = x; }
    public void setY (double y) { this.y = y; }
}
```

public 클래스에서라면 이 방식이 확실히 맞다. 패키지 바깥에서 접귾라 수 있는 클래스라면 접근자를 제공함으로써 클래스 내부 표현 방식을 언제든 바꿀 수 있는 유연성을 얻을 수 있다. public 클래스가 필드를 공개하면 이를 사용하는 클라이언트가 생겨날 것이므로 내부 표현 방식을 마음대로 바꿀 수 없게 된다.

하지만 package-private 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출한다 해도 하등 문제가 없다. 그 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다. 이 방식은 클래스 선언면에서나 이를 사용하는 클라이언트의 코드 면에서나 접근자 방식보다 훨씬 깔끔하다. 클라이언트 코드가 이 클래스 내부 표현에 묶이기는 하나, 클라이언트도 어차피 이 클래스를 포함하는 패키지 안에서만 동작하는 코드일 뿐이다. 따라서 패키지 바깥 코드는 전혀 손대지 않고도 데이터 표현 방식을 바꿀 수 있다. private 중첩 클래스의 경우라면 수정 범위가 더 좁아져서 이 클래스를 포함한 외부 클래스까지로 제한된다.

자바 플랫폼 라이브러리에서도 public 클래스의 필드를 직접 노출하지 말라는 규칙을 어기는 사례가 종종 있다. 대표적인 예가 java.awt.package 패키지의 Point와 Dimension 클래스다. 이 클래스들을 흉내 내지 말고, 타산지석으로 삼길 바란다. 내부를 노출한 Dimension 클래스의 심각한 성능 문제는 오늘날까지도 해결되지 못했다.

public 클래스의 필드가 불변이라면 직접 노출할 때의 단점은 조금은 줄어들지만, 여전히 결코 좋은 생각은 아니다. API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없단 단점은 여전하다. 단, 불변식은 보장할 수 있게 된다. 예컨대 다음 클래스는 각 인스턴스가 유효한 시간을 표현함을 보장한다.

#### **불변 필드를 노출한 public 클래스 - 과연 좋은가?**
```java
private final class Time {
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    public final int hour;
    public final int minute;

    public Time(int hour, int minute) {
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
    ... // 나머지 생략
}
```

### **핵심 정리**
- public 클래스는 절대 가변 필드를 직접 노출해서는 안 된다. 불변 필드라면 노출해도 덜 위험하지만 완전히 안심할 수는 없다. 하지만 package-private 클래스나 private 중첩 클래스에서는 종종 (불변이든 가변이든) 필드를 노출하는 편이 나을 때도 있다.