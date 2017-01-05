import java.util.LinkedList;

public class LinkedListDeque<Item> implements Deque<Item> {

    private class Node {
        public Item item;
        public Node next, prev;

        public Node (Node p, Item i, Node n) {
            prev = p;
            item = i;
            next = n;
        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node(null, null, null);
    }

    @Override
    public void addFirst(Item x) {
        if (isEmpty()) {
            Node newFront = new Node(sentinel, x, sentinel);
            sentinel.next = newFront;
            sentinel.prev = newFront;
        }
        else {
            Node oldFront = sentinel.next;
            Node newFront = new Node(sentinel, x, oldFront);
            oldFront.prev = newFront;
            sentinel.next = newFront;
        }
        size ++;
    }

    @Override
    public void addLast(Item x) {
        if (isEmpty()) {
            addFirst(x);
        }
        else {
            Node oldEnd = sentinel.prev;
            Node newEnd = new Node(oldEnd, x, sentinel);
            oldEnd.next = newEnd;
            sentinel.prev = newEnd;
            size++;
        }
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
        Node n = sentinel.next;
        String s = "";
        while (n != sentinel) {
            s += n.item + " ";
            n = n.next;
        }
        System.out.print(s);
    }

    @Override
    public Item removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node gone = sentinel.next;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size --;
        return gone.item;
    }

    @Override
    public Item removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node bye = sentinel.prev;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return bye.item;
    }

    @Override
    public Item get(int index) {
        if ((index >= size) || isEmpty()) {
            return null;
        }

        Node n = sentinel.next;
        while (index != 0) {
            n = n.next;
            index--;
        }
        return n.item;
    }

    public Item getRecursive(int index) {
        if ((index > size) || isEmpty()) {
            return null;
        }
        Node first = sentinel.next;
        return helper(index, first);
    }

    private Item helper(int i, Node n) {
        if (i == 0) {
            return n.item;
        }
        return helper(i-1, n.next);
    }
}