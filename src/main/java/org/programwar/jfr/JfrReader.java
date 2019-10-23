package org.programwar.jfr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCStackTrace;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.common.item.ItemToolkit;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator.FrameCategorization;
import org.openjdk.jmc.flightrecorder.stacktrace.StacktraceFormatToolkit;

public class JfrReader {

    private Map<String, Integer> stacks = new HashMap<>();

    /**
     * @param file jfr file
     */
    public JfrReader(File file) {
        validateFile(file);
        try {
            IItemCollection collection = JfrLoaderToolkit.loadEvents(file);
            buildStacks(collection);
        } catch (IOException | CouldNotLoadRecordingException e) {
            e.printStackTrace();
            stacks = null;
        }
    }

    public Map<String, Integer> getStacksMap() {
        return stacks;
    }

    public String getStacks() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String stack : stacks.keySet()) {
            int num = stacks.get(stack);
            stringBuilder.append(String.format("%s %d%n", stack, num));
        }
        return stringBuilder.toString();
    }

    private void validateFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file + " does not exist");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " is not a file");
        }
    }

    private void buildStacks(IItemCollection collection) {
        IItemCollection executionSamples = collection.apply(ItemFilters.type(JdkTypeIDs.EXECUTION_SAMPLE));

        for (IItemIterable iItems : executionSamples) {
            for (IItem iItem : iItems) {
                String stack = this.getStack(iItem);
                int value = stacks.computeIfAbsent(stack, s -> 0);
                stacks.put(stack, value + 1);
            }
        }
    }

    private String getStack(IItem iItem) {
        IMCStackTrace member = ItemToolkit.getItemType(iItem).getAccessor(JfrAttributes.EVENT_STACKTRACE.getKey())
            .getMember(iItem);

        List<? extends IMCFrame> frames = member.getFrames();

        List<String> methodCalls = new ArrayList<>(20);
        for (int i = frames.size() - 1; i >= 0; i--) {
            IMCFrame frame = frames.get(i);
            methodCalls.add(frameFormat(frame));
        }

        return String.join(";", methodCalls);
    }

    private String frameFormat(IMCFrame frame) {
        return StacktraceFormatToolkit.formatFrame(
            frame,
            new FrameSeparator(FrameCategorization.METHOD, false),
            false,
            false,
            true,
            false,
            false,
            true
        );
    }
}