# [Effective Java] Item 18. 상속보다는 컴포지션을 사용하라

상속은 코드를 재사용하는 강력한 수단이지만, 항상 최선은 아니다. 잘못 사용하면 오류를 내기 쉬운 소프트웨어를 만들게 된다. 상위 클래스와 하위 클래스를 모두 같은 프로그래머가 통제하는 패키지 안에서라면 상속도 안전한 방법이다. 확장할 목적으로 설계되었고 문서화도 잘 된 클래스도 마찬가지다. 하지만 일반적인 구체 클래스를 패키지 경계를 넘어, 즉 다른 패키지의 구체 클래스를 상속하는 일을 위험하다. 

> 이 책에서 논하는 상속은 클래스가 다른 클래스를 확장하는 구현 상속을 의미한다. 인터페이스 상속과는 무관하다.

`메서드 호출과 달리 상속은 캡슐화를 깨뜨린다.`

다르게 말하면, 상위 클래스가 어떻게 구현되느냐에 따라 하위 클래스의 동작에 이상이 생길 수 있다. 상위 클래스는 릴리즈마다 내부 구현이 달라질 수 있으며, 그 여파로 코드 한 줄 건드리지 않은 하위 클래스가 오동작할 수 있다는 말이다. 이러한 이유로 상위 클래스 설계자가 확장을 충분히 고려하고 문서화도 제대로 해두지 않으면 하위 클래스는 상위 클래스의 변화에 발맞춰 수정돼야만 한다.

구체적인 예를 살펴보자. 우리에게 HashSet을 사용하는 프로그램이 있다. 성능을 높이려면 이 HashSet은 처음 생성된 이후 원소가 몇 개 더해졌는지 알 수 있어야 한다(HashSet의 현재 크기와는 다른 개념이다. 현재 크기는 원소가 제거되면 줄어든다.) 그래서 코드 18-1과 같이 변형된 HashSet을 만들어 추가된 원소의 수를 저장하는 변수와 접근자 메서드를 추가했다. 그런 다음 HashSet에 원소를 추가하는 메서드인 add와 addAll을 재정의했다.

#### **잘못된 예 - 상속을 잘못 사용했다!**
```java
public class InstrumentedHashSet<E> extends HashSet<E> {
    // The number of attempted element insertions
    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```

이 클래스는 잘 구현된 것처럼 보이지만 제대로 작동하지 않는다. 이 클래스의 인스턴스에 addall 메서드로 원소 3개를 더했다고 해보자. 다음 코드는 자바 9부터 지원하는 정적 메서드인 List.of로 리스트를 생성했다. 그 전 버전을 사용하는 독자는 Arrays.asList를 사용하면 된다.

```java
    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(List.of("Snap", "Crackle", "Pop"));
        System.out.println(s.getAddCount());
    }
```

이제 getAddCount 메서드를 호출하면 3을 반환하리라 기대하겠지만, 실제로는 6을 반환한다. 어디서 잘못된 것일까? 그 원인은 HashSet의 addAll 메서드가 add메서드를 사용해 구현된 데 있다. 이런 내부 구현 방식은 HashSet 문서에는 (당연히) 쓰여 있지 않다. InstrumentedHashSet의 addAll은 addCount에 3을 더한 후 HashSet의 addAll 구현을 호출했다. HashSet의 AddAll은 각 원소를 add메서드를 호출해 추가하는데, 이때 불리는 add는 InstrumentedHashSet에서 재정의한 메서드다. 따라서 addCount에 값이 중복해서 더해져, 최종값이 6으로 늘어난 것이다. addAll로 추가한 원소 하나당 2씩 늘어났다.

이 경우 하위 클래스에서 addAll 메서드를 재정의하지 않으면 문제를 고칠 수 있다. 하지만 당장은 제대로 동작할지 모르나, HashSet의 addAll이 add메서드를 이용해 구현했음을 가장한 해법이라는 한계를 지닌다. 이처럼 자신의 다른 부분을 사용하는 '자기 사용(self-use)'여부는 해당 클래스의 내부 구현 방식에 해당하며, 자바 플랫폼 전반적인 정책인지, 그래서 다음 릴리즈에서도 유지가 될지 알 수 없다. 따라서 이런 가정에 기댄 InstrumentedHashSet도 깨지기 쉽다.

addAll 메서드를 다른 식으로 재정의할 수도 있다. 예컨대 주어진 컬렉션을 순회하며 원소 하나당 add 메서드를 한 번만 호출하는 것이다. 이 방식은 HashSet의 addAll을 더 이상 호출하지 않으니 addAll이 add를 사용하는지와 상관없이 결과가 옳다는 점에서 조금은 나은 해법이다. 하지만 여전히 문제는 남는다. 상위 클래스의 메서드 동작을 다시 구현하는 이 방식은 어렵고, 시간도 더 들고, 자칫 오류를 내거나 성능을 떨어뜨릴 수도 있다. 또한 하위 클래스에서는 접근할 수 없는 private 필드를 써야 하는 상황이라면 이 방식으로는 구현자체가 불가능하다.

하위 클래스가 깨지기 쉬운 이유는 더 있다. 다음 릴리즈에서 상위 클래스에 새로운 메서드를 추가한다면 어떨까? 보안 때문에 컬렉션에 추가된 모든 원소가 특정 조건을 만족해야만 하는 프로그램을 생각해보자. 그 컬렉션을 상속하여 원소를 추가하는 모든 메서드를 재정의해 필요한 조건을 먼저 검사하게끔 하면 될 것 같다. 하지만 이 방식이 통하는 것은 상위 클래스에 똗 라느 원소 추가 메서드가 만들어지기 전까지다. 다음 릴리즈에서 우려한 일이 생기면, 하위 클래스에서 재정의하지 못한 그 새로운 메서드를 사용해 '허용되지 않은' 원소를 추가할 수 있게 된다. 실제로도 컬렉션 프레임워크 이전부터 존재하던 Hashtable과 Vector를 컬렉션 프레임워크에 포함시키자 이와 관련된 보안 구멍들을 수정해야 하는 사태가 벌어졌다.

이상의 두 문제 모두 메서드 재정의가 원인이었다. 따라서 클래스를 확장하더라도 메서드를 재정의하는 대신 새로운 메서드를 추가하면 괜찮으리라 생각할 수도 있다.이 방식이 훨씬 안전한 것은 맞지만, 위험이 전혀 없는 것은 아니다. 다음 릴리즈에서 상위 클래스에서 새 메서드가 추가됐는데 운 없게도 하필 여러분이 하위 클래스에 추가한 메서드와 시그니처가 같고 반환 타입은 다르다면 여러분의 클래스는 컴파일조차 되지 않는다. 혹, 반환 타입마저 같다면 상위 클래스의 새 메서드를 재정의한 꼴이니 앞서의 문제와 똑같은 상황에 부닥친다.

문제는 여기서 그치지 않는다. 여러분이 이 메서드를 작성할 때는 상위 메서드는 존재하지도 않았으니, 여러분이 만든 메서드는 상위 클래스의 메서드가 요구하는 규약을 만족하지 못할 가능성이 크다.

다행히 이상의 문제를 모두 피해가는 묘안이 있다. `기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 private 필드로 기존 클래스의 인스턴스를 참조하게 하자.` 기존 클래스가 새로운 클래스의 구성요소로 쓰인다는 뜻에서 이러한 설계를 `컴포지션(composition; 구성)`이라 한다.

`새 클래스의 인스턴스 메서드들은 (private 필드로 참조하는) 기존 클래스의 대응하는 메서드를 호출해 그 결과를 반환`한다. 이 방식을 `전달(forwarding)`이라 하며, 새 클래스의 메서드들을 `전달 메서드(forwarding method)`라 부른다. 그 결과 새로운 클래스는 기존 클래스의 내부 구현 방식의 영향에서 벗어나며, 심지어 기존 클래스에 새로운 메서드가 추가되더라도 전혀 영향받지 않는다. 구체적인 예시를 위해 InstrumentedHashSet을 컴포지션과 전달 방식으로 다시 구현한 코드를 준비했다. 이번 구현은 둘로 나누어보았다. 하나는 집합 클래스 자신이고, 다른 하나는 전달 메서드만으로 이뤄진 재사용 가능한 전달 클래스다.

#### **래퍼 클래스 - 상속 대신 컴포지션을 사용했다.**
```java
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }
    @Override public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }
    public int getAddCount() {
        return addCount;
    }

    public static void main(String[] args) {
        InstrumentedSet<String> s = new InstrumentedSet<>(new HashSet<>());
        s.addAll(List.of("Snap", "Crackle", "Pop"));
        System.out.println(s.getAddCount());
    }
}
```

#### **재사용할 수 있는 전달 클래스**
```java
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s; }

    public void clear()               { s.clear();            }
    public boolean contains(Object o) { return s.contains(o); }
    public boolean isEmpty()          { return s.isEmpty();   }
    public int size()                 { return s.size();      }
    public Iterator<E> iterator()     { return s.iterator();  }
    public boolean add(E e)           { return s.add(e);      }
    public boolean remove(Object o)   { return s.remove(o);   }
    public boolean containsAll(Collection<?> c)
                                   { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c)
                                   { return s.addAll(c);      }
    public boolean removeAll(Collection<?> c)
                                   { return s.removeAll(c);   }
    public boolean retainAll(Collection<?> c)
                                   { return s.retainAll(c);   }
    public Object[] toArray()          { return s.toArray();  }
    public <T> T[] toArray(T[] a)      { return s.toArray(a); }
    @Override public boolean equals(Object o)
                                       { return s.equals(o);  }
    @Override public int hashCode()    { return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}
```

InstrumentedSet은 HashSet의 모든 기능을 정의한 Set 인터페이스를 활용해 설계되 견고하고 아주 유연하다. 구체적으로는 Set 인터페이스를 구현했고, Set의 인스턴스를 인수로 받는 생성자를 하나 제공한다. 임의의 Set에 계측 기능을 덧씌워 새로운 Set을 만드는 것이 이 클래스의 핵심이다. 상속 방식은 구체 클래스 각각을 따로 확장해야 하며, 지원하고 싶은 상위 클래스의 생성자 각각에 대응하는 생성자를 별도로 정의해주어야 한다. 하지만 지금 선보인 컴포지션 방식은 한 번만 구현해두면 어떠한 Set 구현체라도 계측할 수 있으며, 기존 생성자들과도 함께 사용할 수 있다.

```java
Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));
```

InstrumentedSet을 이용하면 대상 Set 인스턴스를 특정 조건하에서만 임시로 계측할 수 있다.

```java
static void walk(Set<Dog> dogs) {
    InstrumentedSet<Dog> iDogs = new IstrumentedSet<>(dogs);
    ... // 이 메서드에서는 dogs 대신 iDogs를 사용한다.
}
```

다른 Set 인스턴스를 감싸고(wrap) 있다는 뜻에서 InstrumentedSet 같은 클래스를 `래퍼 클래스`라고 하며, 다른 Set에 계측 기능을 덧씌운다는 뜻에서 `데코레이터 패턴(Decorator pattern)`이라고 한다. 컴포지션과 전달의 조합은 넓은 의미로 위임(delegation)이라고 부른다. 단, 엄밀히 따지면 래퍼 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우만 위임에 해당한다.

래퍼 클래스는 단점이 거의 없다. 한 가지, 래퍼클래스가 콜백(callback) 프레임워크와는 어울리지 않다는 점만 주의하면 된다. 콜백 프레임워크에서는 자기 자신의 참조를 다른 객체에 넘겨서 다음 호출(콜백) 때 사용하도록 한다. 내부 객체는 자신을 감사고 있는 래퍼의 존재를 모르니 대신 자신(this)의 참조를 넘기고, 콜백 때는 래퍼가 아닌 내부 객체로 호출하게 된다. 이를 SELF 문제라고 한다. 전달 메서드가 성능에 주는 영향이나 래퍼 객체가 메모리 사용량에 주는 영향을 걱정하는 사람들도 있지만, 실전에서는 둘 다 별다른 영향이 없다고 밝혀졌다. 전달 메서드들을 작성하는게 지루하겠지만, 재사용할 수 있는 전달 클래스를 인터페이스당 하나씩만 만들어두면 원하는 기능을 덧씌우는 전달 클래스들을 아주 손쉽게 구현할 수 있다. 좋은 예로, 구아바는 모든 컬렉션 인터페이스용 전달 메서드를 전부 구현해두었다.

상속은 반드시 하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 쓰여야 한다. 다르게 말하면, 클래스 B가 클래스A와 is-a 관계일 때만 클래스 A를 상속해야 한다. 클래스 A를 상속하는 클래스 B를 작성하려 한다면 B가 정말 A인가? 라고 자문해보자. "그렇다"고 확신할 수 없다면 B는 A를 상속해서는 안된다. 대답이 "아니다"라면 A를 private 인스턴스로 두고, A와는 다른 API를 제공해야 하는 상황이 대다수다. 즉, A는 B의 필수 구성요소가 아니라 구현하는 방법 중 하나일 뿐이다.

자바 플랫폼 라이브러리에서도 이 원칙을 명백히 위반한 클래스를을 찾아볼 수 있다. 예를 들어, 스택은 벡터가 아니므로 Stack은 Vector를 확장해서는 안 됐다. 마찬가지로, 속성 목록도 해시 테이블이 아니므로 Properties도 Hashtable을 확장해서는 안 됐다. 두 사례 모두 컴포지션을 사용했다면 더 좋았을 것이다.

컴포지션을 써야 할 상황에서 상속을 사용하는 건 내부 구현을 불필요하게 노출하는 꼴이다. 그 결과 API가 내부 구현에 묶이고 그 클래스의 성능도 영원히 제한된다. 더 심각한 문제는 클라이언트가 노출된 내부에 직접 접근할 수 있다는 점이다. 다른 문제는 접어두더라도, 사용자를 혼란스럽게 할 수 있다. 예컨대 Properties의 인스턴스인 p가 있을 때, p.getProperty(key)와 p.get(key)는 결과가 다를 수 있다. 전자가 Properties의 깁노 동작인 데 반해, 후자는 Properties의 상위 클래스인 Hashtable로부터 물려받은 메서드이기 때문이다. 가장 심각한 문제는 클라이언트에서 상위 클래스를 직접 수정하여 하위 클래스의 불변식을 해칠 수 있다는 사실이다. 예컨대 Properties는 키와 값으로 문자열만 허용하도록 설계하려 했으나, 상위 클래스인 Hashtable의 메서드를 직접 호출하면 이 불변식을 깨버릴 수 있다. 불변식이 한번 깨지면 load와 store같은 다른 Properties API는 더 이상 사용할 수 없다. 이 문제가 밝혀졌을 때는 이미 수많은 사용자가 문자열 이외의 타입을 Properties의 키나 값으로 사용하고 있었다. 문제를 바로잡기에는 너무 늦어버린 것이다.

컴포지션 대신 상속을 사용하기로 결정하기 전에 마지막으로 자문해야 할 질문을 소개한다. 확장하려는 클래스의 API에 아무런 결함이 없는가? 결함이 있다면 이 결함이 여러분의 클래스의 API까지 전파돼도 괜찮은가? 컴포지션으로 이런 결함을 숨기는 새로운 API를 설계할 수 있지만, 상속은 상위 클래스의 API를 '그 결함까지도'그대로 승계한다.

---

### 참고 자료
- Effective Java 3/E