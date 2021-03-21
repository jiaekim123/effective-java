package chapter9.item66;

public class JavaProcess {
    public static void printProcessInfo(){
        ProcessHandle processHandle = ProcessHandle.current();
        ProcessHandle.Info processInfo = processHandle.info();
    
        System.out.println("processHandle.pid(): " + processHandle.pid());
        System.out.println("processInfo.arguments(): " + processInfo.arguments());
        System.out.println("processInfo.command(): " + processInfo.command());
        System.out.println("processInfo.startInstant(): " + processInfo.startInstant());
        System.out.println("processInfo.user(): " + processInfo.user());    
    }
    public static void main(String[] args) {
        printProcessInfo();
    }
}