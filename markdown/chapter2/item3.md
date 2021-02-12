# [Effective Java] Item 3. private 생성자나 열거 타입으로 싱글턴임을 보증해라

---

### 예시

```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();
    private Elvis() { }
    public static Elvis getInstance() { return INSTANCE; }

    public void leaveTheBuilding() {
        System.out.println("Whoa baby, I'm outta here!");
    }

    // This code would normally appear outside the class!
    public static void main(String[] args) {
        Elvis elvis = Elvis.getInstance();
        elvis.leaveTheBuilding();
    }
}
```

`싱글턴(singleton)이란 인스턴스를 오직 하나만 생성할 수 있는 클래스`를 말한다. 싱글턴의 전형적인 예로는 함수(아이템24)와 같은 무상태(stateless) 객체나 `설계상 유일해야 하는 시스템 컴포넌트`를 들 수 있다. `그런데 클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트를 테스트하기 어려워질 수 있다.` 타입을 인터페이스로 정의한 다음 그 인터페이스를 구현해서 만든 싱글턴이 아니라면 싱글턴 인스턴스를 가짜(mock) 구현으로 대체할 수 없기 때문이다.

싱글턴을 만드는 방식은 보통 둘 중 하나다. `두 방식 모두 생성자는 private 으로 감춰두고, 유일한 인스턴스에 접근할 수 있는 수단으로 public static 멤버
를 하나 마련해둔다.` 우선 public static 멤버가 final 필드인 방식을 살펴보자.

---

### **1. public static 멤버가 final 필드인 방식**

```java
public class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() {...}
    
    public void leaveTheBuilding(){...}
}
```

`private 생성자는 public static final 필드인 Elvis.INSTANCE를 초기화할 때 딱 한번만 호출된다.` public이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다. 

예외는 딱 한가지, 권한이 있는 클라이언트는 리플렉션 API인 AccessibleObject.setAccessble을 사용해 private 생성자를 호출할 수 있다. 이러한 공격을 방어하려면 생성자를 수정하여 두 번째 객체가 생성되려 할 때 예외를 던지게 하면 된다.

---

### **2. 정적 팩터리 방식의 싱글턴**
```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    public static Elvis getInstance() { return INSTANCE; }

    public void leaveTheBuilding() { ... }
}
```

싱글턴을 만드는 두 번째 방법에서는 정적 팩터리 메서드를 public static 멤버로 제공한다.
Elvis.getInstance는 항상 같은 객체의 참조를 반환하므로 제 2의 Elvis 인스턴스란 결코 만들어지지 않는다. (역시 리플렉션을 통한 예외는 똑같이 적용된다.)

---

[1. public static 멤버가 final 필드인 방식]의 큰 장점은 `해당 클래스가 싱글턴임이 API에 명확히 드러난다`는 것이다. public static 필드가 final이니 절대로 다른 객체를 참조할 수 없다. 두 번째 장점은 `간결함`이다.

[2. 정적 팩터리 방식의 싱글턴]의 첫 번째 장점은 `마음이 바뀌면, API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다`는 점이다. `유일한 인스턴스를 반환하던 팩터리 메서드가 호출하는 쓰레드별로 다른 인스턴스를 넘겨주게 할 수도 있다.` 두 번째 장점은 원한다면 `정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있다`는 점이다. 세 번째 장점은 `정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있다`는 점이다. 가령 Elvis::getInstance를 Supplier<Elvis>로 사용하는 식이다. 이러한 장점들이 굳이 필요하지 않다면 public 필드 방식이 좋다.

둘 중 하나의 방식으로 만든 `싱글턴 클래스를 직렬화`하려면 단순히 Serializable을 구현한다고 선언하는 것만으로는 부족하다. 모든 인스턴스 필드를 일시적(transient)이라고 선언하고 readResolve 메서드를 제공해야 한다. 이렇게 하지 않으면 직렬화된 인스턴스를 역직렬화할 때마다 새로운 인스턴스가 만들어진다. 2. 정적 팩터리 방식의 싱글턴의 예에서 가짜 Elvis가 탄생한다는 뜻이다. `가짜 Elvis 탄생을 예방하고 싶다면 Elvis 클래스의 다음의 readResolve 메서드를 추가해야 한다.`

```java
// 싱글턴임을 보장해주는 readResolve 메서드
private Object readResolve(){
    // 진짜 Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에게 맡긴다.
    return INSTANCE;
}
```

### 3. **원소가 하나인 열거 타입을 선언한다.**

```java
public enum Elvis {
    INSTANCE;

    public void leaveTheBuilding() { ... }
}
```

public 필드 방식과 비슷하지만, 더 간결하고 추가 노력 없이 직렬화할 수 있고, 심지어 아주 복잡한 직렬화 상황이나 리플렉션 공격에도 제2의 인스턴스가 생기는 일을 완벽히 막아준다. `조금 부자연스러워 보일 수는 있으나 대부분의 상황에서는 원소가 하나 뿐인 열거 타입이 싱글톤을 만드는 가장 좋은 방법이다.` 단, 만들려는 싱글턴이 Enum 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없다. (열거 타입이 다른 인터페이스를 구현하도록 선언할 수는 있다.)

---


### 관련 자료 링크
1. 리플렉션 API - item 65
    - 구체적인 클래스 타입을 알지 못해도, 그 클래스의 메소드, 타입, 변수들을 접근할 수 있도록 해주는 자바 API
2. 제네릭 싱글턴 팩터리 - item 30
3. 직렬화 - Chapter 12
    - 직렬화(Serialization): 객체를 데이터 스트림으로 만드는 것. (객체에 저장된 데이터를 스트림에 쓰기 위해 연속적인 데이터로 변환하는 것)
    - 역직렬화(Deserialization): 스트림으로부터 데이터를 읽어서 객체를 만드는 것 

### 참고 자료
- Effective Java 3/E