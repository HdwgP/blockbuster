package mchorse.blockbuster.recording;

import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.recording.data.Frame;
import mchorse.blockbuster.recording.data.Mode;
import mchorse.blockbuster.recording.data.Record;

/**
 * Record player class
 *
 * This thing is responsible for playing given record. It applies frames and
 * actions from the record instance on the given actor.
 */
public class RecordPlayer
{
    /**
     * Record from which this player is going to play
     */
    public Record record;

    /**
     * Play mode
     */
    public Mode mode;

    /**
     * Current tick
     */
    public int tick = 0;

    /**
     * Speed of playback (or delay between frames) in frames
     */
    public int delay = 1;

    /**
     * Temporary speed of playback. This is used only when record is absent
     */
    public int recordDelay = 1;

    /**
     * Whether to kill an actor when player finished playing
     */
    public boolean kill = false;

    /**
     * Is this player is playing
     */
    public boolean playing = true;

    public RecordPlayer(Record record, Mode mode)
    {
        this.record = record;
        this.mode = mode;
    }

    /**
     * Check if the record player is finished
     */
    public boolean isFinished()
    {
        return this.record != null && this.tick - this.record.preDelay - this.record.postDelay >= this.record.getLength();
    }

    /**
     * Get appropriate amount of real ticks (for accessing current 
     * action or something like this)
     */
    public int getTick()
    {
        return Math.max(0, this.record == null ? this.tick : this.tick - this.record.preDelay);
    }

    /**
     * Get current frame 
     */
    public Frame getCurrentFrame()
    {
        return this.record.frames.get(this.getTick());
    }

    /**
     * Apply current frame and advance to the next one
     */
    public void next(EntityActor actor)
    {
        if (this.isFinished() || --this.delay > 0)
        {
            return;
        }

        if (this.record != null)
        {
            boolean both = this.mode == Mode.BOTH;

            if (this.mode == Mode.ACTIONS || both) this.record.applyAction(this.tick - this.record.preDelay, actor);
            if (this.mode == Mode.FRAMES || both) this.record.applyFrame(this.tick - this.record.preDelay, actor, false);

            this.delay = this.record.delay;
            this.record.resetUnload();
        }
        else
        {
            this.delay = this.recordDelay;
        }

        /* Align the body with the head on spawn */
        if (this.tick == 0)
        {
            actor.renderYawOffset = actor.rotationYaw;
        }

        this.tick++;
    }
}