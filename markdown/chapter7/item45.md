# [Effective Java] item 45. 스트림은 주의해서 사용하라

## 핵심 정리
- 스트림을 사용해야 멋지게 처리할 수 있는 일이 있고, 반복 방식이 더 알맞은 일도 있다. 그리고 수많은 작업이 이 둘을 조합했을 때 가장 멋지게 해결된다.
- 어느 쪽을 선택하든 확고부동한 규칙은 없지만 참고할 만한 지침 정도는 있다. 어느 쪽이 나은지가 확연히 드러나는 경우가 많겠지만, 아니더라도 방법은 있다.
- `스트림과 반복 중 어느 쪽이 나은지 확신하기 어렵다면 둘 다 해보고 더 나은 쪽을 선택하라.`

---

### 스트림
#### 스트림이란?
- 다량의 데이터 처리 작업(순차적이든 병렬적이든)을 돕고자 자바 8에 추가된 개념
- 스트림의 원소들은 어디로부터든 올 수 있다. 대표적으로는 컬렉션, 배열, 파일, 정규 표현식 패턴 매처(matcher), 난수 생성기, 혹은 다른 스트림이 있다.
- 스트림 안의 데이터 원소들은 객체 참조나 기본 타입 값이다. 기본 타입 값으로는 int, long, dobule 이렇게 세 가지를 지원한다.

#### 스트림이 제공하는 추상 개념 핵심 두 가지 
##### 1. 스트림(stream)
- 데이터 원소의 유한 혹은 무한 시퀀스를 뜻하는 개념
##### 2. 스트림 파이프라인(stream pipeline)
- 이 원소들로 수행하는 연산 단계를 포함하는 개념

### 스트림 파이프라인
`소스 스트림 -> 하나 이상의 중간 연산(선택) ->  종단 연산`

#### 중간 연산
- 각 중간 연산은 스트림을 어떠한 방식으로 변환한다. 예컨대 각 원소의 함수를 적용하거나 특정 조건을 만족 못하는 원소를 걸러낼 수 있다. 
- 중간 연산들은 모두 한 스트림을 다른 스트림으로 변환하는데, 변환된 스트림의 원소 타입은 변환 전 스트림의 원소 타입과 같을 수도 있고 다를 수도 있다.

#### 종단 연산
- 마지막 중간 연산이 내놓은 스트림에 최후의 연산을 한다.
- 원소를 정렬해 컬렉션에 담거나, 특정 원소 하나를 선택하거나, 모든 원소를 출력하거나 하는 동작을 할 수 있다.

#### 지연 평가
- 스트림 파이프라인은 `지연 평가(lazy evaluation)`된다. 
- `평가는 종단 연산이 호출될 때 이뤄지며, 종단 연산에 쓰이지 않는 데이터 원소는 계산에 쓰이지 않는다.` 이러한 지연 평가가 무한 스트림을 다룰 수 있게 해주는 열쇠다. 종단 연산이 없는 스트림 파이프라인은 아무것도 하지 않는 명령어인 no-op과 같으니, 종단 연산을 빼먹는 일이 절대 없도록 하자.

#### 스프링 API
- 메서드 연쇄를 지원하는 `플루언트 API(fluent API)`다. 즉, 파이프라인 하나를 구성하는 모든 호출을 연결하여 단 하나의 표현식으로 완성할 수 있다. 파이프라인 여러 개를 연결해 표현식 하나로 만들 수도 있다.
- 기본적으로 스트림 파이프라인은 순차적으로 수행된다. 파이프라인을 병렬로 실행하려면 파이프라인을 구성하는 스트림 중 하나에서 parallel 메서드를 호출해주기만 하면 되나, 효과를 볼 수 있는 상황은 많지 않다.

#### 스프링 API의 사용
- 스프링 API는 다재다능하여 사실상 어떠한 계산이라도 해낼 수 있다. 하지만 할 수 있다는 뜻이지, 해야 한다는 뜻은 아니다. 스트림을 제대로 사용하면 프로그램이 짧고 깔끔해지지만, 잘못 사용하면 읽기 어렵고 유지보수도 힘들어진다. 
- 스트림을 언제 써야하는지를 규정하는 확고부동한 규칙은 없지만, 참고할만한 노하우는 잇다.

##### 사전 하나를 훑어 원소 수가 많은 아나그램 그룹들을 출력한다.
- 사전 파일에서 단어를 읽어 사용자가 지정한 문턱값보다 원소 수가 많은 아나그램(anagram) 그룹을 출력한다.

```java
public class Anagrams {
    public static void main(String[] args) throws IOException{
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        Map<String, Set<String>> groups = new HashMap<>();
        try (Scannar s = new Scanner(dictionary)) {
            while (s.hasNext()) {
                String word = s.next();
                groups.computeIfAbsent(alphabetize(word),
                    (unused) -> new TreeSet<>()).add(word);
            }
        }

        for (Set<String> group : groups.values())
            if (group.size() >= minGroupSize)
                System.out.println(group.size() + ": " + group);
    }

    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
```
- computeIfAbsent 메서드
    - 맵 안에 키가 있는지 찾은 다음, 있으면 단순히 그 키에 매핑된 값을 반환한다. 키가 없으면 건네진 함수 객체를 키에 적용하여 값을 계산해낸 다음 그 키와 값을 매핑해놓고, 계산된 값을 반환한다.
    - computeIfAbsent를 사용하면 각 키에 다수의 값을 매핑하는 맵을 쉽게 구현할 수 있다.

##### 스트림을 과하게 사용했다. - 따라하지 말 것!
이제 다음 프로그램을 살펴보자. 앞의 코드와 같은 일을 하지만 스트림을 과하게 활용한다. 사전 파일을 여는 부분만 제외하면 프로그램 전체가 단 하나의 표현식으로 처리된다. 사전 파일을 여는 부분 외에 프로그램 전체가 단 하나의 표현식으로 처리된다. 사전을 여는 작업을 분리한 이유는 그저 try-with-resources문을 사용해 사전 파일을 제대로 닫기 위해서다.
```java
public class StreamAnagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(
                    groupingBy(word -> word.chars().sorted()
                            .collect(StringBuilder::new,
                                    (sb, c) -> sb.append((char) c),
                                    StringBuilder::append).toString()))
                    .values().stream()
                    .filter(group -> group.size() >= minGroupSize)
                    .map(group -> group.size() + ": " + group)
                    .forEach(System.out::println);
        }
    }
}
```
코드를 이해하기 어려운가? 걱정 말자. 다른 사람도 마찬가지다. 이 코드는 확실히 짧지만 읽기는 어렵다. 특히 스트림에 익숙하지 않은 프로그래머라면 더욱 그럴 것이다. 이처럼 스트림을 과용하면 프로그램이 읽거나 유지보수하기 어려워진다.

다행히 절충 지점이 있다. 다음 프로그램도 앞서의 두 프로그램과 기능은 같지만 스트림을 적당히 사용한다. 그 결과 원래 코드보다 짧을 뿐만 아니라 명확하기까지 하다.

##### 스트림을 적당히 활용하면 깔끔하고 명료해진다.
```java
public class HybridAnagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphabetize(word)))
                    .values().stream()
                    .filter(group -> group.size() >= minGroupSize)
                    .forEach(g -> System.out.println(g.size() + ": " + g));
        }
    }

    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
```

스트림을 전에 본 적 없더라도 이 코드는 이해하기 쉬울 것이다. try-with-resources 블록에서 사전 파일을 열고, 파일의 모든 라인으로 구성된 스트림을 얻는다. 스트림 변수 이름을 words로 지어 스트림 안의 각 원소가 단어(word)임을 명확히 했다. 이 스트림의 파이프라인에는 중간 연산은 없으며, 종단 연산에서는 모든 단어를 수집해 맵으로 모은다. 이 맵은 단어들을 아나그램으로 묶어놓은 것으로, 앞선 두 프로그램이 생성한 맵과 실질적으로 같다. 그 다음으로 이 맵의 values()가 반환한 값으로부터 새로운 `Stream<List<String>>` 스트림을 연다. 이 스트림의 원소는 물론 아나그램 리스트다. 그 리스트들 중 원소가 minGroupSize보다 적은 것은 필터링돼 무시된다. 마지막으로, 종단 연산인 forEach는 살아남은 리스트를 출력한다.

> 람다 매개변수의 이름은 주의해서 정해야 한다. 앞 코드에서 매개변수 g는 사실 group이라고 하는 게 나으나, 종이책의 지면 폭이 부족해 짧게 지었다. 람다에서는 타입 이름을 자주 생략하므로 매개변수의 이름을 잘 지어야 스트림 파이프라인의 가독성이 유지된다.
> 한편, 단어의 철자를 알파벳순으로 정렬하는 일은 별도 메서드인 alphabetize에서 수행했다. 연산에 적절한 이름을 지어주고 세부 구현을 주 프로그램 로직 밖으로 빼내 전체적인 가독성을 높인 것이다. 도우미 메서드를 적절히 활용하는 일의 중요성은 일반 반복문 코드에서보다는 스트림 파이프라인에서 훨씬 크다. 파이프라인에서는 타입 정보가 명시되지 않거나 임시 변수를 자주 사용하기 때문이다.

#### 자바가 char 스트림을 처리하지 않는 이유
alphabetize 메서드도 스트림을 사용해 다르게 구현할 수 있다. 하지만 그렇게 하면 명확성이 떨어지고 잘못 구현할 가능성이 커진다. 심지어 느려질 수도 있다. 자바가 기본 타입인 char용 스트림을 지원하지 않기 때문이다. (그렇다고 자바가 char 스트림을 지원했어야 한다는 뜻은 아니다. 그렇게 하는 것은 불가능했다.) 문제를 직접 보여주는게 나을 것 같아 char 값들을 스트림으로 처리하는 코드를 준비해보았다.

```
"Hello world!".chars().forEach(System:out::print);
```

Hello word!를 출력하리라 기대했겠지만, 7210110810811132119111111410810033을 출력한다. "Hello world!".chars()가 반환하는 스트림의 원소는 char가 아닌 int 값이기 때문이다. 따라서 정숫값을 출력하는 print 메서드가 호출된 것이다. 이처럼 이름이 chars인데 int 스트림을 반환하면 헷갈릴 수 있다. 올바른 print 메서드를 호출하게 하려면 다음처럼 형변환을 명시적으로 해줘야 한다.

```
"Hello world!".chars().forEach(s-> System.out.print((char) x));
```

하지만 `char 값들을 처리할 때는 스트림을 삼가는 편이 낫다.`

#### 어떤 경우에 스트림으로 바꾸는 것이 좋은가?
스트림을 처음 쓰기 시작하면 모든 반복문을 스트림으로 바꾸고 싶은 유혹이 일겠지만, 서두르지 않는 게 좋다. 스트림으로 바꾸는 게 가능할지라도 코드 가독성과 유지보수 측면에서 손해를 볼 수 있기 때문이다. 중간 정도 복잡한 작업에도 (앞서의 아나그램 프로그램처럼) 스트림과 반복문을 적절히 조합하는 게 최선이다. 그러니 `기존 코드는 스트림을 사용하도록 리팩터링하되, 새 코드가 더 나아 보일 때만 반영하자.`

#### 함수 객체로는 할 수 없지만 코드 블록으로는 할 수 있는 일
- 코드 블록에서는 범위 안의 지역변수를 읽고 수정할 수 있다. 하지만 람다에서는 final이거나 사실상 final인 변수만 읽을 수 있고, 지역 변수를 수정하는 건 불가능하다.
- 코드 블록에서는 return문을 사용해 메서드에서 빠져나가거나, break나 continue문으로 블록 바깥의 반복문을 종료하거나 반복을 한 번 건너뛸 수 있다. 또한 메서드 선언에 명시된 검사 예외를 던질 수 있다. 하지만 람다로는 이 중 어떤 것도 할 수 없다.

계산 로직에서 이상의 일들을 수행해야 한다면 스트림과는 맞지 않는 것이다.

#### 스트림으로 안성맞춤인 일
- 원소들의 시퀀스를 일관되게 변환한다.
- 원소들의 시퀀스를 필터링한다.
- 원소들의 시퀀스를 하나의 연산을 사용해 결합한다(더하기, 연결하기, 최솟값 구하기 등)
- 원소들의 시퀀스를 컬렉션에 모은다(아마도 공통된 속성을 기준으로 묶어가며)
- 원소들의 시퀀스에서 특정 조건을 만족하는 원소를 찾는다.

이러한 일 중 하나를 수행하는 로직이라면 스트림을 적용하기에 좋은 후보다.

#### 스트림으로 처리하기 어려운 일
- 한 데이터가 파이프라인의 여러 단계(stage)를 통과할 때 이 데이터의 각 단계에서의 값들에 동시에 접근하는 경우 (스트림 파이프라인은 일단 한 값을 다른 값에 매핑하고 나면 원래의 값은 잃는 구조이기 떄문에)

##### 메르센 소수 프로그램
```java
public class MersennePrimes {
    static Stream<BigInteger> primes() {
        return Stream.iterate(TWO, BigInteger::nextProbablePrime);
    }

    public static void main(String[] args) {
        primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
                .filter(mersenne -> mersenne.isProbablePrime(50))
                .limit(20)
                .forEach(mp -> System.out.println(mp.bitLength() + ": " + mp));
    }
}
```
#### 스트림과 반복 중 어느 쪽을 써야 할지 바로 알기 어려운 작업
##### 데카르트 곱 계산을 반복 방식으로 구현
```java
private static List<Card> newDeck() {
    List<Card> result = new ArrayList<>();
    for (Suit suit : Suit.values())
        for (Rank rank : Rank.values())
            result.add(new Card(suit, rank));
    return result;
}
```

##### 데카르트 곱 계산을 스트림 방식으로 구현
```java
private static List<Card> newDeck() {
    return Stream.of(Suit.values())
            .flatMap(suit ->
                    Stream.of(Rank.values())
                            .map(rank -> new Card(suit, rank)))
            .collect(toList());
}
```

어느 newDeck이 좋아 보이는가? 결국은 개인 취향과 프로그래밍 환경의 문제다. 처음 방식은 더 단순하고 아마 더 자연스러워 보일 것이다. 이해하고 유지보수하기에 처음 코드가 더 편한 프로그래머가 많겠지만, 두 번째인 스트림 방식을 편하게 생각하는 프로그래머도 있다. 스트림과 함수형 프로그래밍에 익숙한 프로그래머라면 스트림 방식이 조금 더 명확하고 그리 어렵지도 않을 것이다. 확신이 서지 않는 독자는 첫 번째 방식을 쓰는 게 더 안전할 것이다. 스트림 방식이 나아 보이고 동료들도 스트림 코드를 이해할 수 있고 선호한다면 스트림 방식을 사용하자.

---

### 참고 자료
- Effective Java 3/E