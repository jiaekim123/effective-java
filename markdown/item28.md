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