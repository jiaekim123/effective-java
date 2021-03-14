# [Effective Java] item 58. 전통적인 for 문보다는 for-each 문을 사용하라

## 전통적인 for문 방식
##### 컬렉션 순회하기 - 더 나은 방법이 있다.
```java
for (Iterator<Element> i = c.iterator(); i.hasNext(); ){
    Element e = i.next();
    ... // e로 무언가를 한다.
}
```
##### 배열 순회하기 - 더 나은 방법이 있다.
```java
for (int i = 0; i < a.length; i++) {
    ... // a[i]로 무언가를 한다.
}
```

- 이 관용구들은 while문보다는 낫지만 가장 좋은 방법은 아니다. 반복자와 인덱스 변수는 모두 코드를 지저분하게 할 뿐 아니라 우리에게 진짜 필요한 건 원소 뿐이다. 
- 더군다나 이처럼 쓰이는 요소 종류가 늘어나면 오류가 생길 가능성이 높아진다.
- 컬렉션이나 배열이냐에 따라 코드 형태가 달라지므로 주의해야 한다.

## for-each문 (enhanced for statement)
- 반복자와 인덱스 변수를 사용하지 않아 코드가 깔끔하고 오류가 날 일도 없다.
- 하나의 관용구로 컬렉션과 배열을 모두 처리할 수 있어서 어떤 컨테이너를 다루는지 신경쓰지 않아도 된다.

##### 컬렉션과 배열을 순회하는 올바른 관용구
```java
for (Element e : elements) {
    ... // e로 무언가를 한다.
}
```
- elements의 각 원소 e에 대해 순회한다.
- 반복 대상이 컬렉션이든 배열이든, for-each문을 사용해도 속도는 그대로다.

## for-each문을 사용하면 좋은 점
##### 버그를 찾아보자
```java
enum Suit { CLUB, DIAMOND, HEART, SPADE }
enum Rank { ACE, DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING}

...
static Collection<Suit> suits = Arrays.asList(Suit.values());
static Collection<Rank> ranks = Arrays.asList(Rank.values());

List<Card> deck = new ArrayList<>();
for (Iterator<Suit> i = suites.iterator(); i.hasNext(); )
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); )
        deck.add(new Card(i.next(), j.next()));

```

- `deck.add(new Card(i.next(), j.next()));`
    - '숫자(Suit) 하나당' 한 번씩만 불려야 하는데, 안쪽 반복문에서 호출되어 '카드(Rank) 하나당' 한 번씩 불리고 있다.
    - 그래서 숫자가 바닥나면 반복문에서 NoSuchElementException을 던진다.

##### 버그를 찾아보자2
```java
enum Face { ONE, TWO, THREE, FOUR, FIVE, SIX }
...
Collection<Face> faces = EnumSet.allOf(Face.class);

for (Iterator<Face> i = faces.iterator(); i.hasNext(); )
    for (Iterator<Face> j = faces.iterator(); j.hasNext(); )
        System.out.println(i.next() + " " + j.next());
```
- 이 프로그램은 예외를 던지진 않지만, 가능한 조합을 ("ONE ONE"부터 "SIX SIX"까지) 단 여섯 쌍만 출력하고 끝난다. (36개의 조합이 나와야 한다).
- 이 문제를 해결하려면 바깥 반복문에 바깥 원소를 저장하는 변수를 하나 추가해야 한다.

##### 문제는 고쳤지만 보기 좋지 않다. 더 나은 방법이 있다!
```java
for (Iterator<Suit> i = suits.iterator(); i.hasNext(); ) {
    Suit suit = i.next();
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); )
        deck.add(new Card(suit, j.next())));
}
```

for-each 문을 중첩하는 것으로 이 문제는 간단히 해결된다. 코드도 놀랄 만큼 간결해진다.

##### 컬렉션이나 배열의 중첩 반복을 위한 권장 관용구
```java
for (Suit suit : suits)
    for (Rank rank : ranks)
        deck.add(new Card(suit, rank));
```

## for-each문을 사용할 수 없는 경우
하지만 안타깝게도 for-each 문을 사용할 수 없는 상황이 세 가지 존재한다.

- 파괴적인 필터링(destructive filtering)
    - 컬렉션을 순회하면서 선택된 원소를 제거해야 한다면 반복자의 remove 메서드를 호출해야 한다.
    - 자바 8부터는 Collection의 removeIf 메서드를 사용해 컬렉션을 명시적으로 순회하는 일을 피할 수 있다.
- 변형 (transforming)
    - 리스트나 배열을 순회하면서 그 원소의 값 일부 혹은 전체를 교환해야 한다면 리스트의 반복자나 배열의 인덱스를 사용해야 한다.
- 병렬 반복(parallel iteration)
    - 여러 컬렉션을 병렬로 순회해야 한다면 각각의 반복자와 인덱스 변수를 사용해 엄격하고 명시적으로 제어해야 한다.


세 가지 상황 중 하나에 속할 때는 일반적인 for문을 사용하되 이번 아이템에서 언급된 문제들을 경계하기 바란다.

for-each 문은 컬렉션과 배열은 물론 Iterable 인터페이스를 구현한 객체라면 무엇이든 순회할 수 있다. Iterable을 구현하는 쪽으로 고민해보기 바란다.

해당 타입에서 Collection 인터페이스는 구현하지 않기로 했더라도 말이다. Iterable을 구현해두면 그 타입을 사용하는 프로그래머가 for-each 문을 사용할 때마다 여러분께 감사해야 할 것이다.

## 핵심 정리
- 전통적인 for 문과 비교했을 때 for-each 문은 명료하고, 유연하고, 버그를 예방해준다. 성능저하도 없다!
- 가능한 모든 곳에서 for문이 아닌 for-each 문을 사용하자.

### 참고 자료
- Effective Java 3/E