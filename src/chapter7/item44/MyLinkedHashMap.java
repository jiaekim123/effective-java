package chapter7.item44;

import java.util.LinkedHashMap;
import java.util.Map;
public class MyLinkedHashMap{
    public static void main(String[] args) {
        // OverrideLinkedHashMap
        Map<Integer, Integer> overrideMap = new OverrideLinkedHashMap<>();
        for (int i = 0; i < 10; i++){
            overrideMap.put(i, i);
        }
        System.out.println(overrideMap.keySet());
        
        // FunctionalLinkedHashMap
        Map<Integer, Integer> functionalMap = new FunctionalLinkedHashMap<>((map, eldest) -> map.size() > 5);
        for (int i = 0; i < 10; i++){
            functionalMap.put(i, i);
        }
        System.out.println(functionalMap);
    }
    private static class OverrideLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > 5;
        }
    }
    private static class FunctionalLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        private EldestEntryRemovalFunction<K, V> eldestEntryRemovalFunction;
        public FunctionalLinkedHashMap(EldestEntryRemovalFunction<K, V> function) {
            this.eldestEntryRemovalFunction = function;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return eldestEntryRemovalFunction.remove(this, eldest);
        }
    }
}
