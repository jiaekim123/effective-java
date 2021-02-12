# [Effective Java] item 33. 타입 안전 이종 컨테이너를 고려하라

## 핵심 정리

- 컬렉션 API로 대표되는 일반적인 제네릭 형태에서는 한 컨테이너가 다룰 수 있는 타입 매개변수의 수가 고정되어 있다.
- 하지만 컨테이너 자체가 아닌 키를 타입 매개변수로 바꾸면 이런 제약이 없는 타입 안전 이종 컨테이너를 만들 수 있다.
- 타입 안전 이종 컨테이너는 Class를 키로 쓰며, 이런 식으로 쓰이는 Class 객체를 타입 토큰이라 한다. 또한 직접 구현한 키 타입도 쓸 수 있다. 
- 예컨대 데이터베이스의 행(컨테이너)을 표현한 DatabaseRow 타입에는 제네릭타입인 Column<T>를 키로 사용할 수 있다.

---

### 타입 안전 이종 컨테이너 패턴(type safe heterogeneous container pattern)
- 컨테이너 대신 키를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 키를 함께 제공하는 것
- 제네릭 타입 시스템이 값의 타입이 키와 같음을 보장해줌.

#### 타입안전 이종 컨테이너 예시 (Favorites 클래스)
- 각 타입의 Class 객체를 매개변수화한 키 역할로 사용
- 컴파일 타임 타입 정보와 런타임 타입 정보를 알아내기 위해 메서드들이 주고받는 class 리터럴을 타입 토큰(type token)이라 한다.
```java
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), instance);
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
    
    public static void main(String[] args) {
        Favorites f = new Favorites();
        f.putFavorite(String.class, "Java");
        f.putFavorite(Integer.class, 0xcafebabe);
        f.putFavorite(Class.class, Favorites.class);
        String favoriteString = f.getFavorite(String.class);
        int favoriteInteger = f.getFavorite(Integer.class);
        Class<?> favoriteClass = f.getFavorite(Class.class);
        System.out.printf("%s %x %s%n", favoriteString,
                favoriteInteger, favoriteClass.getName());
    }
}
```

Favorites 인스턴스는 타입 안전하다. String을 요청했는데 Integer를 반환하는 일이 절대 없다. 또한 모든 키의 타입이 제각각이라, 일반적인 맵과 달리 여러 가지 타입 원소를 담을 수 있다.

Favorites가 사용하는 private 맵 변수인 favorites의 타입은 `Map<Class<?>, Object>`이다. 비한정적 와일드카드 타입이라 이 맵 안에 아무것도 넣을 수 없다고 생각할 수 있지만, 사실은 그 반대다. 와일드카드 타입이 중첩(nested)되어있다는 점을 깨달아야 한다. 맵이 아니라 키가 와일드카드 타입인 것이다. 이는 모든 키가 서로 다른 매개변수화 타입일 수 있다는 뜻으로, 첫 번째는 `Class<String>`, 두 번째는 `Class<Integer>`식으로 될 수 있다. 

그 다음으로 알아둘 점은 favorates 맵의 값 타입은 단순히 Object라는 것이다. 무슨 뜻인고 하니, 이 맵은 키와 값 사이에 타입 관계를 보증하지 않는다는 말이다. 즉, 모든 값이 키로 명시한 타입임을 보증하지 않는다.

cast 메서드는 형변환 연산자의 동적 버전이다. 이 메서드는 단순히 주어진 인수가 Class 객체가 알려주는 타입의 인스턴스인지를 검사한 다음, 맞다면 그 인수를 그대로 반환하고, 아니면 ClassCastException을 던진다.

다음과 같이 cast의 반환 타입은 Class 객체의 타입 매개변수와 같다. 이것이 정확히 getFavorite 메서드에 필요한 기능으로, T로 비검사 형변환하는 손실 없이도 Favorates를 타입 안전하게 만드는 비결이다.

```java
public class Class<T> {
    T cast(Object obj);
}
```

### Favorites 클래스에는 제약사항 두 가지
#### 1. 악의적인 클라이언트가 Class 객체를 (제네릭이 아닌) 로 타입으로 넘기면 Favorites 인스턴스의 타입 안전성이 쉽게 깨진다. 
- 하지만 이렇게 짜여진 클라이언트 코드에서는 컴파일할 때 비검사 경고가 뜰 것이다.
- 이러한 문제를 해결하기 위해 다음과 같이 동적 형변환을 사용해야 한다.
```java
public <T> void putFavorite(Class<T> type, T instance) {
    favorites.put(Objects.requireNonNull(type), type.cast(instance));
}
```
#### 2. 실체화 불가 타입에는 사용할 수 없다.
- 즉, 즐겨 찾는 String이나 String[]은 저장할 수 있어도 즐겨 찾는 `List<String>`은 저장할 수 없다. 
- `List<String>`용 Class 객체를 얻을 수 없기 때문에 오류가 난다. `List<String>`는 List.class와 같은 Class 객체를 공유하므로, 만약 `List<String>.class`와 `List<Integer>.class`를 허용해서 둘 다 똑같은 타입 객체 참조를 반환한다면 Favorites 객체 내부는 아수라장이 될 것이다.

> 이 두 번째 제약을 슈퍼 타입 토큰(super type token)으로 해결하려는 시도가 있다. 스프링 프레임워크에서는 이 슈퍼 타입 토큰을 ParameterezedTypeReference라는 클래스를 미리 구현하여 사용하고 있다. 
>```java
> Favorites f = new Favorites();
> List<String> pets = Arrays.asList("개", "고양이", "앵무");
> f.putFavorite(new TypeRef<List<String>>(){}, pets);
> List<String> listofStrings = f.getFavorite(new TypeRef<List<String>>(){});
>```


### 참고 자료
- Effective Java 3/E