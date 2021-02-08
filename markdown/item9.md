# [Effective Java] Item 9. try-finally보다는 try-with-resources를 사용하라

---

자바 라이브러리에는 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많ㅇ다. InputStream, OutputStream, java.sql.Connection 등이 좋은 예다. 자원 닫기는 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 한다. 이런 자원 중 상당수가 안전망으로 finalizer를 사용하고 있지만 finalizer는 그리 믿을만하지 못하다.
 전통적으로 자원이 제대로 닫힘을 보장하는 수단으로 try-finally가 쓰였다. 예외가 발생하거나 메서드에서 반환되는 경우를 포함해서 말이다.

### **try-finally (더 이상 자원을 회수하는 최선의 방책이 아님)**

```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```
나쁘지 않지만, 자원을 하나 더 사용한다면 어떨까?

### **try-finally (자원이 둘 이상인 경우)**

```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutPutStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
        out.close();
    } finally {
        in.close();
    }
}
```

try-finally 문을 제대로 사용한 앞의 두 코드 예제에조차 미묘한 결점이 있다. 예외는 try 블록과 finally 블록 모두에서 발생할 수 있다는 점이다. 예를 들어 기기에 물리적인 문제가 생겼을 때, firstLineOfFile 메서드 안의 readLine 메서드가 예외를 던지고, 같은 이유로 close 메서드도 실패할 것이다. 이런 상황이라면 두번째 예외가 첫 번째 예외를 완전히 집어 삼켜 버린다. 그러면 스택 추적 내역에 첫 번째 예외에 관한 내용은 전혀 남지 않게 되어, 실제 시스템의 디버깅을 몹시 어렵게 할 수 있다. 물론 두 번째 예외 대신 첫 번째 예외를 기록하도록 코드를 수정할 수 있지만, 코드가 너무 지저분해서 실제로 그렇게까지 하는 경우는 거의 없을 것이다.

이러한 문제는 자바 7에서 추가된 `try-with-resources` 덕에 모두 해결되었다. 이 구조를 사용하려면 해당 자원이 `AutoCloseable 인터페이스`를 구현해야 한다. 단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스다. 자바 라이브러리와 서드파티 라이브러리들의 수많은 클래스와 인터페이스가 이미 AutoCloseable을 구현하거나 확장해뒀다. 여러분도 닫아야 하는 자원을 뜻하는 클래스를 작성한다면 AutoCloseable을 반드시 구현하길 바란다.

다음은 try-with-resource를 사용해 재작성한 예시다.

### **try-with-resources 자원을 회수하는 최선책**
```java
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```

### **복수의 자원을 처리하는 try-with-resources**
```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try (InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutPutStream(dst)){
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```

try-with-resources 버전이 짧고 읽기 수월할 뿐 아니라 문제를 진단하기도 훨씬 좋다. firstLineOfFile 메서드를 생각해보자. readLine과 close 호출 양쪽에서 예외가 발생하면, close에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록된다. 이처럼 실전에서는 프로그래머에게 보여줄 예욓 ㅏ나만 보존되고 여러 개의 다른 예외가 숨겨질 수도 있다.

이렇게 숨겨진 예외들도 그냥 버려지지 않고, 스택 추적 내역에 '숨겨졌다(suppressed)'는 꼬리표를 달고 출력된다. 또한 자바 7에서 Throwable에 추가된 getSuppressed 메서드를 이용하면 프로그램 코드에서 가져올 수도 있다.

 보통의 try-finally 에서처럼 try-with-resources에서도 catch절을 사용할 수 있다. catch 절 덕에 try문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다.

 다음 코드에서는 firstLineOfFile 메서드를 살짝 수정하여 파일을 열거나 데이터를 읽지 못했을 때 예외를 던지는 대신 기본값을 반환하도록 해봤다. 예가 살짝 어색하더라도 이해해주기 바란다.

### **try-with-resources를 catch절과 함께 쓰는 모습**
 ```java
 static String firstLineOfFile(String path, String defaultVal) {
     try (BufferedReader br = new BufferedReader(
         new FileReader(path))) {
        return br.readLine();             
    } catch (IOException e) {
        return defaultVal;
    }
 }
 ```

---

 ### 핵심 정리
```
꼭 회수해야 하는 자원을 다룰 때는 try-finall 말고, try-with-resources를 사용하자. 예외는 없다. 코드는 더 짧고 분명해지고, 만들어지는 예외 정보도 훨씬 유용하다. try-finally로 작성하면 실용적이지 못할 만큼 코드가 지저분해지는 경우라도, try-with-resources로는 정확하고 쉽게 자원을 회수할 수 있다.
```

### 참고 자료
- Effective Java 3/E