package com.cscie97.ledger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Stephen Sheldon
 **/
public class MerkleTree {

    /**
     * The root node of the Merkle tree
     */
    private Node root;

    /**
     * Method to create a merkle tree from a transaction hash list.
     * @param hashList the hash list containing the transactions within a block.
     * @return
     */
    public MerkleTree createTree(List<Node> hashList) {

        List<Node> tempList;

        Node tempNode;

        // Case - More than 2 nodes in list and size if even
        if (hashList.size() > 2 && hashList.size() % 2 == 0) {

            tempList = new ArrayList<>();

            for (int i = 0; i < hashList.size(); i += 2) {
                tempNode = new Node(createHash(hashList.get(i), hashList.get(i+1)));
                tempNode.setLeft(hashList.get(i));
                tempNode.setRight(hashList.get(i+1));
                tempList.add(tempNode);
            }
            createTree(tempList);
        }
        // Case - More than 2 nodes in list and size is odd
        else if (hashList.size() > 2 && hashList.size() % 2 == 1) {

            tempList = new ArrayList<>();

            for (int i = 0; i < hashList.size()-1; i += 2) {
                tempNode = new Node(createHash(hashList.get(i), hashList.get(i+1)));
                tempNode.setLeft(hashList.get(i));
                tempNode.setRight(hashList.get(i+1));
                tempList.add(tempNode);
            }

            // Add the last node to list before recursive call
            tempList.add(hashList.get(hashList.size()-1));
            createTree(tempList);
        } else {

            // Case - Base case when we reach 2 nodes in list
            tempNode = new Node(createHash(hashList.get(0), hashList.get(1)));
            tempNode.setLeft(hashList.get(0));
            tempNode.setRight(hashList.get(1));
            root = tempNode;
        }
        return this;
    }

    /**
     * Method to create a hash by taking the hash of two nodes and hashing it.
     * @param left    The left node.
     * @param right   The right node.
     * @return        The string hash value.
     */
    private String createHash(Node left, Node right) {
        String hashString = left.getHash() + right.getHash();

        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algorithm not found.");
        }
        byte[] hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));

        // Convert our byte array to a string
        return new String(hash, StandardCharsets.UTF_8);
    }

    /**
     * Public facing method to traverse the tree in order and print node hashes.
     */
    public void inOrder() {
        inOrder(root);
    }

    /**
     * Private helper method to traverse the Merkle tree in order.
     * @param x    The root node.
     */
    private void inOrder(Node x) {
        if (x == null)
            return;
        inOrder(x.getLeft());
        System.out.println("Hash Value is " + x.getHash());
        inOrder(x.getRight());
    }

    /**
     * Returns the root node of a Merkle tree.
     * @return    The root node of the Merkle tree.
     */
    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "MerkleTree{" +
                "root=" + root +
                '}';
    }
}
