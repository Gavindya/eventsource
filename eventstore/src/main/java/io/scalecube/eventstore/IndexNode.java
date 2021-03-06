package io.scalecube.eventstore;

import java.util.Iterator;

public class IndexNode extends Node {

  public IndexNode(NodeDriver driver, int pos) {
    super(driver, pos);
  }

  public Node getLeftChild(int index) {
    long value = driver.getValue(pos, index);
    return driver.loadNode(value);
  }

  public Node getRightChild(int index) {
    long value = driver.getValue(pos, index + 1);
    return driver.loadNode(value);
  }

  public void putLeftChildPos(int index, long val) {
    putValue(index, val);
  }

  public void putRightChildPos(int index, long val) {
    putValue(index + 1, val);
  }

  @Override
  public Status put(byte[] key, byte[] value) {
    int index = driver.binarySearch(pos, getKeyCount(), key);
    Node node;
    if (index == -1) {
      index = 0;
      node = getLeftChild(0);
    } else {
      index = Math.abs(adjustIndex(index));
      node = getRightChild(index);
    }
    return node.put(key, value);
  }

  @Override
  public Iterator<NodeEntry> findGte(byte[] key) {
    int index = driver.binarySearch(pos, getKeyCount(), key);
    Node node;
    if (index == -1) {
      index = 0;
      node = getLeftChild(0);
    } else {
      index = Math.abs(adjustIndex(index));
      node = getRightChild(index);
    }
    return node.findGte(key);
  }

  /**
   * insert key with child node reference.
   *
   * @param key key to insert in to index node
   * @param leftChildPos left side child position of the given key
   * @param rightChildPos right side child position of the given key
   * @return key status
   */
  public Status putChild(byte[] key, long leftChildPos, long rightChildPos) {
    int keyCount = getKeyCount();
    if (isFull()) {
      return Status.NODE_FULL;
    }

    int index;
    if (keyCount == 0) {
      index = 0;
    } else {
      index = driver.binarySearch(pos, keyCount, key);
      if (index == -1) {
        index = 0;
      } else if (index < 0) {
        index = adjustIndex(index);
        index = Math.abs(index);
        index++;
      }
    }
    moveKeyIndex(index, keyCount);
    moveValueIndex(index, keyCount + 1);
    putKey(index, driver.getKeyProvider().apply(key));
    putLeftChildPos(index, leftChildPos);
    putRightChildPos(index, rightChildPos);
    updateKeyCount(keyCount + 1);
    return Status.DONE;
  }

  @Override
  public Node split(byte[] key) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
    // | Templates.
  }

}
