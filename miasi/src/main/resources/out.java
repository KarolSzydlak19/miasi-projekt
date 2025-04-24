public class MyClass {

    private int myField = 42;

    public void doSomething() {
        int myField = 99; // zmienna lokalna, NIE powinna być zmieniona
        System.out.println(myField);
    }

    public void printField() {
        System.out.println(this.myField); // odwołanie do pola – POWINNO zostać zmienione
    }

    public static void main(String[] args) {
        MyClass obj = new MyClass(); // konstruktor – POWINNO zostać zmienione
        obj.doSomething();
        obj.printField();
    }

    public class MyClass1 {

        private int myField = 42;
        private int doSomething = 42;

        public void doSomething() {
            int myField = 99; // zmienna lokalna, NIE powinna być zmieniona
            System.out.println(myField);
        }

        public void printField() {
            int newRewriter = 99;
            System.out.println(this.myField); // odwołanie do pola – POWINNO zostać zmienione
        }

        public static void main(String[] args) {
            MyClass obj = new MyClass(); // konstruktor – POWINNO zostać zmienione
            obj.doSomething();
            obj.printField();
        }
    }
}
