# [Effective Java] item 37. ordinal 인덱싱 대신 EnumMap을 사용하라

## 핵심 정리
- 배열의 인덱스를 얻기 위해 ordinal을 쓰는 것은 일반적으로 좋지 않으니, 대신 EnumMap을 사용하라.
- 다차원 관계는 `EnumMap<..., EnumMap<...>>`으로 표현하라.
- "어플리케이션 프로그래머는 Enum.ordinal을 (웬만해서는) 사용하지 말아야 한다.(아이템 35)"는 일반 원칙의 특수한 사례다.

---

### 배열이나 리스트에서 원소를 꺼낼 때 ordinal 메서드로 인덱스를 얻는 방식

- 정원에 심은 식물들을 배열 하나로 관리하고, 이들을 생애주기(한해살이, 여러해살이, 두해살이)별로 묶는다.
- 생애주기별로 총 3개의 집합을 만들고 정원을 한바퀴 돌며 각 식물을 해당 집합에 넣는다.
- 이 때 어떤 프로그래머는 집합들을 배열 하나에 넣고 생애 주기의 ordinal값을 그 배열의 인덱스로 사용하려 할 것이다.

```java
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override public String toString() {
        return name;
    }
    
    public static void main(String[] args) {
        Plant[] garden = {
            new Plant("Basil",    LifeCycle.ANNUAL),
            new Plant("Carroway", LifeCycle.BIENNIAL),
            new Plant("Dill",     LifeCycle.ANNUAL),
            new Plant("Lavendar", LifeCycle.PERENNIAL),
            new Plant("Parsley",  LifeCycle.BIENNIAL),
            new Plant("Rosemary", LifeCycle.PERENNIAL)
        };

        // Using ordinal() to index into an array - DON'T DO THIS!  (Page 171)
        Set<Plant>[] plantsByLifeCycleArr =
                (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
        for (int i = 0; i < plantsByLifeCycleArr.length; i++)
            plantsByLifeCycleArr[i] = new HashSet<>();
        for (Plant p : garden)
            plantsByLifeCycleArr[p.lifeCycle.ordinal()].add(p);
        // Print the results
        for (int i = 0; i < plantsByLifeCycleArr.length; i++) {
            System.out.printf("%s: %s%n",
                    Plant.LifeCycle.values()[i], plantsByLifeCycleArr[i]);
        }
    }
}
```

```
ANNUAL: [Basil, Dill]
PERENNIAL: [Rosemary, Lavendar]
BIENNIAL: [Carroway, Parsley]
{ANNUAL=[Basil, Dill], PERENNIAL=[Rosemary, Lavendar], BIENNIAL=[Carroway, Parsley]}
{ANNUAL=[Basil, Dill], BIENNIAL=[Carroway, Parsley], PERENNIAL=[Lavendar, Rosemary]}
{ANNUAL=[Basil, Dill], PERENNIAL=[Rosemary, Lavendar], BIENNIAL=[Carroway, Parsley]}
```

#### 문제점
- 배열은 제네릭과 호환되지 않으니 비검사 형변환을 수행해야 하고 깔끔히 컴파일 되지 않는다.
- 배열은 각 인덱스의 의미를 모르니 출력 결과에 직접 레이블을 달아야 한다.
- 정확한 정숫값을 사용한다는 것을 직접 보증해야 한다. 정수는 열거 타입과 달리 타입 안전하지 않기 때문에 잘못된 값을 사용하면 잘못된 동작을 묵묵히 수행하거나 (운이 좋다면) ArrayIndexOutOfBoundsException을 던질 것이다.

#### 해결책
- 배열은 실질적으로 열거 타입 상수를 값으로 매핑하는 일을 한다. 그러면 Map을 사용할 수도 있을 것이다!
- 열거 타입을 키로 사용하도록 설계한 아주 빠른 Map 구현체가 존재하는데 바로 EnumMap이 그 주인공이다.

#### EnumMap을 사용해 데이터와 열거 타입을 매핑한다.
```java
Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle =
        new EnumMap<>(Plant.LifeCycle.class);
for (Plant.LifeCycle lc : Plant.LifeCycle.values())
    plantsByLifeCycle.put(lc, new HashSet<>());
for (Plant p : garden)
    plantsByLifeCycle.get(p.lifeCycle).add(p);
System.out.println(plantsByLifeCycle);
```

#### EnumMap의 장점
- 더 짧고 명료하고 안전하고 성능도 원래 버전과 비등하다.
- 안전하지 않은 형변환을 쓰지 않고, 맵의 키인 열거 타입이 그 자체로 출력용 문자열을 제공하니 출력 결과에 직접 레이블을 달 일도 없다.
- 배열 인덱스를 계산하는 과정에서 오류가 날 가능성도 원천봉쇄된다.
- EnumMap의 성능이 ordinal을 쓴 배열에 비견되는 이유는 그 내부에서 배열을 사용하기 때문이다.
- 내부 구현방식을 안으로 숨겨서 Map의 타입 안전성과 배열의 성능을 모두 얻어낸 것이다.
- 여기서 EnumMap의 생성자가 받는 키 타입의 Class 객체는 한정적 타입 토큰으로, 런타임 제네릭 타입 정보를 제공한다.

#### Stream을 통해 맵 사용하기
- 스트림을 사용해 맵을 관리하면 코드를 더 줄일 수 있다. 다음은 앞 예의 동작을 거의 그대로 모방한 가장 단순한 형태의 스트림 기반 코드다.

##### 스트림을 사용한 코드1 - EnumMap을 사용하지 않는다!
```java
System.out.println(Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle)));
```
이 코드는 EnumMap이 아닌 고유한 맵 구현체를 사용했기 때문에 EnumMap을 써서 얻은 공간과 성능 이점이 사라진다는 문제가 있다. 이 문제를 좀 더 구체적으로 살펴보자. 매개변수 3개짜리 Collectors.groupingBy 메서드는 mapFactory 매개변수에 원하는 맵 구현체를 명시해 호출할 수 있다.


##### 스트림을 사용한 코드2 - EnumMap을 이용해 데이터와 열거 타입을 매핑했다.
```java
System.out.println(Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle,
                    () -> new EnumMap<>(LifeCycle.class), toSet())));
```

스트림을 사용하면 EnumMap만 사용했을 때와 살짝 다르게 동작한다. EnumMap 버전은 언제나 식물의 생애주기당 하나씩의 중첩 맵을 만들지만, 스트림 버전에서는 해당 생애주기에 속하는 식물이 있을 때만 만든다. 예컨대 정원에 한해살이와 여러해살이 식물만 살고 두해살이는 없다면 EnumMap 버전에서는 맵을 3개 만들고 스트림 버전에서는 2개만 만든다.

두 열거 타입 값들을 매핑하느라 ordinal을 (두 번이나) 사용한 배열들의 배열을 본 적이 있을 것이다. 다음은 이 방식을 적용해 두 가지 상태(Phase)를 전이(Transition)와 매핑하도록 구현한 프로그램이다. 예컨대 액체(LIQUID)에서  고체(SOLID)로의 전이는 응고(FREEZE)가 되고 액체에서 기체(GAS)로의 전이는 기화(BOIL)가 된다.

##### 배열들의 배열의 인덱스에 ordinal()을 사용 - 따라하지 말 것!
```java
public enum Phase {
    SOLID, LIQUID, GAS;
    public enum Transition {
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;

        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.
        private static final Transition[][] TRANSITIONS = {
            { null, MELT, SUBLIME },
            { FREEZE, null, BOIL },
            { DEPOSIT, CONDENSE, null},
        };

        // 한 상태에서 다른 상태로의 전이를 반환한다.
        public static Transition from(Phase from, Phase to){
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}
```

멋져 보이지만 겉모습에 속으면 안 된다. 앞서 보여준 간단한 정원 예제와 마찬가지로 컴파일러는 ordinal과 배열 인덱스의 관계를 알 도리가 없다. 즉, Phase나 Phase.Transition 열거 타입을 수정하면서 상전이 표 TRANSITIONS를 함께 수정하지 않거나 실수로 잘못 수정하면 런타임 오류가 날 것이다. Array IndexOutOfBoundsException이나 NullPointerException을 던질 수도 있고, (운이 나쁘면) 예외도 던지지 않고 이상하게 동작할 수도 있다. 그리고 상전이 표의 크기는 상태의 가짓수가 늘어나면 제곱해서 커지며 null로 채워지는 칸도 늘어날 것이다.

다시 말하면 EnumMap을 사용하는 편이 낫다. 전이 하나를 얻으려면 이전 상태(from)와 이후 상태(to)가 필요하니 맵 2개를 중첩하면 쉽게 해결할 수 있다. 안쪽 맵은 이전 상태와 전이를 연결하고 바깥 맵은 이후 상태와 안쪽 맵을 연결한다. 전이 전후의 두 상태를 전이 열거 타입 Transition의 입력으로 받아, 이 Transition 상수들로 중첩된 EnumMap을 초기화하면 된다.

##### 중첩 EnumMap으로 데이터와 열거 타입 쌍을 연결했다.
```java
public enum Phase {
    SOLID, LIQUID, GAS;
    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

        private final Phase from;
        private final Phase to;
        Transition(Phase from, Phase to) {
            this.from = from;
            this.to = to;
        }

        // Initialize the phase transition map
        private static final Map<Phase, Map<Phase, Transition>>
                m = Stream.of(values()).collect(Collectors.groupingBy(t -> t.from,
                () -> new EnumMap<>(Phase.class),
                Collectors.toMap(t -> t.to, t -> t,
                        (x, y) -> y, () -> new EnumMap<>(Phase.class))));
        
        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```
- `Map<Phase, Map<Phase, Transition>>`: "이전 상태에서 '이후 상태에서 전이로의 맵'에 대응시키는 맵" 
    - groupingBy: 전이를 이전 상태(t.from)를 기준으로 묶는다.
    - toMap: 이후 상태(t.to)를 전이에 대응시키는 EnumMap을 생성한다.

##### Phase에 PLASMA라는 상태를 추가하는 경우
```java
public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;
    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),
        IONIZE(GAS, PLASMA), DEPOSIT(PLASMA, GAS);
    ... // 나머지 코드는 동일
    }
}
```

나머지는 기존 로직에서 잘 처리해주어 잘못 수정할 가능성이 극히 작다. 실제 내부에서는 맵들의 맵이 배열들의 배열로 구현되니 낭비되는 공간과 시간도 거의 없이 명확하고 안전하고 유지보수하기 좋다.

---

### 참고 자료
- Effective Java 3/E