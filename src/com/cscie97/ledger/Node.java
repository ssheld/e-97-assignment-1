package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Node {

    private String hash;

    private Node left, right;

    public Node(String hash) {
        this.hash = hash;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public String getHash() {
        return hash;
    }
}
