package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Node {

    /**
     * Hash value for a node in a Merkle tree.
     */
    private String hash;

    /**
     * References to left and right children nodes of a node.
     */
    private Node left, right;

    /**
     * Constructor to create a new node with the given hash.
     * @param hash    Hash value to assign to new node.
     */
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
