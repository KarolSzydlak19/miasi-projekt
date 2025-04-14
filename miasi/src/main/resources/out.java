public class newRewriter {

    private int myField = 42;

    public void doSomething() {
        int myField = 99; // zmienna lokalna, NIE powinna być zmieniona
        System.out.println(myField);
    }

    public void printField() {
        System.out.println(this.newRewriter); // odwołanie do pola – POWINNO zostać zmienione
    }

    public static void main(String[] args) {
        newRewriter obj = new newRewriter(); // konstruktor – POWINNO zostać zmienione
        obj.doSomething();
        obj.printField();
    }
}
