# [Effective Java] Item 28. 배열보다는 리스트를 사용하라

## 핵심정리

- 배열과 제네릭에는 매우 다른 타입 규칙이 적용된다.
- 배열
    - 공변이고 실체화 된다.
    - 런타임에는 타입 안전하지만 컴파일 타임에는 그렇지 않다.
- 제네릭
    - 불공변이고 타입 정보가 소거된다.
    - 런타임에는 타입 안전하지 않지만 컴파일 타임에는 안전하다.
- 둘을 섞어 쓰다가 컴파일 오류나 경고를 만나면, 가장 먼저 배열을 리스트로 대체하는 방법을 적용해보자.

---

배열과 제네릭 타입에는 중요한 차이 두 가지가 있다.
## 배열은 공변이지만 제네릭은 불공변이다.
1. **배열은 공변이다.**
    - Sub가 Super의 하위 타입이라면 배열 Sub[]은 배열 Super[]의 하위 타입이 된다. (공변, 즉 함께 변한다는 뜻이다.)
2. **제네릭은 불공변이다.**
    - 서로 다른 타입 Type1과 Type2가 있을 때, `List<Type1>`은 `List<Type2>`의 하위 타입도 아니고 상위 타입도 아니다. 

이것만 보면 제네릭에 문제가 있다고 생각할 수도 있지만, 사실 문제가 있는 건 배열 쪽이다. 다음은 문법상 허용되는 코드다.

#### 런타임에 실패하는 코드
```java
Object[] objectArray = new Long[1];
objectArray[0] = "타입이 달라 넣을 수 없다."; // ArrayStoreException
```

#### 컴파일에 실패하는 코드
```java
List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입
ol.add("타입이 달라 넣을 수 없다.");
```

어느 쪽이든 Long용 저장소에 String을 넣을 수 없다. 다만 배열에서는 그 실수를 런타임에야 알게 되지만, 리스트를 사용하면 컴파일할 때 바로 알 수 있다. 여러분도 물론 컴파일 시에 알아채는 쪽을 선호할 것이다.

## 배열은 실체화(reify)되지만 제네릭은 타입 정보가 런타임에 소거(erasure)된다.
1. **배열은 실체화(reify)된다.**
    - 배열은 런타임에도 자신이 담기로 한 원소의 타입을 인지하고 확인한다. 그래서 위 코드에서 Long 배열에 String을 넣으려 하면 ArrayStoreException이 발생한다.
2. **제네릭은 타입 정보가 런타임에는 소거(erasure)된다.**
    - 원소 타입을 컴파일타임에만 검사하며 런타임에는 알 수조차 없다는 뜻이다.
    - 소거는 제네릭이 지원되기 전의 레거시 코드와 제네릭 타입을 함께 사용할 수 있게 해주는 매커니즘으로, 자바 5가 제네릭으로 순조롭게 전환될 수 있도록 해줬다.

---

이상의 주요 차이로 인해 배열과 제네릭은 잘 어우러지지 못한다. 예컨대 배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없다. 즉, 코드를 `new List<E>[]`, `new List<String>[]`, `new E[]`, 식으로 작성하면 컴파일할 때 제네릭 배열 생성 오류를 일으킨다.

## 제네릭 배열을 만들지 못하게 막은 이유는 무엇일까?

제네릭 배열은 타입안전하지 않기 때문이다. 이를 허용한다면 컴파일러가 자동 생성한 형변환 코드에서 런타임에 ClassCastException이 발생할 수 있다. 런타임에 ClassCastException이 발생하는 일을 막아주겠다는 제네릭 타입 시스템의 취지에 어긋나는 것이다.

다음 코드로 구체적인 상황을 살펴보자.

#### 제네릭 배열 생성을 허용하지 않는 이유 - 컴파일 되지 않는다.
```java
List<String>[] stringLists = new List<String>[1];
List<Integer> intList = List.of(42);
Object[] objects = stringLists;
objects[0] = intList;
String s = stringList(0).get[0];
```

제네릭 배열을 생성하는 (1)이 허용된다고 가정해보자. (2)는 원소가 하나인 `List<Integer>`를 생성한다. (3)은 (1)에서 생성한 `List<String>`의 배열을 Object 배열에 할당한다. 배열은 공변이니 아무 문제 없다. (4)는 (2)에서 생성한 `List<Integer>`의 인스턴스를 Object 배열의 첫 원소로 저장한다. 제네릭은 소거 방식으로 구현되어서 이 역시 성공한다. 즉, 런타임에는 `List<Integer>` 인스턴스의 타입은 단순히 List가 되고, `List<Integer>[]` 인스턴스의 타입은 `List[]`가 된다. 따라서 (4)에서도 ArrayStoreException을 일으키지 않는다.

이제부터가 문제다. List<String> 인스턴스만 담겠다고 선언한 stringLists배열에는 지금 `List<Integer>` 인스턴스가 저장되어 있다. 그리고 (5)는 이 배열의 처음 리스트에서 첫 원소를 꺼내려 한다. 컴파일러는 꺼낸 원소를 자동으로 String으로 형변환하는데, 이 원소는 Integer이므로 런타임에 ClassCastException이 발생한다. 이런 일을 방지하려면 (제네릭 배열이 생성되지 않도록) (1)에서 컴파일 오류를 내야 한다.

`E, List<E>, List<String>`같은 타입을 `실체화 불가 타입(non-reifiable type)`이라 한다. 쉽게 말해, `실체화되지 않아서 런타임에는 컴파일타임보다 타입 정보를 적게 가지는 타입이다.`

소거 메커니즘 때문에 매개변수화 타입 가운데 실체화될 수 있는 타입은 `List<?>`와 `Map<?, ?>`같은 비한정적 와일드카드 타입 뿐이다. 배열을 비한정적 와일드카드 타입으로 만들 수는 있지만, 유용하게 쓰일 일은 거의 없다.

배열을 제네릭으로 만들 수 없어 귀찮을 때도 있다. 예컨대 제네릭 컬렉션에서는 자신의 원소 타입을 담은 배열을 반환하는게 보통은 불가능하다(완벽하지는 않지만 대부분의 상황에서 이 문제를 해결해주는 방법을 아이템33에서 설명한다). 보통 제네릭 타입과 가변인수 메서드(varargs method)를 함께 쓰면 해석하기 어려운 경고 메세지를 받게 된다. 가변인수 메서드를 호출할 때마다 가변인수 매개변수를 담을 배열이 하나 만들어지는데, 이때 그 배열의 원소가 실체화 불가 타입이라면 경고가 발생하는 것이다. 이 문제는 @SafeVarags 애너테이션으로 대처할 수 있다.

### 비검사 형변환 경고를 제거하려면 배열 대신 리스트를 쓰자

배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 경고가 뜨는 경우 대부분은 배열인 E[] 대신 컬렉션인 List<E>를 사용하면 해결된다. 코드가 조금 복잡해지고 성능이 살짝 나빠질 수도 있지만 그 대신 타입 안정성과 상호운용성은 더 좋아진다.

생성자에서 컬렉션을 받는 Chooser 클래스를 예로 살펴보자. 이 클래스는 컬렉션 안의 원소 중 하나를 무작위로 선택해 반환하는 choose 메서드를 제공한다. 생성자에 어떤 컬렉션을 넘기느냐에 따라 이 클래스를 주사위판, 매직 8볼, 몬테카를로 시뮬레이션용 데이터 소스 등으로 사용할 수 있다.

다음은 제네릭을 쓰지 않고 구현한 가장 간단한 버전이다.
```java
public class Chooser {
    private final Object[] choiceArray;

    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)]);
    }
}
```

이 클래스를 사용하려면 choose 메서드를 호출할 때마다 반환된 Object를 원하는 타입으로 형변환해야 한다. 혹시나 타입이 다른 원소가 들어 있었다면 런타임에 형변환 오류가 날 것 이다. 뒤에 나올 아이템 29의 조언을 가슴에 새기고 이 클래스를 제네릭으로 만들어보자.

#### Chooser를 제네릭으로 만들기 위한 첫 시도 - 컴파일되지 않는다.
```java
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = choices.toArray();
    }
...
}
```

이 클래스를 컴파일하면 다음의 오류 메세지가 출력된다.
![item28_1](https://user-images.githubusercontent.com/37948906/107368234-a4551980-6b23-11eb-98c4-6c4d0571e8e7.PNG)

걱정할 것 없다. Object 배열을 T배열로 형변환하면 된다.
그런데 이번엔 경고가 뜬다.

![item28_2](https://user-images.githubusercontent.com/37948906/107368359-d0709a80-6b23-11eb-9f33-ea3552729ee4.PNG)

T가 무슨 타입인지 알 수 없으니 컴파일러는 이 형변환이 런타임에도 안전한지 보장할 수 없다는 메세지다. 제네릭에는 원소의 타입 정보가 소거되어 런타임에는 무슨 타입인지 알 수 없음을 기억하자! 그렇다면 이 프로그램은 동작할까? 동작한다! 단지 컴파일러가 안전을 보장하지 못할 뿐이다. 코드를 작성하는 사람이 안전하다고 확신하면 주석을 남기고 애너테이션을 달아 경고를 숨겨도 된다. 하지만 애초에 경고의 원인을 제거하는 편이 훨씬 낫다.

비검사 형변환 경고를 제거하려면 배열 대신 리스트를 사용하면 된다. 다음 Chooser는 오류나 경고 없이 컴파일된다.

## List 기반 Chooser (타입 안정성 확보)

비검사 형변환 경고를 제거하려면 배열 대신 리스트를 쓰면 된다. 다음 Chooser는 오류나 경고 없이 컴파일된다.

```java
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }

    public static void main(String[] args) {
        List<Integer> intList = List.of(1, 2, 3, 4, 5, 6);

        Chooser<Integer> chooser = new Chooser<>(intList);

        for (int i = 0; i < 10; i++) {
            Number choice = chooser.choose();
            System.out.println(choice);
        }
    }
}
```

코드의 양이 조금 늘었고 아마 조금 더 느리겠지만 런타임에 ClassCastException을 만날 일이 없으니 그만한 가치가 있다.

---

### 참고 자료
- Effective Java 3/E