# [Effective Java] item 46. 스트림에서는 부작용 없는 함수를 사용하라

## 핵심정리
- `스트림 파이프라인 프로그래밍의 핵심은 부작용 없는 함수 객체`에 있다. 스트림뿐만 아니라 스트림 관련 객체에 건네지는 모든 함수 객체가 부작용이 없어야 한다.
- `종단 연산 중 forEach는 스트림이 수행한 계산 결과를 보고할 때만 이용해야 한다. 계산 자체에는 이용하지 말자.`
- 스트림을 올바로 사용하려면 수집기를 잘 알아둬야 한다. 가장 중요한 수집기 팩터리는 `toList, toSet, toMap, groupingBy, joining`이다.

---

### 스트림이 가져오는 함수형 프로그래밍의 패러다임
스트림은 그저 또 하나의 API가 아닌, 함수형 프로그래밍에 기초한 패러다임이다. 스트림이 제공하는 표현력, 속도 (상황에 따라서는) 병렬성을 얻으려면 API는 말할 것도 없고 이 패러다임까지 함께 받아들여야 한다.

스트림 패러다임의 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 부분이다. 이때 각 변환 단계는 가능한 한 이전 단계의 결과를 받아 처리하는 순수 함수여야 한다. 순수 함수란 오직 입력만이 결과에 영향을 주는 함수를 말한다.

다른 가변 상태를 참조하지 않고, 함수 스스로도 다른 상태를 변경하지 않는다. 이렇게 하려면 (중간 단계든 종단 단계든) 스트림 연산에 건네는 함수 객체는 모두 부작용(side effect)이 없어야 한다.

다음은 주위에서 종종 볼 수 있는 스트림 코드로, 텍스트 파일에서 단어별 수를 세어 빈도표를 만드는 일을 한다.

##### 스트림 패러다임을 이해하지 못한 채 API만 사용했다 - 따라하지 말 것!
```java
Map<String, Long> freq = new HashMap<>();
try (Stream<String> words = new Scanner(file).token()) {
    words.forEach(wrod -> {
        freq.merge(word.toLowerCase(), 1L, Long::sum);
    });
}
```

무엇이 문제인지 보이는가? 스트림, 람다, 메서드 참조를 사용했고, 결과도 올바르다. 하지만 절대 스트림 코드라 할 수 없다. 스트림 코드를 가장한 반복적 코드다. 스트림 API의 이점을 살리지 못하여 같은 기능의 반복적 코드보다 (조금 더) 길고, 읽기 어렵고, 유지보수에도 좋지 않다. 이 코드의 모든 작업이 종단 연산인 forEach에서 일어나는데, 이때 외부 상태(빈도표)를 수정하는 람다를 실행하면서 문제가 생긴다. forEach가 그저 스트림이 수행한 연산 결과를 보여주는 일 일상을 하는 것(이 예에서는 람다가 상태를 수정함)을 보니 나쁜 코드일 것 같은 냄새가 난다. 이제 올바르게 작성한 모습을 살펴보자.

##### 스트림을 제대로 활용해 빈도표를 초기화한다.
```java
Map<String, Long> freq;
try (Stream<String> words = new Scanner(file).tokens()) {
    freq = words
        .collect(groupingBy(String::toLowerCase, counting()));
}
```

앞서와 같은 일을 하지만, 이번엔 스트림 API를 제대로 사용했다. 그뿐만 아니라 짧고 명확하다. 그런데 forEach를 반복문처럼 사용하는 사람도 분명 있을 것이다. 자바 프로그래머라면 for-each 반복문을 사용할 줄 알고 그 반복문과 forEach 종단 연산이 비슷하게 생겼기 때문에 익숙하기 때문이다. 하지만 forEach 연산은 종단 연산 중 기능이 가장 적고 가장 '덜' 스트림 답다. 대놓고 반복적이라서 병렬화할 수도 없다.

`forEach 연산은 스트림 계산 결과를 보고할 때만 사용하고, 계산하는 데는 쓰지 말자`

물론 가끔은 스트림 계산 결과를 기존 컬렉션에 추가하는 등의 다른 용도로는 쓸 수 있다.

이 코드는 수집기(collector)를 사용하는데, 스트림을 사용하려면 꼭 배워야하는 새로운 개념이다. java.util.stream.Collectors 클래스는 메서드를 무려 39개나 가지고 있고, 그중에는 타입 매개변수가 5개나 되는 것도 있다. 다행히 복잡한 세부 내용은 잘 몰라도 이 API의 장점을 대부분 활용할 수 있다. 익숙해지기 전까지는 Collector 인터세이스를 잠시 잊고, 그저 축소(reduction) 전략을 캡슐화한 블랙박스 객체라고 생각하기 바란다. 여기서 축소는 스트림의 원소들을 객체 하나에 취합한다는 뜻이다. `수집기가 생성하는 객체는 일반적으로 컬렉션이며, 그래서 "collector"라는 이름을 쓴다.`

수집기를 사용하면 스트림의 원소를 손쉽게 컬렉션으로 모을 수 있다. `수집기는 총 세 가지로, toList(), toSet, toCollection(collectionFactory)`가 그 주인공이다. 이들은 차례로 리스트, 집합, 프로그래머가 지정한 컬렉션 타입을 반환한다. 지금까지 배운 지식을 활용해 빈도표에서 가장 흔한 단어 10개를 뽑아내는 스트림 파이프라인을 작성해보자.

##### 빈도표에서 가장 흔한 단어 10개를 뽑아내는 파이프라인
```java
List<String> topTen = freq.keySet().stream()
    .sorted(comparing(freq::get).reversed())
    .limit(10)
    .collect(toList());
```
> 마지막 `toList는 Collectors의 메서드`이다. 이처럼 Collectors의 멤버를 정적 임포트하여 쓰면 스트림 파이프라인 가독성이 좋아져, 흔히들 이렇게 사용한다.

이 코드에서 어려운 부분은 sorted에 넘긴 비교자, 즉 comparing(freq::get), reversed()뿐이다. `comparing 메서드는 키 추출 함수를 받는 비교자 생성 메서드다.` 그리고 한정적 메서드 참조이자, 여기서 키 추출 함수로 쓰인 `freq::get은 입력받은 단어(키)를 빈도표에서 찾아(추출) 그 빈도를 반환한다.`

그런 다음 가장 흔한 단어가 위로 오도록 비교자(comparing)을 역순(reversed)으로 정렬한다(sorted). 여기까지 왔으면 스트림에서 단어 10개를 뽑아 리스트에 담는 일은 식은 죽 먹기다.
> 위 코드에서는 Scanner의 스트림 메서드인 tokens를 사용해 스트림을 얻었다. tokens는 자바 9부터 지원하므로, 그 이전 버전을 사용하는 사람은 어댑터를 이용하여 (Iterator를 구현한) Scanner를 스트림으로 변환할 수 있다.

Collectors의 나머지 36개 메서드들도 알아보자. 이 중 대부분은 스트림을 맵으로 취합하는 기능으로, 진짜 컬렉션에서 취합하는 것보다 훨씬 복잡하다. 스트림의 각 원소는 키 하나와 값 하나에 연관되어 있다. 그리고 다수 스트림 원소가 같은 키에 연관될 수 있다.

[java.util.stream.Collectors API 문서](https://docs.oracle.com/javase/10/docs/api/java/util/stream/Collectors.html)

### toMap 수집기

가장 간단한 맵 수집기는 toMap(keyMapper, valueMapper)로, 보다시피 스트림 원소를 키에 매핑하는 함수와 값에 매핑하는 함수를 인수로 받는다. 이 수집기는 `열거 타입 상수의 문자열 표현을 열거 타입 자체에 매핑하는 fromString을 구현하는데 사용`했다.

##### toMap수집기를 사용하여 문자열을 열거 타입 상수에 매핑한다.
```java
private static final Map<String, Operation> stringToEnum = Stream.of(values()).collect(toMap(Object::toString, e-> e));
```

이 간단한 toMap 형태는 스트림의 각 원소가 고유한 키에 매핑되어 있을 때 적합하다. 스트림 원소 다수가 같은 키를 사용한다면 파이프라인이 IllegalStateException을 던지며 종료될 것이다.

더 복잡한 형태의 toMap이나 groupingBy는 이런 충돌을 다루는 다양한 전략을 제공한다. 예컨대 toMap에 키 매퍼와 값 매퍼는 물론 병합(merge) 함수까지 제공할 수 잇다. 병합 함수의 형태는 `BinaryOperator<U>`이며, 여기서 U는 해당 맵의 값 타입이다. 같은 키를 공유하는 값들은 이 병합 함수를 사용해 기존 값에 합쳐진다. 예컨대 병합 함수가 곱셈이라면 키가 같은 모든 값(키/값 매퍼가 정한다)을 곱한 결과를 얻는다.

인수 3개를 받는 toMap은 어떤 키와 그 키에 연관된 원소들 중 하나를 골라 연관 짓는 맵을 만들 때 유용하다. 예컨대 다양한 음악가의 앨범들을 담은 스트림을 가지고, 음악가와 그 음악가의 베스트 앨범을 연관 짓고 싶다고 해보자. 다음은 이 일을 수행하는 수집기이다.

##### 각 키와 해당 키의 특정 원소를 연관 짓는 맵을 생성하는 수집기
```java
Map<Artist, Album> topHits = albums.collect(toMap(Album:artist, a->a, mapBy(comparing(Album::sales))));
```

여기서 비교자로는 BinaryOperator에서 정적 임포트한 maxBy라는 정적 팩터리 메서드를 사용했다. maxBy는 `Comparator<T>`를 입력받아 `BinaryOperator<T>`를 돌려준다. 이 경우 비교자 생성 메서드인 comparing이 maxBy에 넘겨줄 비교자를 반환하는데, 자신의 키 추출 함수로는 Album::sales를 받았다. 복잡해 보일 수 있지만 매끄럽게 읽히는 코드다. 말로 풀어보자면 "앨범 스트림을 맵으로 바꾸는데, 이 맵은 각 음악가와 그 음악가의 베스트 앨범을 짝지은 것이다"는 이야기다. 놀랍게도 우리가 풀려고 한 문제를 그대로 기술한 코드가 되었다.

인수가 3개인 toMap은 충돌이 나면 마지막 값을 취하는(last-write-wins) 수집기를 만들 때도 유용하다. 많은 스트림의 결과가 비결정적이다. 하지만 매핑 함수가 키 하나에 연결해준 값들이 모두 같을 때, 혹은 값이 다르더라도 모두 허용되는 값일 때 이렇게 동작하는 수집기가 필요하다.

##### 마지막에 쓴 값을 취하는 수집기
```java
toMap(keyMapper, valueMapper, (oldVal, newVal) -> newVal)
```

세 번째이자 마지막 toMap은 네 번째 인수로 맵 팩터리를 받는다. 이 인수로는 EnumMap이나 TreeMap처럼 원하는 특정 맵 구현체를 직접 지정할 수 있다.

이상의 세 가지 toMap에는 변종이 있다. 그 중 toConcurrentMap은 병렬 실행된 후 결과로 ConcurrentHashMap 인스턴스를 생성한다.

### groupingBy 수집기
이번에는 Collector가 제공하는 또 다른 메서드인 groupingBy를 알아보자.

`이 메서드는 입력으로 분류 함수(classifier)를 받고 출력으로는 원소들을 카테고리별로 모아 놓은 맵을 담은 수집기를 반환`한다. 분류 함수는 입력받은 원소가 속하는 카테고리를 반환한다. 그리고 이 카테고리가 해당 원소의 맵 키로 쓰인다. 다중정의된 groupingBy 중 형태가 가장 간단한 것은 분류 함수 하나를 인수로 받아 맵을 반환한다. 반환된 맵에 담긴 각각의 값은 해당 카테고리에 속하는 원소들을 모두 담은 리스트다. 이는 아이템 45의 아나그램 프로그램에서 사용한 바로 그 수집기로, 알파벳화한 단어를 알파벳화 결과가 같은 단어들의 리스트로 매핑하는 맵을 생성했다.

```java
words.collect(groupingBy(word -> alphabetize(word)))
```

groupingBy가 반환하는 수집기가 리스트 외의 값을 갖는 맵을 생성하게 하려면, 분류 함수와 함께 다운스트림 수집기도 명시해야 한다. 다운 스트림 수집기의 역할은 해당 카테고리의 모든 원소를 담은 스트림으로부터 값을 생성하는 일이다. 이 매개변수를 사용하는 가장 간단한 방법은 toSet()을 넘기는 것이다. 그러면 groupingBy는 원소들의 리스트가 아닌 집합(Set)을 값으로 갖는 맵을 만들어낸다.

toSet() 대신 toCollection(collectionFactory)를 건네는 방법도 있다. 예상할 수 있듯이 이렇게 하면 리스트나 집합 대신 컬렉션을 값으로 갖는 맵을 생성한다. 원하는 컬렉션 타입을 선택할 수 있다는 유연성은 덤이다. 다운스트림 수집기로 counting()을 건내는 방법도 잇다. 이렇게 하면 각 카테고리(키)를 (원소를 담는 컬렉션이 아닌) 해당 카테고리에 속하는 원소의 개수(값)와 매핑한 맵을 얻는다.

```java
Map<String, Long> freq = words.collect(groupingBy(String::toLowerCase, counting()));
```

groupingBy의 세 번째 버전은 다운스트림 수집기에 더해 맵 팩터리도 지정할 수 있게 해준다. 참고로 이 메서드는 점층적 인수 목록 패턴에 어긋난다. 즉, mapFactory 매개변수가 downStream 개변수보다 앞에 놓인다. 이 버전의 groupingBy를 사용하면 맵과 그 안에 담긴 컬렉션의 타입을 모두 지정할 수 있다. 예컨대 값이 TreeSet인 TreeMap을 반환하는 수집기를 만들 수 있다.

#### groupingByConcurrent 메서드
이상의 총 세 가지 groupingBy각각에 대응하는 groupingByConcurrent 메서드들도 볼 수 있다. 이름에서 알 수 있듯 대응하는 `메서드의 동시 수행 버전으로, ConcurrentHashMap 인스턴스를 만들어준다.`

#### partitioningBy
많이 쓰이진 않지만, groupingBy의 사촌격인 partitioningBy도 있다. `분류 함수 자리에 프레디키트(predicate)를 받고 키가 Boolean인 맵을 반환`한다. 프레디키트에 더해 다운스트림 수집기까지 입력받는 버전도 다중정의되어 있다.

counting 메서드가 반환하는 수집기는 다운스트림 수집기 전용이다. Stream의 count 메서드를 직접 사용하여 같은 기능을 수행할 수 있으니 collect(counting()) 형태로 사용할 일은 전혀 없다. Collections에는 이런 속성의 메서드가 16개나 더 있다. 그 중 9개의 이름이 summing, averaging, summarizing으로 시작하며, 각각 int, long, double 스트림용으로 하나씩 존재한다. 그리고 다중정의된 reducing 메서드들, filtering, mapping, flatMapping, collectingAndThen 메서드가 있는데, 대부분 프로그래머는 이들의 존재도 모르고 있어도 상관 없다. 설계 관점에서 보면, 이 수집기들은 스트림 기능의 일부를 복제하여 다운 스트림 수집기를 작은 스트림처럼 동작하게 한 것이다.

#### minBy, maxBy
- 인수로 받은 비교자를 이용해 스트림에서 값이 가장 작은, 혹은 가장 큰 원소를 찾아 반환한다. Stream의 min과 max 메서드를 살짝 일반화한 것이자, java.util.function.BinaryOperator의 minBy와 maxBy 메서드가 반환하는 이진 연산자의 수집기 버전이다.

#### joining
이 메서드는 (문자열 등의) CharSequence 인스턴스의 스트림에만 적용할 수 잇다. 이 중 매개변수가 없는 joining은 단순히 원소를 연결(concatenate)하는 수집기를 반환한다. 한편 인수 하나짜리 joining은 CharSequence 타입의 구분문자(delimiter)를 매개변수로 받는다. 연결 부위에 이 구분문자를 삽입하는데, 예컨대 구분문자로 쉼표(,)를 입력하면 CSV 형태의 문자열을 만들어준다(단, 스트림에 쉼표를 이미 포함한 원소가 있다면 구분문자와 구별되지 않으니 유념하자). 인수 3개짜리 joining은 구분문자에 더해 접두문자(prefix)와 접미문자(suffix)도 받는다. 예컨대 접두, 구분, 접미문자를 각각 `[`, `,`, `]`로 지정하여 얻은 수집기는 [came, saw, conquered]처럼 마치 컬렉션을 출력한 듯한 문자열을 생성한다.

### 참고 자료
- Effective Java 3/E