package mchorse.blockbuster.network.client;

import mchorse.blockbuster.network.common.PacketActorPause;
import mchorse.blockbuster.recording.RecordPlayer;
import mchorse.blockbuster.utils.EntityUtils;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerActorPause extends ClientMessageHandler<PacketActorPause>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketActorPause message)
    {
        EntityLivingBase actor = (EntityLivingBase) player.worldObj.getEntityByID(message.id);
        RecordPlayer playback = EntityUtils.getRecordPlayer(actor);

        if (playback != null)
        {
            if (message.pause) playback.pause();
            else playback.resume(message.tick, null);

            playback.tick = message.tick;
            playback.playing = !message.pause;

            if (playback.record != null)
            {
                playback.applyFrame(message.tick, actor, true);
                actor.renderYawOffset = actor.prevRenderYawOffset = actor.rotationYawHead;
            }
        }
    }
}