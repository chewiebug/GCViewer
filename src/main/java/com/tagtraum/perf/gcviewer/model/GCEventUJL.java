package com.tagtraum.perf.gcviewer.model;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 03.01.2018</p>
 */
public class GCEventUJL extends GCEvent {

    @Override
    public boolean isFull() {
        // assumption AbstractGCEvent does not hold here; only the event type itself can tell
        return getExtendedType().getGeneration().equals(Generation.ALL);
    }

    @Override
    public Generation getGeneration() {
        // assumption in AbstractGCEvent concerning "has information about several generations"
        // -> "has collected objects from several generations" is not correct for unified jvm logging events
        // they usually seem to hold information about several generations, as soon as heap details are logged
        if (generation == null) {
            generation = getExtendedType() != null ? getExtendedType().getGeneration() : null;
        }

        return generation == null ? Generation.YOUNG : generation;
    }

    @Override
    public void addPhase(AbstractGCEvent<?> phase) {
        super.addPhase(phase);

        // If it is a stop-the-world event, increase pause time for parent GC event
        if (Concurrency.SERIAL.equals(phase.getExtendedType().getConcurrency())) {
            setPause(getPause() + phase.getPause());
        }
    }
}
