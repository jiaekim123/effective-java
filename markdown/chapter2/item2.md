# [Effective Java] Item 2. 생성자에 매개변수가 많다면 빌더를 고려해라

---

### 예시

```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
                .calories(100).sodium(35).carbohydrate(27).build();
```

---

정적 팩터리와 생성자에는 똑같은 제약이 하나 있다. 선택적 매개변수가 많을 때 적절히 대응하기 어렵다는 점이다. 식품 포장의 영양 정보를 표현하는 클래스에서 영양 정보는 1회 내용량, 총 2회 제공량, 1회 제공량당 칼로리 같은 필수 항목 몇 개와 총 지방, 트랜스지방, 포화지방, 콜레스테롤, 나트륨 등 총 20개가 넘는 선택 항목으로 이뤄진다. 그런데 대부분 제품은 이 선택 항목중 대다수의 값이 그냥 0이다.

이런 클래스용 생성자 혹은 정적 팩터리는 어떤 모습일까? 프로그래머들은 이럴 때 점층적 생성자 패턴(telescoping constructor pattern)을 즐겨 사용했다. 단, 이 경우 확장하기 어렵다는 단점이 있다. 아래 예시 코드를 보자

### 점층적 생성자 패턴

```java
    public class NutritionFacts {
        private final int servingSize;  // ml, 1회 제공량       (필수)
        private final int servings;     // 회, 총 n회 제공량    (필수)
        private final int calories;     // 1회 제공량당         (선택)
        private final int fat;          // g, 1회 제공량당      (선택)
        private final int sodium;       // mg, 1회 제공량당     (선택)
        private final int carbohydrate; // g, 1회 제공량당      (선택)
    }

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
```

이 클래스의 인스턴스를 만들려면 원하는 매개변수를 모두 포함한 생성자 중 가장 짧은 것을 골라 호출하면 된다.

```java
NutritionFacts cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);
```

보통 이런 생성자는 사용자가 설정하길 원치 않는 매개변수까지 포함하기 쉬운데, 어쩔 수 없이 그런 매개변수에도 값을 지정해줘야 한다. 

`즉, 점층적 생성자 패턴도 쓸 수 있지만, 매개변수의 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다.` 코드를 읽을 때 각 값의 의미가 무엇인지 헷갈릴 것이고, 매개변수가 몇 개인지도 주의해서 세어 보아야 할 것이다. 타입이 같은 매개변수가 연달아 늘어서 있으면 찾기 어려운 버그로 이어질 가능성이 있다. 클라이언트가 실수로 매개변수의 순서를 바꿔 건내줘도 컴파일러는 알아채지 못하고, 결국 런타임에 엉뚱한 동작을 하게 된다.

---

### 자바빈즈 패턴 (JavaBeans pattern)

자바빈즈 패턴은 선택 매개변수가 많을 경우 활용할 수 있는 두 번째 대안이다. 매개변수가 없는 생성자로 객체를 만든 후, 세터(setter) 메서드를 호출해 원하는 매개변수의 값을 설정하는 방식이다.

```java
public class NutritionFacts {
    // 매개변수들은 (기본값이 있다면) 기본값으로 초기화된다.
    private int servingSize = -1;  // ml, 1회 제공량       (필수) 기본값 없음
    private int servings = -1;     // 회, 총 n회 제공량    (필수) 기본값 없음
    private int calories = 0;     // 1회 제공량당         (선택)
    private int fat = 0;          // g, 1회 제공량당      (선택)
    private int sodium = 0;       // mg, 1회 제공량당     (선택)
    private int carbohydrate = 0; // g, 1회 제공량당      (선택)

    public NutritionFacts(){}   
    // 세터 메서드들
    public void setServingSize(int val) { servingSize = val; }
    public void setServings(int val) { servings = val; }
    public void setCalories(int val) { calories = val; }
    public void setFat(int val) { fat = val; }
    public void setSodium(int val) { sodium = val; }
    public void setCarbohydrate(int val) { carbohydrate = val; }
}
```

점층적 생성자 패턴의 단점들이 자바 빈즈에서는 더이상 없다. 코드가 길어지긴 했지만, 인스턴스를 만들기 쉽고 그 결과 더 읽기 쉬운 코드가 되었다.

```java
NutritionFacts cocaCola = NutritionFacts();
cocaCola.setServingSize(240);
cocaCola.setServings(8);
cocaCola.setCalories(100);
cocaCola.setSodium(35);
cocaCola.setCarbohydrate(27);
```

하지만 자바빈즈는 심각한 단점이 있다. `자바빈즈 패턴에서는 객체 하나를 만들려면 메서드를 여러 개 호출해야 하고, 객체가 완전히 생성되기 전까지는 일관성(consistency)이 무너진 상태에 놓이게 된다.

점층적 생성자 패턴에서는 매개변수들이 유효한지를 생자에서만 확인하면 일관성을 유지할 수 있었는데, 그 장치가 완전히 사라진 것이다.

이 일관성이 무너지는 문제 때문에 `자바빈즈 패턴에서는 클래스를 불변으로 만들 수 없으며` 쓰레드 안전성을 얻으려면 프로그래머가 추가 작업을 해주어야 한다.

---

### 빌더 패턴 (Builder pattern)

클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자(혹은 정적 팩터리)를 호출해 빌더 객체를 얻는다. 그런 다음 빌더 객체가 제공하는 일종의 세터 메서드들로 원하는 선택 매개변수들을 설정한다.

```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본값으로 초기화한다.
        private int calories      = 0;
        private int fat           = 0;
        private int sodium        = 0;
        private int carbohydrate  = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings    = servings;
        }

        public Builder calories(int val)
        { calories = val;      return this; }
        public Builder fat(int val)
        { fat = val;           return this; }
        public Builder sodium(int val)
        { sodium = val;        return this; }
        public Builder carbohydrate(int val)
        { carbohydrate = val;  return this; }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize  = builder.servingSize;
        servings     = builder.servings;
        calories     = builder.calories;
        fat          = builder.fat;
        sodium       = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```

NutritionFacts 클래스는 불변이며, 모든 매개변수의 기본값들을 한곳에 모아뒀다. 빌더의 세터 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다. 이런 방식을 메서드 호출이 흐르듯 연결된다는 뜻으로 플루언트 API(fluent API) 혹은 메서드 연쇄(method chaining)라 한다. 다음은 이 클래스를 사용하는 클라이언트 코드의 모습이다.

```java
public static void main(String[] args) {
    NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
            .calories(100).sodium(35).carbohydrate(27).build();
}
```

이 클라이언트 코드는 쓰기 쉽고, 무엇보다도 읽기 쉽다. `빌더 패턴은 (파이썬과 스칼라에 있는)명명된 선택적 매개변수(named optional parameters)를 흉내 낸 것이다.`

핵심이 도드라져 보이도록 유효성 검사 코드는 생략했다. 잘못된 매개변수를 최대한 일찍 발견하려면 빌더의 생성자와 메서드에서 입력 매개변수를 검사하고, build 메서드가 호출하는 생성자에서 여러 매개변수에 걸친 불변식(invariant)을 검사하자. 공격에 대비해 이런 불변식을 보장하려면 빌더로부터 매개변수를 복사한 후 해당 객체 필드들도 검사해야 한다.

검사해서 잘못된 점을 발견하면 어떤 매개변수가 잘못되었는지를 자세히 알려주는 메세지를 담아 IlligalArgumentException을 던지면 된다.

---

### 빌더 패턴 (Builder pattern)과 계층구조

`빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다.` 각 계층의 클래스에 관련 빌더를 멤버로 정의하자. 추상 클래스는 추상 빌더를, 구체 클래스(concrete class)는 구체 빌더를 갖게 한다.

다음은 피자의 다양한 종류를 표현하는 계층 구조의 루트에 놓인 추상 클래스다.

```java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addingTopping(Topping topping){
            topping.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 크래스는 이 메서드를 재정의(overriding)하여 "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}
```

Pizza.Builder 클래스는 `재귀적 타입 한정`을 이용하는 제네릭 타입이다. 여기에 추상 메서드인 self를 더해 하위 클래스에서는 형변환 하지 않고도 메서드 연쇄를 지원할 수 있다. self 타입이 없는 자바를 위한 이 우회 방법을 시뮬레이트한 셀프 타입 관용구라고 한다.

이제 Pizza의 하위 클래스를 만들어보자. 하나는 일반적인 뉴욕 피자이고, 다른 하나는 칼초네(calzone) 피자이다. 뉴욕 피자는 크기(size)를 필수 매개변수로 받고, 칼초네 피자는 소스를 안에 넣을지 선택하는 매개변수를 필수로 받는다.

[뉴욕 피자 클래스]
```java
public class NyPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override public NyPizza build() {
            return new NyPizza(this);
        }

        @Override protected Builder self() { return this; }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```
칼초네 피자 클래스

```java
public class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // 기본 값

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override public Calzone build() {
            return new Calzone(this);
        }

        @Override protected Builder self() { return this; }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```

각 하위 클래스가 빌더가 정의한 build 메서드는 해당하는 구체 하위 클래스를 반환하도록 선언한다. NyPizza.Builder는 NyPizza를 반환하고, Calzone.Builder는 Calzone를 반환한다. 이렇게 하위 클래스의 메서드가 상위 클래스의 메서드가 정의한 반환 타입이 아닌, 그 하위 타입을 반환하는 기능을 공변 반환 타이핑(covariant return typing)이라 한다. 이 기능을 이용하면 클라이언트가 형변환에 신경쓰지 않고도 빌더를 사용할 수 있다.

이렇게 `계층적 빌더`를 사용하는 클라이언트의 코드도 앞선 영양정보 빌더를 사용하는 코드와 다르지 않다. 다음의 클라이언트 측 코드 예는 열거 타입 상수를 정적 임포트 했다고 가정하자.

```java
 NyPizza pizza = new NyPizza.Builder(SMALL)
                .addTopping(SAUSAGE).addTopping(ONION).build();
 Calzone calzone = new Calzone.Builder()
                .addTopping(HAM).sauceInside().build();
```

생성자로는 누릴 수 없는 사소한 이점으로, 빌더를 이용하면 가변인수(varargs)매개변수를 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하면 된다. 아니면 메서드를 여러번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수 있다. `addToping`메서드가 이렇게 구현한 예이다.

빌더 패턴은 상당히 유연하다. 빌더 하나로 여러 객체를 순회하면서 만들 수 있고, 빌더에 넘기는 매개변수에 따라 다른 객체를 만들 수도 있다. 객체마다 부여되는 일련번호와 같은 특정 필드는 빌더가 알아서 채우도록 할 수도 있다.

빌더 패턴에 장점만 있는 것은 아니다. 객체를 만들려면, 그에 앞서 빌더부터 만들어야 한다. 빌더의 생성 비용이 크지는 않지만 성능에 민감한 상황에서는 문제가 될 수 있다. 또한 점층적 생성자 패턴보다는 코드가 장황해서 매개 변수가 4개 이상은 되어야 값어치를 한다. 하지만 API는 시간이 지날수록 매개변수가 많아지는 경향이 있음을 명심하자. 생성자나 정적 팩터리 방식으로 시작했다가 나중에 매개 변수가 많아지면 빌더 패턴으로 전환할 수 있지만, 이전에 만들어둔 생성자와 정적 팩터리가 아주 도드라져 보일 것이다. 그러니 애초에 빌더로 시작하는 편이 나을 때가 많다. 

---

### 불변(immutable)

불변(immutable 혹은 immutability)은 어떠한 변경도 허용하지 않겠다는 뜻으로, 주로 변경을 허용하는 가변(mutable) 객체와 구분하는 용도로 쓰인다. 대표적으로 String 객체는 한번 만들어지면 절대 값을 바꿀 수 없는 불변 객체이다.

한편, 불변식(invariant)는 프로그램이 실행되는 동안, 혹은 정해진 기간 동안 반드시 만족해야 하는 조건을 말한다. 다시 말해 변경을 허용할 수는 있으나 주어진 조건 내에서만 허용한다는 뜻이다. 예컨대 리스트의 크기는 반드시 0 이상이어야 하니, 만약 한순간이라도 음수 값이 된다면 불변식이 깨진 것이다. 또한 기간을 표현하는 Period 클래스에서 start 필드의 값은 반드시 end 필드의 값보다 앞서야 하므로, 두 값이 역전되는 불변식이 깨진 것이다. 

따라서 가변 객체에도 불변식은 존재할 수 있으며, 넓게 보면 불변은 불변식의 극단적인 예라 할 수 있다.

---

### 핵심 정리
생성자나 정적 팩터리가 처리해야 할 매개변수가 많다면 빌더 패턴을 선택하는 게 더 낫다. 매개변수 중 다수가 필수가 아니거나 같은 타입이면 특히 더 그렇다. 빌더는 점층적 생성자보다 클라이언트 코드를 읽고 쓰기가 훨씬 간결하고, 자바빈즈보다 훨씬 안전하다.


---

### 관련 자료 링크
1. 재귀적 타입 한정 - item 30

### 참고 자료
- Effective Java 3/E