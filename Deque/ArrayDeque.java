public class ArrayDeque<Item> implements Deque<Item> {

    /** Array based list. Array items holds Items.
     *  Int size holds true size of Deque.  Int frontInd is
     *  index that will contain the NEXT front of Deque.
     *  Int backInd is index that will contain the NEXT back of Deque.
     *  @author Suman Tripathy
     */

    private Item[] items;
    private int size, frontInd, backInd;
    private static int RESIZE = 2;

    public ArrayDeque() {
        size = 0;
        items = (Item[]) new Object[8];
        frontInd = items.length-1;
        backInd = 0;
    }

    private void halveArrayCheck() {
        if ((((double) size / items.length) < 0.25) && (items.length > 8)) {
            changeSize(items.length/2);
        }
    }

    private void resetPointers() {
        frontInd = items.length - 1;
        backInd = 0;
    }

    private void changeSize(int capacity) {
        Item[] x = (Item[]) new Object[capacity];

        // If array's front index < back index
        // i.e. |__|f__| 4 | 5 | 6 |b__|__|
        // copies all values to back of Deque
        // (first indices of items)
        if ((frontInd + 1) % items.length <= backInd) {
            System.arraycopy(items, (frontInd + 1) % items.length, x, 0, size);
            frontInd = x.length - 1;
            backInd = size;
        }

        // if front index  > back index
        else {
            int numItemsInFront = items.length - frontInd - 1;
            System.arraycopy(items, frontInd + 1, x, x.length - numItemsInFront, numItemsInFront);
            frontInd = x.length - numItemsInFront - 1;

            if (backInd != 0) {
                System.arraycopy(items, 0, x, 0, backInd);
            }

        }
        items = x;
    }

    @Override
    public void addFirst(Item a) {
        // Check if array is full and if so,
        // increase size before adding last
        if ((size == items.length) || (frontInd == backInd)) {
            changeSize(items.length * RESIZE);
        }

        // Adds item to items at frontInd
        items[frontInd] = a;
        size++;

        // If we just added at position 0, we
        // need to add to back of items now
        if (frontInd == 0) {
            frontInd = items.length - 1;
        }
        else {
            frontInd--;
        }
    }

    @Override
    public void addLast(Item a) {
        // Check if array is full and if so,
        // increase size before adding last
        if ((size == items.length) || (frontInd == backInd)) {
            changeSize(items.length * RESIZE);
        }

        // Add a to items at backInd
        items[backInd] = a;
        size++;
        backInd = (backInd + 1) % items.length;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override

    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = frontInd + 1; i <= (frontInd + 1 + backInd); i++) {
            if (items[i % items.length] != null) {
                System.out.print(items[i % items.length] + " ");
            }
        }
        System.out.println();
    }

    @Override
    public Item removeFirst() {
        // Can't remove from null Deque
        if (isEmpty()) {
            return null;
        }

        frontInd = (frontInd + 1) % items.length;
        Item bye = items[frontInd];
        items[frontInd] = null;
        size--;
        halveArrayCheck();

        if (isEmpty()) {
            resetPointers();
        }
        return bye;
    }

    @Override
    public Item removeLast() {
        if (isEmpty()) {
            return null;
        }

        // If there are no items in "back"
        // we remove the last indexed item in items
        // and set backInd to items.length - 1
        else if (backInd == 0) {
            Item bye = items[items.length-1];
            items[items.length - 1] = null;
            size--;
            backInd = items.length - 1;
            halveArrayCheck();
            return bye;
        }

        // If backInd is != 0
        Item oldLast = items[backInd - 1];
        items[backInd - 1] = null;
        size--;
        backInd--;
        halveArrayCheck();

        if (isEmpty()) {
            resetPointers();
        }

        return oldLast;
    }

    /**
     * Deque index 0 is equivalent to items index frontInd.
     * If Deque is empty, return null.
     */
    @Override
    public Item get(int index) {
        if (isEmpty() || (index >= size)) {
            return null;
        }
        return items[(frontInd + index + 1) % (items.length)];
    }
}
