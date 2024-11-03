package com.snehasishroy.queue;

class Node {
    public Node prev;
    public Node next;
    public Integer message;

    public Node(Integer message) {
        this.message = message;
    }
}
