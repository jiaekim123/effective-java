# [Effective Java] item 31. 한정적 와일드카드를 사용해 API 유연성을 높이라

## 핵심 정리

- 조금 복잡해지더라도 `와일드카드 타입을 적용하면 API가 훨씬 유연해진다.` 그러니 널리 쓰일 라이브러리를 작성한다면 반드시 와일드카드 타입을 적절히 사용해줘야 한다. 
- `PECS 공식`을 기억하자. 
    - `생성자(producer)는 extends를 소비자(consumer)는 super를 사용한다.`
    - `Comparable과 Comparator는 모두 소비자라는 사실도 잊지 말자.`

---

아이템 28에서 이야기했듯 `매개변수화 타입은 불공변(invariant)`이다. 즉, 서로 다른 타입 Type1과 Type2가 있을 때 `List<Type1>`은 `List<Type2>`의 하위 타입도 상위 타입도 아니다.

직관적이지 않겠지만 `List<String>`은 `List<Object>`의 하위 타입이 아니라는 뜻인데, 곰곰이 따져보면 사실 이쪽이 말이 된다. `List<Object>`에는 어떤 객체든 넣을 수 있지만 `List<String>`에는 문자열만 넣을 수 있다. 즉, `List<String>`은 `List<Object>`가 하는 일을 제대로 수행하지 못하니 하위 타입이 될 수 없다.(리스코프 치환 원칙에 어긋난다.)

하지만 때론 불공변 방식보다 유연한 무언가가 필요하다. 아이템 29의 Stack 클래스를 떠올려보자. 여기 Stack의 public API를 추려 보았다.

```java
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public boolean isEmpty();
}
```

여기 일련의 원소를 스택에 넣는 메서드를 추가해야 한다고 해보자.

#### 와일드카드 타입을 사용하지 않은 pushAll 메서드 - 결함이 있다!
```java
public void pushAll(Iterable<E> src) {
    for (E e : src)
        push(e);
}
```

이 메서드는 깨끗이 컴파일되지만 완벽하지 않다. Iterable src의 원소 타입이 스택의 원소 타입과 일치하면 잘 작동한다. 하지만 `Stack<Number>`로 선언한 후 pushAll(intVal)을 호출하면 어떻게 될까? 여기서 intVal은 Integer 타입이다.

Integer는 Number의 하위 타입이니 잘 동작한다. 아니, 논리적으로는 잘 동작해야 할 것 같다.

```java
Stack<Number> numberStack = new Stack<>();
Iterable<Integer> integers = ...;
numberStack.pushAll(integers);
```

하지만 실제로는 다음의 오류 메세지가 뜬다. 매개변수화 타입이 불공변이기 때문이다.

![item31_1](https://user-images.githubusercontent.com/37948906/107358865-49b5c080-6b17-11eb-90c1-4c063c58d547.PNG)

다행히 해결책은 있다. 자바는 이런 상황에 대처할 수 있는 한정적 와일드카드 타입이라는 특별한 매개변수화 타입을 지원한다. pushAll의 입력 매개변수 타입은 'E의 Iterable'이 아니라 'E의 하위 타입의 Iterable'이어야 하며, `와일드 카드 타입 Iterable<? extends E>`가 정확히 이런 뜻이다. (사실 extends라는 키워드는 이 상황에 딱 어울리지는 않는다.) 하위 타입이란 자기 자신도 포함하지만, 그렇다고 자신을 확장(extends)한 것은 아니기 때문이다. 

와일드카드 타입을 사용하도록 pushAll 메서드를 수정해보자.
```java
public void pushAll(Iterable<? extends E> src) {
    for (E e : src)
        push(e);
}
```

이번 수정으로 Stack은 물론 이를 사용하는 클라이언트 코드도 말끔히 컴파일된다. Stack과 클라이언트 모두 깔끔히 컴파일되었다는 건 모든 것이 타입 안전하다는 뜻이다.

이제 pushAll과 짝을 이루는 popAll 메서드를 작성ㅎ라 차례다. popAll 메서드는 Stack 안의 모든 원소를 주어진 컬렉션으로 옮겨 담는다. 다음처럼 작성했다고 해보자.

#### 와일드카드 타입을 사용하지 않는 popAll 메서드 - 결함이 있다!
```java
public void popAll(Collection<E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```

이번에도 주어진 컬렉션 원소 타입이 스택의 원소 타입과 일치한다면 말끔히 컴파일되고 문제없이 동작한다. 하지만 이번에도 역시나 완벽하지 않다. `Stack<Number>`의 원소를 Object용 컬렉션으로 옮기려 한다고 해보자. 컴파일과 동작 모두 문제 없을 것 같다. 정말 그럴까?

```java
Stack<Number> numberStack = new Stack<>();
Collection<Object> objects = new ArrayList<>();
numberStack.popAll(objects);
```

이 클라이언트 코드를 앞의 popAll 코드와 함께 컴파일하면 `Collection<Object>는 Collection<Number>의 하위 타입이 아니다`라는 pushall을 사용했을 때와 비슷한 오류가 발생한다.

![item31_2](https://user-images.githubusercontent.com/37948906/107359612-3d7e3300-6b18-11eb-96f5-82248eec931e.PNG)


이번에도 와일드카드 타입으로 해결할 수 있다. 이번에는 popAll 입력 매개변수 타입이 'E의 Collection'이 아니라 'E의 상위 타입의 Collection'이어야 한다. (모든 타입은 자기 자신이 상위타입이다.) 와일드카드 타입을 사용한 `Collection<? super E>`가 정확히 이런 의미다.이를 popAll에 적용해보자.

#### E 소비자(consumer) 매개변수에 와일드카드 타입 적용
```java
public void popAll(Collection<? super E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```

이제 Stack과 클라이언트 코드 모두 말끔히 컴파일된다.

메세지는 분명하다. `유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라.` 

---

한편, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도 좋을 게 없다. 타입을 정확히 지정해야 하는 상황으로, 이때는 와일드카드 타입을 쓰지 말아야 한다.

다음 공식을 외워두면 어떤 와일드카드 타입을 써야 하는지 기억하는데 도움이 될 것이다.

> 팩스(PECS): producer-extends, consumer-super

즉, 매개변수화 타입 `T가 생산자`라면 `<? extends T>`를 사용하고, `소비자`라면 `<? super T>`를 사용하라. 

Stack 예에서 pushAll의 src 매개변수는 Stack이 사용할 E 인스턴스를 생산하므로 src의 적절한 타입은 `Iterable<? extends E>`이다.
한편, popAll의 dst 매개변수는 Stack으로부터 E 인스턴스를 소비하므로 dst의 적절한 타입은 `Collection<? super E>`이다. 

PECS 공식은 와일드카드 타입을 사용하는 기본 원칙이다. 나프탈린과 와들러는 이를 겟풋원칙(Get and Put Principle)으로 부른다.

---

### 참고 자료
- Effective Java 3/E