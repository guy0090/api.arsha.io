package io.arsha.api.lib;

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
// This isn't entirely accurate anymore, but it used to basically be a direct copy so im keeping it
@Component
public class HuffmanDecoder {

    private static Map<String, Long> getFrequencies(InputStream buffer) throws IOException {
        var data = Struct.create("<3I").unpack(buffer);
        var chars = (long) data.get(2);

        var frequencyMap = new LinkedHashMap<String, Long>();
        for (int i = 0; i < chars; i++) {
            var charData = Struct.create("<Ic3b").unpack(buffer);
            var frequency = (long) charData.get(0);
            var character = new String(new byte[]{(byte) charData.get(1)});
            frequencyMap.put(character, frequency);
        }
        return frequencyMap;
    }

    private static Node getRoot(InputStream stream) throws IOException {
        var frequencies = getFrequencies(stream);

        var heap = new PriorityQueue<>(Comparator.comparingLong(Node::frequency));
        frequencies.entrySet().stream().map(Node::fromEntry).forEach(heap::add);

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

    public String unpack(byte[] buffer) throws IOException {
        var stream = new ByteArrayInputStream(buffer);

        var root = getRoot(stream);
        var sizes = Struct.create("<3I").unpack(stream);
        var bits = (long) sizes.getFirst(); // The bit size of the encoded data
        // var bytes = (long) sizes.get(1); // The byte size of the encoded data
        var expectedBytes = (long) sizes.get(2); // The expected byte size of the unpacked data

        var counter = 0;
        var current = root;
        var unpacked = new StringBuilder();
        outer:
        for (var b : stream.readAllBytes()) {
            if (unpacked.length() > expectedBytes) {
                throw new IllegalStateException("Unpacked data exceeds expected size");
            }

            for (int i = 7; i >= 0; i--) {
                if (counter >= bits) {
                    break outer;
                }
                int bit = (b >> i) & 1;
                current = bit == 0 ? current.left() : current.right();
                assert current != null;
                if (current.left() == null && current.right() == null) {
                    unpacked.append(current.character());
                    current = root;
                }
                counter++;
            }
        }

        return unpacked.toString();
    }

    record Node(@Nullable String character, @NotNull Long frequency, @Nullable Node left, @Nullable Node right) {

        public static Node fromEntry(Map.Entry<String, Long> entry) {
            return new Node(entry.getKey(), entry.getValue(), null, null);
        }

        public static Node branch(Node left, Node right) {
            return new Node(null, left.frequency() + right.frequency(), left, right);
        }
    }
}
