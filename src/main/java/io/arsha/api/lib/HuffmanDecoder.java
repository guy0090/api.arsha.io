package io.arsha.api.lib;

import io.arsha.api.exceptions.CannotBeRegisteredException;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.springframework.stereotype.Component;
import org.thshsh.struct.Struct;

// Implementation from https://github.com/shrddr/huffman_heap
@Component
public class HuffmanDecoder {

    record Node(@Nullable String character, @NotNull Long frequency, @Nullable Node left, @Nullable Node right) {
        public static Node fromEntry(Map.Entry<String, Long> entry) {
            return new Node(entry.getKey(), entry.getValue(), null, null);
        }

        public static Node branch(Node left, Node right) {
            return new Node(null, left.frequency() + right.frequency(), left, right);
        }
    }

    private static Map<String, Long> getFrequencies(InputStream buffer) throws IOException {
        var data = Struct.create("<3I").unpack(buffer);
        var chars = (long) data.get(2);

        var frequencyMap = new LinkedHashMap<String, Long>();
        for (int i = 0; i < chars; i++) {
            var charData = Struct.create("<Ic3b").unpack(buffer);
            var frequency = (long) charData.get(0);
            var character = new String(new byte[] { (byte) charData.get(1) });
            frequencyMap.put(character, frequency);
        }
        return frequencyMap;
    }

    private static Node getRoot(InputStream stream) throws IOException {
        var frequencies = getFrequencies(stream);

        var heap = new PriorityQueue<>(Comparator.comparingLong(Node::frequency));
        frequencies.entrySet().stream()
            .map(Node::fromEntry)
            .forEach(heap::add);

        while (heap.size() > 1) {
            var left = heap.poll();
            var right = heap.poll();
            if (left == null || right == null) {
                throw new IllegalStateException("Invalid heap: a leaf node is null");
            }
            var branch = Node.branch(left, right);
            heap.add(branch);
        }

        return heap.peek();
    }

    private static String unpack(Node root, StringBuilder packed) {
        var current = root;
        var unpacked = new StringBuilder();
        for (var i = 0; i < packed.length(); i++) {
            current = packed.charAt(i) == '0' ? current.left() : current.right();
            assert current != null;

            if (current.left() == null && current.right() == null) {
                unpacked.append(current.character());
                current = root;
            }
        }
        return unpacked.toString();
    }

    public String decode(byte[] buffer) throws CannotBeRegisteredException, IOException {
        var testResult = new String(buffer);
        if (testResult.contains("resultMsg")) { // Probably an item that can't be registered on market (code 8)
            throw new CannotBeRegisteredException(testResult);
        }

        var dataIn = new ByteArrayInputStream(buffer);
        var root = getRoot(dataIn);

        var data = Struct.create("<3I").unpack(dataIn);
        var bits = (long) data.getFirst(); // The bit size of the encoded data
        // var bytes = (long) data.get(1); // The byte size of the encoded data
        // var expectedSize = (long) data.get(2); // The expected byte size of the unpacked data

        var bin = new StringBuilder();
        for (byte b : dataIn.readAllBytes())
            bin.append(Integer.toBinaryString(b & 255 | 256).substring(1));
        if (bin.length() > bits) bin.setLength(Math.toIntExact(bits));

        return unpack(root, bin);
    }
}
