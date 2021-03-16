# [Effective Java] item 63. 문자열 연결은 느리니 주의하라

문자열 연결 연산자(+)는 여러 문자열을 하나로 합쳐주는 편리한 수단이다. 그런데 한 줄짜리 출력값 혹은 작고 크기가 고정된 객체의 문자열 표현을 만들 때라면 괜찮지만, 본격적으로 사용하기 시작하면 성능 저하를 감내하기 어렵다.

`문자열 연결 연산자로 문자열 n개를 잇는 시간은 n의 제곱에 비례한다.` 문자열은 불변이라서 두 문자열을 연결할 경우 양쪽의 내용을 모두 복사해야 하므로 성능 저하는 피할 수 없는 결과다.

예를 들어 다음 메서드는 청구서의 품목(item)을 전부 하나의 문자열로 연결해준다.

##### 문자열 연결을 잘못 사용한 예 - 느리다!
```java
public String statement() {
    String result = "";
    for (int i = 0; i < numItems(); i++)
        result += lineForItem(i); // 문자열 연결
    return result;
}
```

품목이 많을 경우 이 메서드는 심각하게 느려질 수 있다. `성능을 포기하고 싶지 않다면 String 대신 StringBuilder를 사용하자.`

##### StringBuilder를 사용하면 문자열 연결 성능이 크게 개선된다.
```java
public String statement2(){
    StringBuilder b = new StringBuilder(numItems() * LINE_WIDTH);
    for (int i = 0; i < numItems(); i++)
        b.append(lineForItem(i));
    return b.toString();
}
```

자바 6 이후 문자열 연결 성능을 다방면으로 개선했지만, 이 두 메서드의 성능 차이는 여전히 크다. 품목을 100개로 하고 lineForItem이 길이 80인 문자열을 반환하게 하여 내 컴퓨터에서 실행해보니 코드 63-2의 statement2가 6.5배나 빨랐다. statement 메서드의 수행 시간은 품목 수의 제곱이 비례해 늘어나고 statement2는 선형으로 늘어나므로, 품목 수가 늘어날수록 성능 격차고 점점 벌어질 것이다. statement2에서 `StringBuilder를 전체 결를 담기에 충분한 크기로 초기화한 점`을 잊지 말자. 하지만 기본값을 사용하더라도 여전히 5.5배 빨랐다.

## 핵심 정리
- 성능에 신경 써야 한다면 많은 문자열을 연결할 때는 문자열 연결 연산자(+)를 피하자.
- 대신 StringBuilder의 append 메서드를 사용하라. 
- 문자 배열을 사용하거나, 문자열을 (연결하지 않고) 하나씩 처리하는 방법도 있다.

### 참고 자료
- Effective Java 3/E
