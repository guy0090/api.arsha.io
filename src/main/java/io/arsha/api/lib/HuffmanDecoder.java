package io.arsha.api.lib;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.arsha.api.exceptions.CannotBeRegisteredException;
import org.springframework.stereotype.Component;
import org.thshsh.struct.Struct;

// Implementation from https://github.com/shrddr/huffman_heap
@Component
public class HuffmanDecoder {

    private static Unpacked read(byte[] buffer, String format) {
        Struct<List<Object>> struct = Struct.create("<" + format);
        int size = struct.byteCount();
        byte[] packed = new byte[size];
        System.arraycopy(buffer, 0, packed, 0, size);

        byte[] resized = new byte[buffer.length - size];
        System.arraycopy(buffer, size, resized, 0, buffer.length - size);

        return new Unpacked(struct.unpack(packed), resized);
    }

    private static Frequencies getFrequencies(byte[] buffer) {
        Unpacked data = read(buffer, "3I");
        long chars = (long) data.get(2);

        Map<String, Long> freqs = new LinkedHashMap<>();
        for (int i = 0; i < chars; i++) {
            data = read(data.resized, "I");
            long freq = (long) data.get(0);

            data = read(data.resized, "c3b");
            String char_ = new String(new byte[] { (byte) data.get(0) });
            freqs.put(char_, freq);
        }

        return new Frequencies(freqs, data.resized);
    }

    private static Node makeTree(Frequencies freqs) {
        MinHeap heap = new MinHeap();
        for (Map.Entry<String, Long> entry : freqs.freqs.entrySet()) {
            heap.push(new Node(entry.getKey(), entry.getValue()));
        }

        while (heap.size() > 1) {
            Node n1 = heap.pop();
            Node n2 = heap.pop();
            Node parent = new Node(n1.c + n2.c, n1.f + n2.f, n1, n2);
            heap.push(parent);
        }

        return heap.pop();
    }

    public String decode(byte[] buffer) throws CannotBeRegisteredException {
        var testResult = new String(buffer);
        if (testResult.contains("resultMsg")) { // Probably an item that can't be registered on market (code 8)
            throw new CannotBeRegisteredException(testResult);
        }

        Frequencies freqs = getFrequencies(buffer);
        Node tree = makeTree(freqs);

        Unpacked data = read(freqs.resized, "3I");
        long packedBits = (long) data.get(0);

        BitList bits = new BitList((int) packedBits).init(data.resized);
        StringBuilder unpacked = new StringBuilder();
        int pos = 0;

        while (pos < bits.length()) {
            Node node = tree;
            while (true) {
                if (pos >= bits.size()) {
                    throw new RuntimeException("Invalid data: pos (" + pos + ") >= bits.size (" + bits.size() + ")");
                }

                if (bits.get(pos)) {
                    node = node.r;
                } else {
                    node = node.l;
                }

                pos += 1;
                if (node == null) {
                    throw new RuntimeException("Invalid data: node");
                }

                if (node.l == null && node.r == null) {
                    break;
                }
            }
            unpacked.append(node.c);
        }

        return unpacked.toString();
    }
}

// Decoding helper classes
class Unpacked {
    final List<Object> result;
    final byte[] resized;

    public Unpacked(List<Object> result, byte[] resized) {
        this.result = result;
        this.resized = resized;
    }

    public Object get(int index) {
        return result.get(index);
    }
}

class Frequencies {
    final Map<String, Long> freqs;
    final byte[] resized;

    public Frequencies(Map<String, Long> freqs, byte[] resized) {
        this.freqs = freqs;
        this.resized = resized;
    }
}

class BitList extends BitSet {

    public BitList(int numBits) {
        super(numBits);
    }

    public BitList init(byte[] buffer) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : buffer) {
            binaryString.append(Integer.toBinaryString(b & 255 | 256).substring(1));
        }

        for (int i = 0; i < this.size() && i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                this.set(i);
            }
        }

        return this;
    }
}

class MinHeap {
    final LinkedList<Node> heap;

    public MinHeap() {
        heap = new LinkedList<>();
    }

    public int size() {
        return heap.size();
    }

    public void swap (int i, int j) {
        Node temp = this.heap.get(i);
        this.heap.set(i, this.heap.get(j));
        this.heap.set(j, temp);
    }

    public void push(Node node) {
        this.heap.add(node);
        int childIndex = this.heap.size() - 1;

        while (true) {
            int parentIndex = (childIndex - 1) / 2;
            if (parentIndex < 0) parentIndex = 0;

            if (this.heap.get(parentIndex).le(this.heap.get(childIndex))) return;

            this.swap(parentIndex, childIndex);
            childIndex = parentIndex;
            if (childIndex <= 0) return;
        }
    }

    public Node pop() {
        Node node = this.heap.get(0);
        Node last = this.heap.removeLast();

        if (this.size() == 0) return node;
        this.heap.set(0, last);

        int parentIndex = 0;
        int childIndex = 1;
        while (childIndex < this.size()) {
            if (childIndex + 1 < this.size() && this.heap.get(childIndex + 1).lt(this.heap.get(childIndex))) {
                childIndex += 1;
            }

            if (this.heap.get(parentIndex).le(this.heap.get(childIndex))) {
                return node;
            }

            this.swap(parentIndex, childIndex);
            parentIndex = childIndex;
            childIndex = 2 * childIndex + 1;
        }

        return node;
    }
}

class Node {
    String c;
    Long f;
    Node l;
    Node r;

    public Node(String c, Long f, Node l, Node r) {
        this.c = c;
        this.f = f;
        this.l = l;
        this.r = r;
    }

    public Node(String c, Long f) {
        this.c = c;
        this.f = f;
        this.l = null;
        this.r = null;
    }

    public boolean lt(Node other) {
        return this.f < other.f;
    }

    public boolean le(Node other) {
        return this.f <= other.f;
    }
}